package com.emarket.customer

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.getDoubleOrNull
import androidx.core.database.getStringOrNull
import com.emarket.customer.models.Product
import com.emarket.customer.models.ProductDTO
import com.emarket.customer.models.Transaction
import com.emarket.customer.models.Voucher

const val DB_NAME = "Emarket.db"
const val DB_VERSION = 1

class Database(ctx: Context) : SQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION) {

    private val tableTransactions = "Transactions"
    private val keyTransactionId = "TransactionId"
    private val colTransactionDate = "TransactionDate"
    private val colTransactionTotal = "TransactionTotal"
    private val colTransactionDiscount = "TransactionDiscount"

    private val tableProducts = "Products"
    private val keyProductId = "ProductId"
    private val colProductName = "ProductName"
    private val colProductPrice = "ProductPrice"

    private val tableVouchers = "Vouchers"
    private val keyVoucherId = "VoucherId"
    private val colVoucherDiscount = "VoucherDiscount"
    private val colVoucherUsed = "VoucherUsed"

    private val tableTransactionProducts = "TransactionProducts"
    private val keyTransactionProducts = "TransProdId"
    private val colQuantity = "Quantity"

    override fun onCreate(db: SQLiteDatabase) {
        val sqlCreateVouchers = "CREATE TABLE $tableVouchers(" +
                "$keyVoucherId VARCHAR(100) PRIMARY KEY, " +
                "$colVoucherDiscount INTEGER, " +
                "$colVoucherUsed INTEGER DEFAULT 0)"
        val sqlCreateProducts = "CREATE TABLE $tableProducts(" +
                "$keyProductId VARCHAR(100) PRIMARY KEY, " +
                "$colProductName VARCHAR(100), " +
                "$colProductPrice FLOAT)"
        val sqlCreateTransactions = "CREATE TABLE $tableTransactions(" +
                "$keyTransactionId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$colTransactionDate VARCHAR(100), " +
                "$colTransactionTotal FLOAT, " +
                "$keyVoucherId VARCHAR(100), " +
                "$colTransactionDiscount FLOAT, " +
                "FOREIGN KEY ($keyVoucherId) REFERENCES $tableVouchers($keyVoucherId))"
        val sqlCreateTransactionProducts = "CREATE TABLE $tableTransactionProducts(" +
                "$keyTransactionProducts INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$keyTransactionId INTEGER, " +
                "$keyProductId VARCHAR(100), " +
                "$colQuantity INTEGER, " +
                "FOREIGN KEY ($keyTransactionId) REFERENCES $tableTransactions($keyTransactionId), " +
                "FOREIGN KEY ($keyProductId) REFERENCES $tableProducts($keyProductId))"
        db.execSQL(sqlCreateVouchers)
        db.execSQL(sqlCreateProducts)
        db.execSQL(sqlCreateTransactions)
        db.execSQL(sqlCreateTransactionProducts)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $tableTransactionProducts")
        db.execSQL("DROP TABLE IF EXISTS $tableTransactions")
        db.execSQL("DROP TABLE IF EXISTS $tableProducts")
        db.execSQL("DROP TABLE IF EXISTS $tableVouchers")
        onCreate(db)
    }

    fun addVoucher(voucher : Voucher) {
        // check if voucher already exists
        if (this.getVoucher(voucher.id) != null) return

        val values = ContentValues().also {
            it.put(keyVoucherId, voucher.id)
            it.put(colVoucherDiscount, voucher.discount)
        }
        writableDatabase.insert(tableVouchers, null, values)
    }
    fun cleanVouchers() {
        cleanTable(tableVouchers)
    }

    /**
     * Get vouchers from the database
     * @param onlyUnUsed if false, return all the vouchers, otherwise only the ones that haven't been used
     * @return a list of vouchers
     */
    fun getVouchers(onlyUnUsed: Boolean = true) : MutableList<Voucher> {
        val vouchers = mutableListOf<Voucher>()
        val query = "SELECT * FROM $tableVouchers" + if (onlyUnUsed) " WHERE $colVoucherUsed = 0" else ""
        val cursor = readableDatabase.rawQuery(query, null)
        if (cursor.count == 0) return vouchers
        while (cursor.moveToNext()) {
            vouchers.add(Voucher(
                cursor.getString(cursor.getColumnIndexOrThrow(keyVoucherId)),
                cursor.getInt(cursor.getColumnIndexOrThrow(colVoucherDiscount)),
                cursor.getInt(cursor.getColumnIndexOrThrow(colVoucherUsed)) == 1
            ))
        }
        cursor.close()
        return vouchers
    }
    private fun getVoucher(id: String?) : Voucher? {
        if (id == null) return null
        var voucher: Voucher? = null
        val query = "SELECT * FROM $tableVouchers WHERE $keyVoucherId = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(id))
        if (cursor.moveToFirst()) {
            voucher = Voucher(
                cursor.getString(cursor.getColumnIndexOrThrow(keyVoucherId)),
                cursor.getInt(cursor.getColumnIndexOrThrow(colVoucherDiscount)),
                cursor.getInt(cursor.getColumnIndexOrThrow(colVoucherUsed)) == 1
            )
        }
        cursor.close()
        return voucher
    }
    fun updateVoucher(voucher: Voucher) {
        val values = ContentValues().also {
            it.put(keyVoucherId, voucher.id)
            it.put(colVoucherDiscount, voucher.discount)
            it.put(colVoucherUsed, voucher.used)
        }
        writableDatabase.update(tableVouchers, values, "$keyVoucherId = ?", arrayOf(voucher.id))
    }

    fun addProduct(product : Product) {
        if (getProduct(product.uuid) != null) return
        val values = ContentValues().also {
            it.put(keyProductId, product.uuid)
            it.put(colProductName, product.name)
            it.put(colProductPrice, product.price)
        }
        writableDatabase.insert(tableProducts, null, values)
    }
    private fun getProduct(id: String?) : ProductDTO? {
        if (id == null) return null
        var product: ProductDTO? = null
        val query = "SELECT * FROM $tableProducts WHERE $keyProductId = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(id))
        if (cursor.count == 0) return null
        if (cursor.moveToFirst()) {
            product = ProductDTO(id,
                cursor.getString(cursor.getColumnIndexOrThrow(colProductName)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(colProductPrice))
            )
        }
        cursor.close()
        return product
    }

    fun addTransaction(transaction: Transaction) {
        val values = ContentValues().also {
            it.put(colTransactionDate, transaction.date)
            it.put(colTransactionTotal, transaction.total)
            it.put(colTransactionDiscount, transaction.discounted)
            it.put(keyVoucherId, transaction.voucher?.id)
        }
        val transactionId = writableDatabase.insert(tableTransactions, null, values)

        transaction.products.forEach {
            addProduct(it)
            addTransProd(transactionId, it.uuid, it.qnt)
        }
    }
    fun cleanTransactions() {
        cleanTable(tableTransactions)
        cleanTransProd()
    }
    fun getTransactions() : MutableList<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val query = "SELECT * FROM $tableTransactions"
        val cursor = readableDatabase.rawQuery(query, null)
        if (cursor.count == 0) return transactions
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(keyTransactionId))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(colTransactionDate))
            val total = cursor.getDouble(cursor.getColumnIndexOrThrow(colTransactionTotal))
            val discounted = cursor.getDoubleOrNull(cursor.getColumnIndexOrThrow(colTransactionDiscount))
            val voucherId = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(keyVoucherId))
            val voucher = getVoucher(voucherId)
            val products = getTransactionProducts(id)
            transactions.add(Transaction(products,discounted, voucher, total, date))
        }
        cursor.close()
        return transactions
    }

    private fun addTransProd(transactionId: Long, productId: String, qnt: Int) {
        val values = ContentValues().also {
            it.put(keyProductId, productId)
            it.put(keyTransactionId, transactionId)
            it.put(colQuantity, qnt)
        }
        writableDatabase.insert(tableTransactionProducts, null, values)
    }
    private fun cleanTransProd() {
        cleanTable(tableTransactionProducts)
    }
    private fun getTransactionProducts(id: Int) : MutableList<Product> {
        val products = mutableListOf<Product>()
        val query = "SELECT * FROM $tableTransactionProducts WHERE $keyTransactionId = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(id.toString()))
        if (cursor.count == 0) return products
        while (cursor.moveToNext()) {
            val quantity = cursor.getInt(cursor.getColumnIndexOrThrow(colQuantity))
            val productId = cursor.getString(cursor.getColumnIndexOrThrow(keyProductId))
            val product = getProduct(productId)!!
            products.add(Product(null, product.uuid, product.name, product.price, quantity))
        }
        cursor.close()
        return products
    }

    private fun cleanTable(tableName: String) {
        writableDatabase.execSQL("DELETE FROM $tableName")
    }
}
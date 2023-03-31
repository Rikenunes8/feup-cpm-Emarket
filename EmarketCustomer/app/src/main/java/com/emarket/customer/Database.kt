package com.emarket.customer

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.emarket.customer.models.Product
import com.emarket.customer.models.Transaction
import com.emarket.customer.models.Voucher

const val DB_NAME = "Emarket.db"
const val DB_VERSION = 1

class Database(ctx: Context) : SQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION) {

    private val tableTransactions = "Transactions"
    private val keyTransaction = "TransactionId"
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

    private val tableTransactionProducts = "TransactionProducts"
    private val keyTransactionProducts = "TransProdId"
    private val colQuantity = "Quantity"

    override fun onCreate(db: SQLiteDatabase) {
        val sqlCreateVouchers = "CREATE TABLE $tableVouchers(" +
                "$keyVoucherId VARCHAR(100) PRIMARY KEY, " +
                "$colVoucherDiscount INTEGER)"
        val sqlCreateProducts = "CREATE TABLE $tableProducts(" +
                "$keyProductId VARCHAR(100) PRIMARY KEY, " +
                "$colProductName VARCHAR(100), " +
                "$colProductPrice FLOAT)"
        val sqlCreateTransactions = "CREATE TABLE $tableTransactions(" +
                "$keyTransaction INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$colTransactionDate VARCHAR(100), " +
                "$colTransactionTotal FLOAT, " +
                "$keyVoucherId VARCHAR(100), " +
                "$colTransactionDiscount FLOAT, " +
                "FOREIGN KEY ($keyVoucherId) REFERENCES $tableVouchers($keyVoucherId))"
        val sqlCreateTransactionProducts = "CREATE TABLE $tableTransactionProducts(" +
                "$keyTransactionProducts INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$keyTransaction INTEGER, " +
                "$keyProductId VARCHAR(100), " +
                "$colQuantity INTEGER, " +
                "FOREIGN KEY ($keyTransaction) REFERENCES $tableTransactions($keyTransaction), " +
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
        val values = ContentValues().also {
            it.put(keyVoucherId, voucher.id)
            it.put(colVoucherDiscount, voucher.discount)
        }
        writableDatabase.insert(tableVouchers, null, values)
    }
    fun cleanVouchers() {
        cleanTable(tableVouchers)
    }
    fun getVouchers() : MutableList<Voucher> {
        val vouchers = mutableListOf<Voucher>()
        val query = "SELECT * FROM $tableVouchers"
        val cursor = readableDatabase.rawQuery(query, null)
        if (cursor.count == 0) return vouchers
        while (cursor.moveToNext()) {
            vouchers.add(Voucher(
                cursor.getString(cursor.getColumnIndexOrThrow(keyVoucherId)),
                cursor.getInt(cursor.getColumnIndexOrThrow(colVoucherDiscount))
            ))
        }
        cursor.close()
        return vouchers
    }

    fun addProduct(product : Product) {
        if (checkProduct(product)) return
        val values = ContentValues().also {
            it.put(keyProductId, product.uuid)
            it.put(colProductName, product.name)
            it.put(colProductPrice, product.price)
        }
        writableDatabase.insert(tableProducts, null, values)
    }
    private fun checkProduct(product: Product) : Boolean {
        val query = "SELECT * FROM $tableProducts WHERE $keyProductId =?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(product.uuid))
        val hasProduct = cursor.count > 0
        cursor.close()
        return hasProduct
    }
    fun cleanProducts() {
        cleanTable(tableProducts)
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
            if (!checkProduct(it)) addProduct(it)
            addTransProd(transactionId, it.uuid, it.qnt)
        }
    }
    fun cleanTransactions() {
        cleanTable(tableTransactions)
        cleanTransProd()
    }

    fun addTransProd(transactionId: Long, productId: String, qnt: Int) {
        val values = ContentValues().also {
            it.put(keyProductId, productId)
            it.put(keyTransaction, transactionId)
            it.put(colQuantity, qnt)
        }
        writableDatabase.insert(tableTransactionProducts, null, values)
    }
    fun cleanTransProd() {
        cleanTable(tableTransactionProducts)
    }

    private fun cleanTable(tableName: String) {
        writableDatabase.execSQL("DELETE FROM $tableName")
    }
}
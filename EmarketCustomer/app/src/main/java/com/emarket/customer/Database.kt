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
    private val colVoucherPercentage = "VoucherPercentage"

    private val tableTransactionProducts = "TransactionProducts"
    private val keyTransactionProducts = "TransProdId"
    private val colQuantity = "Quantity"



    override fun onCreate(db: SQLiteDatabase) {
        val sqlCreateVouchers = "CREATE TABLE $tableVouchers(" +
                "$keyVoucherId VARCHAR(100) PRIMARY KEY, " +
                "$colVoucherPercentage INTEGER)"
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

    fun addVoucher(voucher : Voucher) : Long {
        val values = ContentValues().also {
            it.put(keyVoucherId, voucher.id)
            it.put(colVoucherPercentage, voucher.percentage)
        }
        return writableDatabase.insert(tableVouchers, null, values)
    }

    fun addProduct(product : Product) : Long {
        val values = ContentValues().also {
            it.put(keyProductId, product.uuid)
            it.put(colProductName, product.name)
            it.put(colProductPrice, product.price)
        }
        return writableDatabase.insert(tableProducts, null, values)
    }

    fun addTransaction(transaction: Transaction) : Long {
        for (x in transaction.products) {
            addProduct(x)
        }

        val values = ContentValues().also {
            it.put(colTransactionDate, transaction.date)
            it.put(colTransactionTotal, transaction.total)
            it.put(colTransactionDiscount, transaction.discounted)
            it.put(keyVoucherId, transaction.voucher?.id)
        }
        return writableDatabase.insert(tableProducts, null, values)
    }

}
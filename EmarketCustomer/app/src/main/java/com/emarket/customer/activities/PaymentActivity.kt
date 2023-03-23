package com.emarket.customer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.emarket.customer.R
import com.emarket.customer.models.Transaction
import com.google.gson.Gson

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val data = intent.getStringExtra("Transaction")
        val transaction = Gson().fromJson(data, Transaction::class.java)
        Log.d("Transaction", transaction.voucher.toString())
        Log.d("Transaction", transaction.discounted.toString())
        Log.d("Transaction", transaction.products.size.toString())
    }
}
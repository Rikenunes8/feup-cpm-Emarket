package com.emarket.customer.activities

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.controllers.ProductsListAdapter
import com.emarket.customer.controllers.VoucherListAdapter
import com.emarket.customer.models.Product
import com.emarket.customer.models.Transaction
import com.emarket.customer.models.Voucher
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class CheckoutActivity : AppCompatActivity() {
    private lateinit var transaction : Transaction
    private var vouchers = mutableListOf(
        Voucher("1", 15),
        Voucher("2", 15),
        Voucher("3", 15),
        Voucher("4", 15),
        Voucher("5", 15)) // TODO get this from other place
    private val accAmount = 13.04 // TODO get this from other place

    private val voucherView by lazy { findViewById<RecyclerView>(R.id.rv_voucher) }
    private val basketView by lazy { findViewById<RecyclerView>(R.id.rv_basket) }
    private val accAmountView by lazy { findViewById<TextView>(R.id.acc_amount) }
    private val totalView by lazy { findViewById<TextView>(R.id.total_price) }
    private val discountCheck by lazy { findViewById<CheckBox>(R.id.discount) }
    private val discountView by lazy { findViewById<TextView>(R.id.discount_price) }
    private val confirmButton by lazy { findViewById<Button>(R.id.confirm_btn) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(Constants.BASKET_ITEMS, null)

        transaction = Transaction(
            Gson().fromJson(json, object : TypeToken<MutableList<Product>>() {}.type),
            0.0,
            null
        )

        voucherView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        basketView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        voucherView.adapter = VoucherListAdapter(vouchers)
        basketView.adapter = ProductsListAdapter(transaction.products)

        accAmountView.text = getString(R.string.template_price, accAmount)

        val total = transaction.products.fold(0.0) { sum, product -> sum + product.price }
        totalView.text = getString(R.string.template_price, total)

        discountCheck.setOnCheckedChangeListener { _, isChecked ->
            totalView.paintFlags = if (isChecked) Paint.STRIKE_THRU_TEXT_FLAG else 0
            discountView.text = if (isChecked) getString(R.string.template_price, maxOf(total - accAmount, 0.0) ) else ""
        }

        confirmButton.setOnClickListener {
            transaction.discounted = if (discountCheck.isChecked) minOf(total, accAmount) else 0.0
            transaction.voucher = (voucherView.adapter as VoucherListAdapter).getSelectedItem()

            val qrcode = Intent(this, TransactionQRcode::class.java).apply {
                putExtra("Transaction", Gson().toJson(transaction))
            }
            startActivity(qrcode)
        }


    }
}


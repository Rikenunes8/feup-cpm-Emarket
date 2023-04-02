package com.emarket.customer.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CheckoutActivity : AppCompatActivity() {
    private lateinit var transaction : Transaction
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
        val products = Gson().fromJson<MutableList<Product>>(json, object : TypeToken<MutableList<Product>>() {}.type)

        transaction = Transaction(
            products,
            0.0,
            null,
            products.fold(0.0) { sum, product -> sum + product.price * product.qnt }
        )

        voucherView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val orientation = if (Configuration.ORIENTATION_PORTRAIT == resources.configuration.orientation) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL
        if (Configuration.ORIENTATION_PORTRAIT == resources.configuration.orientation)
            basketView.isNestedScrollingEnabled = false
        basketView.layoutManager = LinearLayoutManager(this, orientation, false)
        voucherView.adapter = VoucherListAdapter(vouchers, true)
        basketView.adapter = ProductsListAdapter(transaction.products)

        accAmountView.text = getString(R.string.template_price, accAmount)
        totalView.text = getString(R.string.template_price, transaction.total)

        discountCheck.setOnCheckedChangeListener { _, isChecked ->
            totalView.paintFlags = if (isChecked) Paint.STRIKE_THRU_TEXT_FLAG else 0
            discountView.text = if (isChecked) getString(R.string.template_price, maxOf(transaction.total - accAmount, 0.0) ) else ""
        }

        confirmButton.setOnClickListener {
            transaction.total = transaction.products.fold(0.0) { sum, product -> sum + product.price * product.qnt}
            transaction.discounted = if (discountCheck.isChecked) minOf(transaction.total, accAmount) else 0.0
            transaction.voucher = (voucherView.adapter as VoucherListAdapter).getSelectedItem()
            transaction.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"))

            // TODO: REMOVE THIS. We must update vouchers when we get confirmation from server.
            if (transaction.voucher != null) {
                transaction.voucher!!.used = true
                dbLayer.updateVoucher(transaction.voucher!!)
            }

            val qrcode = Intent(this, PaymentActivity::class.java).apply {
                putExtra("Transaction", Gson().toJson(transaction))
            }
            startActivity(qrcode)
        }
    }
}


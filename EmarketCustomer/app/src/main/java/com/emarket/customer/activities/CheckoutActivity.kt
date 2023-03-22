package com.emarket.customer.activities

import android.content.Context
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class CheckoutActivity : AppCompatActivity() {
    private var vouchers = mutableListOf(15, 15, 15, 15, 15)
    private lateinit var productItems : MutableList<Product>
    private val accAmount = 13.04

    private val voucherView by lazy { findViewById<RecyclerView>(R.id.rv_voucher) }
    private val basketView by lazy { findViewById<RecyclerView>(R.id.rv_basket) }
    private val accAmountView by lazy { findViewById<TextView>(R.id.acc_amount) }
    private val totalView by lazy { findViewById<TextView>(R.id.total_price) }
    private val discountCheck by lazy { findViewById<CheckBox>(R.id.discount) }
    private val discountView by lazy { findViewById<TextView>(R.id.discount_price) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(Constants.BASKET_ITEMS, null)
        productItems = Gson().fromJson(json, object : TypeToken<MutableList<Product>>() {}.type)

        voucherView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        basketView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        voucherView.adapter = VoucherListAdapter(vouchers)
        basketView.adapter = ProductsListAdapter(productItems)

        accAmountView.text = getString(R.string.template_price, accAmount)

        val sum = productItems.fold(0.0) { total, product -> total + product.price }
        totalView.text = getString(R.string.template_price, sum)

        discountCheck.setOnCheckedChangeListener { _, isChecked ->
            totalView.paintFlags = if (isChecked) Paint.STRIKE_THRU_TEXT_FLAG else 0
            discountView.text = if (isChecked) getString(R.string.template_price, sum - accAmount) else ""
        }
    }
}


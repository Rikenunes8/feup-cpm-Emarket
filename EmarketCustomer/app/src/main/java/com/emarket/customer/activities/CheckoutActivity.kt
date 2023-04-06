package com.emarket.customer.activities

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.controllers.Fetcher.Companion.vouchers
import com.emarket.customer.controllers.ProductsListAdapter
import com.emarket.customer.controllers.VoucherListAdapter
import com.emarket.customer.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class CheckoutActivity : AppCompatActivity() {
    private lateinit var basket : Basket

    private val voucherView by lazy { findViewById<RecyclerView>(R.id.rv_voucher) }
    private val basketView by lazy { findViewById<RecyclerView>(R.id.rv_basket) }
    private val amountToDiscountView by lazy { findViewById<TextView>(R.id.acc_amount) }
    private val totalView by lazy { findViewById<TextView>(R.id.total_price) }
    private val discountCheck by lazy { findViewById<CheckBox>(R.id.discount) }
    private val discountView by lazy { findViewById<TextView>(R.id.tv_discounted_price) }
    private val confirmButton by lazy { findViewById<Button>(R.id.confirm_btn) }
    private val noVouchersView by lazy { findViewById<TextView>(R.id.tv_no_vouchers) }
    private val accumulatedAmountCardView by lazy { findViewById<CardView>(R.id.accumulated_amount_cv) }
    private val accumulatedAmountSubtitle by lazy { findViewById<TextView>(R.id.accumulated_amount_subtitle) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val amountToDiscount = UserViewModel(application).user!!.amountToDiscount
        val sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(Constants.BASKET_ITEMS, null)
        val products = Gson().fromJson<MutableList<Product>>(json, object : TypeToken<MutableList<Product>>() {}.type)

        val total = products.fold(0.0) { sum, product -> sum + product.price * product.quantity }

        if (amountToDiscount > 0.0) {
            accumulatedAmountCardView.visibility = View.VISIBLE
            accumulatedAmountSubtitle.visibility = View.VISIBLE
        }
        voucherView.adapter = VoucherListAdapter(vouchers, true)
        basketView.adapter = ProductsListAdapter(products)

        if (vouchers.isEmpty()) {
            voucherView.visibility = View.GONE
            noVouchersView.visibility = View.VISIBLE
        }
        amountToDiscountView.text = getString(R.string.template_price, amountToDiscount)
        totalView.text = getString(R.string.template_price, total)

        discountCheck.setOnCheckedChangeListener { _, isChecked ->
            totalView.paintFlags = if (isChecked) Paint.STRIKE_THRU_TEXT_FLAG else 0
            discountView.text = if (isChecked) getString(R.string.template_price, maxOf(total - amountToDiscount, 0.0) ) else ""
        }

        confirmButton.setOnClickListener {
            basket = Basket(
                products.map { ProductToCheckout(it.uuid, it.price, it.quantity) },
                discountCheck.isChecked,
                (voucherView.adapter as VoucherListAdapter).getSelectedItem()?.id
            )

            val qrcode = Intent(this, PaymentActivity::class.java).apply {
                putExtra("Basket", Gson().toJson(basket))
            }
            startActivity(qrcode)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

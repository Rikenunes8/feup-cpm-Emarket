package com.emarket.customer.activities

import android.content.res.Configuration
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.R
import com.emarket.customer.controllers.ProductsListAdapter
import com.emarket.customer.models.Transaction
import com.google.gson.Gson

class TransactionDetailsActivity : AppCompatActivity() {

    private val transactionDateTv by lazy { findViewById<TextView>(R.id.rv_transaction_date) }
    private val voucherCv by lazy { findViewById<CardView>(R.id.cv_voucher) }
    private val productRecyclerView by lazy { findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_transaction_products) }
    private val accumulatedLinearLayout by lazy { findViewById<LinearLayout>(R.id.accumulated_holder) }
    private val totalPriceTv by lazy { findViewById<TextView>(R.id.tv_total_price) }
    private val discountedTv by lazy { findViewById<TextView>(R.id.tv_discounted) }

    private lateinit var transaction: Transaction;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)

        title = getString(R.string.header_transaction_detail)

        productRecyclerView.isNestedScrollingEnabled = false

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        transaction = Gson().fromJson(intent.getStringExtra("transaction"), Transaction::class.java)

        transactionDateTv.text = transaction.date

        if (transaction.voucher == null) {
            voucherCv.visibility = CardView.GONE
            accumulatedLinearLayout.visibility = LinearLayout.GONE
        } else {
            setVoucherInfo()
            setAccumulatedValue()
        }

        totalPriceTv.text = getString(R.string.template_price, transaction.total)
        discountedTv.text = getString(R.string.template_price, transaction.discounted)

        val products = transaction.products
        val adapter = ProductsListAdapter(products)
        val orientation = if (Configuration.ORIENTATION_PORTRAIT == resources.configuration.orientation)
            RecyclerView.VERTICAL else RecyclerView.HORIZONTAL
        productRecyclerView.apply {
            this.adapter = adapter
            this.layoutManager = LinearLayoutManager(this@TransactionDetailsActivity, orientation, false)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * Set UI voucher information
     */
    private fun setVoucherInfo() {
        val voucher = transaction.voucher!!
        val voucherPercentage = getString(R.string.template_percentage, voucher.discount)

        val voucherIcon = findViewById<TextView>(R.id.voucher_icon)
        val voucherDiscount = findViewById<TextView>(R.id.voucher_discount)

        voucherIcon.text = voucherPercentage
        voucherDiscount.text = voucherPercentage
    }

    /**
     * Set UI accumulated value from using the voucher
     */
    private fun setAccumulatedValue() {
        val accumulatedTv = findViewById<TextView>(R.id.accumulated)
        val accumulatedValue = transaction.total / transaction.voucher!!.discount
        accumulatedTv.text = getString(R.string.template_price, accumulatedValue)
    }
}
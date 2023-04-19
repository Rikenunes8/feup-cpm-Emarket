package com.emarket.customer.activities

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.R
import com.emarket.customer.Utils
import com.emarket.customer.controllers.adapters.ProductsListAdapter
import com.emarket.customer.models.Transaction
import com.google.gson.Gson

class TransactionDetailsActivity : AppCompatActivity() {

    private val transactionDateTv by lazy { findViewById<TextView>(R.id.rv_transaction_date) }
    private val voucherCv by lazy { findViewById<CardView>(R.id.cv_voucher) }
    private val productRecyclerView by lazy { findViewById<RecyclerView>(R.id.rv_transaction_products) }
    private val accumulatedLinearLayout by lazy { findViewById<LinearLayout>(R.id.accumulated_holder) }
    private val totalPriceTv by lazy { findViewById<TextView>(R.id.tv_total_price) }
    private val discountedPriceTv by lazy { findViewById<TextView>(R.id.tv_discounted_price) }
    private val voucherIcon by lazy { findViewById<TextView>(R.id.voucher_icon) }
    private val voucherDiscount by lazy { findViewById<TextView>(R.id.voucher_discount) }
    private val accumulatedTv by lazy { findViewById<TextView>(R.id.accumulated) }

    private lateinit var transaction: Transaction;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        transaction = Gson().fromJson(intent.getStringExtra("transaction"), Transaction::class.java)
        transactionDateTv.text = transaction.date?.let { Utils.formatDate(it) }
        totalPriceTv.text = getString(R.string.template_price, transaction.total)

        transaction.discounted?.let {
            totalPriceTv.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            discountedPriceTv.text = getString(R.string.template_price, (transaction.total - transaction.discounted!!))
        }

        if (transaction.voucher == null) {
            voucherCv.visibility = View.GONE
            accumulatedLinearLayout.visibility = View.GONE
        } else {
            setVoucherInfo()
            setAccumulatedValue()
        }

        productRecyclerView.adapter = ProductsListAdapter(transaction.products)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * Set UI voucher information
     */
    private fun setVoucherInfo() {
        val voucher = transaction.voucher!!
        val voucherPercentage = getString(R.string.template_percentage, voucher.discount)

        voucherIcon.text = voucherPercentage
        voucherDiscount.text = voucherPercentage
    }

    /**
     * Set UI accumulated value from using the voucher
     */
    private fun setAccumulatedValue() {
        val accumulatedValue = transaction.total * transaction.voucher!!.discount / 100
        accumulatedTv.text = getString(R.string.template_price, accumulatedValue)
    }
}

package com.emarket.customer.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.emarket.customer.R
import com.emarket.customer.controllers.ProductsListAdapter
import com.emarket.customer.models.Transaction
import com.emarket.customer.models.Voucher
import com.google.gson.Gson

class TransactionDetailsActivity : AppCompatActivity() {

    private val transactionDateTv by lazy { findViewById<TextView>(R.id.rv_transaction_date) }
    private val voucherCv by lazy { findViewById<CardView>(R.id.cv_voucher) }
    private val productRecyclerView by lazy { findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_transaction_products) }

    private lateinit var transaction: Transaction;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)


        // Set the title of the transaction in the top bar
        title = getString(R.string.header_transaction_detail)

        // Set the back button to go to the previous page
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Get the transaction information from the intent
        transaction = Gson().fromJson(intent.getStringExtra("transaction"), Transaction::class.java)

        // Set the transaction information
        transactionDateTv.text = transaction.date

        if (transaction.voucher == null) {
            voucherCv.visibility = CardView.GONE
        } else {
            setVoucherInfo(transaction.voucher!!)
            voucherCv.visibility = CardView.VISIBLE
        }

        val products = transaction.products
        val adapter = ProductsListAdapter(products)


        productRecyclerView.apply {
            this.adapter = adapter
            this.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@TransactionDetailsActivity)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * Set UI voucher information
     * @param voucher Voucher object
     */
    private fun setVoucherInfo(voucher: Voucher) {
        val voucherPercentage = getString(R.string.template_percentage, voucher.discount)

        val voucherIcon = findViewById<TextView>(R.id.voucher_icon)
        val voucherDiscount = findViewById<TextView>(R.id.voucher_discount)

        voucherIcon.text = voucherPercentage
        voucherDiscount.text = voucherPercentage
    }
}
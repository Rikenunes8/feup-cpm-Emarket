package com.emarket.customer.controllers

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.R
import com.emarket.customer.models.Transaction

class TransactionsListAdapter(private val transactionItems: MutableList<Transaction>) :
    RecyclerView.Adapter<TransactionsListAdapter.TransactionItem>() {

    class TransactionItem(private val item: View) : RecyclerView.ViewHolder(item) {
        private val total: TextView = item.findViewById(R.id.tv_total)
        private val paid: TextView = item.findViewById(R.id.tv_paid)
        private val date: TextView = item.findViewById(R.id.tv_date)
        private val transactionItem: CardView = item.findViewById(R.id.transaction_item)

        fun bindData(transaction: Transaction) {
            // TODO: Confirm this calculation
            val totalPaid = transaction.total - transaction.discounted

            total.text = item.context.getString(R.string.template_price, transaction.total)
            paid.text = item.context.getString(R.string.template_price, totalPaid)
            date.text = transaction.date

            transactionItem.setOnClickListener {
                // TODO: Navigate to transaction detail
                Log.d("TransactionItem", "Clicked on transaction item")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, vType: Int): TransactionItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)
        return TransactionItem(view)
    }

    override fun onBindViewHolder(holder: TransactionItem, pos: Int) {
        holder.bindData(transactionItems[pos])
    }

    override fun getItemCount(): Int {
        return transactionItems.size
    }

}
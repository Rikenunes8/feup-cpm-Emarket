package com.emarket.customer.controllers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.R
import com.emarket.customer.Utils.getAttributeColor

class VoucherListAdapter(private val vouchers: MutableList<Int>) : RecyclerView.Adapter<VoucherListAdapter.ProductItem>() {
    private var checkedPosition = -1

    class ProductItem(private val item: View) : RecyclerView.ViewHolder(item) {
        private val discount: TextView by lazy { item.findViewById(R.id.voucher_discount) }
        private val cardView: CardView by lazy { item.findViewById(R.id.voucher_card) }

        fun bindData(discount_value: Int, position: Int, checkedPosition: Int, listener: OnItemClickListener) {
            discount.text = item.context.getString(R.string.template_percentage, discount_value)

            if (position == checkedPosition) {
                cardView.setCardBackgroundColor(item.context.getColor(getAttributeColor(item.context, androidx.appcompat.R.attr.colorControlActivated)))
            } else {
                cardView.setCardBackgroundColor(item.context.getColor(getAttributeColor(item.context, com.google.android.material.R.attr.colorSecondary)))
            }

            item.setOnClickListener {
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.voucher, parent, false)
        return ProductItem(view)
    }

    override fun onBindViewHolder(holder: ProductItem, position: Int) {
        holder.bindData(vouchers[position], position, checkedPosition, object : OnItemClickListener {
                override fun onItemClick(position: Int) {
                    if (checkedPosition != position) {
                        val prevCheckedPosition = checkedPosition
                        checkedPosition = position
                        notifyItemChanged(prevCheckedPosition)
                        notifyItemChanged(checkedPosition)
                    } else {
                        val prevCheckedPosition = checkedPosition
                        checkedPosition = -1
                        notifyItemChanged(prevCheckedPosition)
                    }
                }
            })
    }

    override fun getItemCount(): Int {
        return vouchers.size
    }
}

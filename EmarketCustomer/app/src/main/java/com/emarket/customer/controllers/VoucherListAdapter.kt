package com.emarket.customer.controllers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.R
import com.emarket.customer.Utils.getAttributeColor
import com.emarket.customer.models.Voucher

class VoucherListAdapter(private val vouchers: MutableList<Voucher>, private val selectable: Boolean = false) : RecyclerView.Adapter<VoucherListAdapter.VoucherItem>() {
    private var checkedPosition = -1

    class VoucherItem(private val item: View) : RecyclerView.ViewHolder(item) {
        private val discount: TextView by lazy { item.findViewById(R.id.voucher_discount) }
        private val cardView: CardView by lazy { item.findViewById(R.id.voucher_card) }

        fun bindData(discount_value: Int, position: Int, checkedPosition: Int, listener: OnItemClickListener?) {
            discount.text = item.context.getString(R.string.template_percentage, discount_value)

            if (position == checkedPosition) {
                cardView.setCardBackgroundColor(
                    item.context.getColor(
                        getAttributeColor(
                            item.context,
                            androidx.appcompat.R.attr.colorControlActivated
                        )
                    )
                )
            } else {
                cardView.setCardBackgroundColor(
                    item.context.getColor(
                        getAttributeColor(
                            item.context,
                            com.google.android.material.R.attr.colorSecondary
                        )
                    )
                )
            }

            if (listener == null) return
            item.setOnClickListener {
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.voucher, parent, false)
        return VoucherItem(view)
    }

    override fun onBindViewHolder(holder: VoucherItem, position: Int) {
        holder.bindData(vouchers[position].percentage, position, checkedPosition,
            if (!selectable) null
            else object : OnItemClickListener {
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

    fun getSelectedItem() : Voucher? {
        if (checkedPosition < 0) return null
        return vouchers[checkedPosition]
    }
}

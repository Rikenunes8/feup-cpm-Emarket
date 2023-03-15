package com.emarket.customer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Product (
    var imgRes: Int,
    var name: String,
    var price: Double,
    var qnt: Int,
    var total: Double
)

class BasketAdapter(private val ctx: Context, val items: ArrayList<Product>, private val layout: Int): RecyclerView.Adapter<BasketAdapter.BasketViewHolder>() {

    inner class BasketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.item_icon)
        val name: TextView = itemView.findViewById(R.id.item_name)
        val price: TextView = itemView.findViewById(R.id.item_price)
        val qnt: TextView = itemView.findViewById(R.id.item_qnt)
        val total: TextView = itemView.findViewById(R.id.item_total_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasketViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)
        return BasketViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BasketViewHolder, position: Int) {
        val item = items[position]
        holder.icon.setImageResource(item.imgRes)
        holder.name.text = item.name
        holder.price.text = "Price: ${item.price} €"
        holder.qnt.text = "${item.qnt} x"
        holder.total.text = "${item.total} €"
    }


    override fun getItemCount(): Int {
        return items.size
    }
}
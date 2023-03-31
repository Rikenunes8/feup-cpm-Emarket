package com.emarket.customer.controllers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.R
import com.emarket.customer.models.Product

class ProductsListAdapter(private val productItems: MutableList<Product>, private val updateTotal: (() -> Unit)? = null ) : RecyclerView.Adapter<ProductsListAdapter.ProductItem>() {

    class ProductItem(private val item: View) : RecyclerView.ViewHolder(item) {
        private val icon: ImageView = item.findViewById(R.id.item_icon)
        private val name: TextView = item.findViewById(R.id.item_name)
        private val price: TextView = item.findViewById(R.id.item_price)
        private val qnt: TextView = item.findViewById(R.id.item_qnt)
        private val total: TextView = item.findViewById(R.id.item_total_price)
        internal val delete: ImageButton = item.findViewById(R.id.delete_btn)

        fun bindData(product: Product) {
            icon.setImageResource(product.imgRes ?: R.drawable.icon)
            name.text = product.name
            qnt.text = item.context.getString(R.string.template_quantity_times, product.qnt)
            price.text = item.context.getString(R.string.template_price, product.price)
            total.text = item.context.getString(R.string.template_price, product.price*product.qnt)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, vType: Int): ProductItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.basket_item, parent, false)
        if (updateTotal == null) {
            view.findViewById<ImageButton>(R.id.delete_btn).visibility = View.INVISIBLE
        }
        return ProductItem(view)
    }

    override fun onBindViewHolder(holder: ProductItem, pos: Int) {
        holder.bindData(productItems[pos])

        if (updateTotal == null) return
        holder.delete.setOnClickListener {
            val itemPosition = holder.adapterPosition
            if (productItems[itemPosition].qnt <= 1) {
                // remove your item from data base
                productItems.removeAt(itemPosition)
                notifyItemRemoved(itemPosition)
            } else {
                productItems[itemPosition].qnt--
                notifyItemChanged(itemPosition)
            }
            updateTotal!!()
        }
    }

    override fun getItemCount(): Int {
        return productItems.size
    }
}
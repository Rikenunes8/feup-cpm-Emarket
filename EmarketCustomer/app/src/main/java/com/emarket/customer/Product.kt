package com.emarket.customer

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

data class Product (
    var imgRes: Int,
    var name: String,
    var price: Double,
    var qnt: Int,
    var total: Double
)

class BasketAdapter(private val ctx: Context, val items: ArrayList<Product>): ArrayAdapter<Product>(ctx, R.layout.basket_item, items) {
    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        val row = convertView ?: (ctx as Activity).layoutInflater.inflate(R.layout.basket_item, parent, false)
        with(items[pos]) {
            row.findViewById<ImageView>(R.id.item_icon).setImageDrawable(ContextCompat.getDrawable(context, imgRes))
            row.findViewById<TextView>(R.id.item_name).text = name
            row.findViewById<TextView>(R.id.item_price).text = "Price: $price€"
            row.findViewById<TextView>(R.id.item_qnt).text = "$qnt x"
            row.findViewById<TextView>(R.id.item_total_price).text = "$total€"
        }
        return row
    }
}
package com.emarket.customer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.time.LocalDate

val product1 = Product(R.drawable.icon, "Apple", 3.0, 1, 3.0)
val product2 = Product(R.drawable.icon, "Banana", 4.0, 3, 12.0)


class ShoppingActivity : AppCompatActivity() {
    private val basketListView by lazy { findViewById<ListView>(R.id.basket_listview) }
    private val items : ArrayList<Product> = arrayListOf(product1, product2, product1, product1, product1, product1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onStart() {
        super.onStart()
        basketListView.run {
            adapter = BasketAdapter(this@ShoppingActivity, items)
        }
    }
}

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


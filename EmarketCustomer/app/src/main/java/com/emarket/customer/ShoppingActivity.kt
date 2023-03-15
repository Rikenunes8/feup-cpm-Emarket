package com.emarket.customer

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable.Orientation
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.io.Console

val product1 = Product(R.drawable.icon, "Apple", 3.0, 1, 3.0)
val product2 = Product(R.drawable.icon, "Banana", 4.0, 3, 12.0)


class ShoppingActivity : AppCompatActivity() {

    private val items : ArrayList<Product> = arrayListOf(product1, product2, product1, product1, product1, product1, product1, product1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping)

        val totalView by lazy {findViewById<TextView>(R.id.total_price)}
        var sum = 0.0
        items.forEach{sum += it.price}
        totalView.text = "$sum€"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onStart() {
        super.onStart()
        var layout = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            R.layout.basket_list_item
        } else {
            R.layout.basket_gallery_item
        }
        val basketView by lazy { findViewById<RecyclerView>(R.id.basket_listview) }
        basketView.adapter = (BasketAdapter(this@ShoppingActivity, items, layout))

    }
}


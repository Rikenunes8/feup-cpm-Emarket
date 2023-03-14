package com.emarket.customer

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

val product1 = Product(R.drawable.icon, "Apple", 3.0, 1, 3.0)
val product2 = Product(R.drawable.icon, "Banana", 4.0, 3, 12.0)


class ShoppingActivity : AppCompatActivity() {
    private val basketListView by lazy { findViewById<ListView>(R.id.basket_listview) }
    private val items : ArrayList<Product> = arrayListOf(product1, product2, product1, product1, product1, product1, product1, product1)

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


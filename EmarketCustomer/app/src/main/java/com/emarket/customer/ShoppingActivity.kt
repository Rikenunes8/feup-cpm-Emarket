package com.emarket.customer

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.model.Product
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

val product1 = Product(R.drawable.icon, "Apple", 3.0, 1, 3.0)
val product2 = Product(R.drawable.icon, "Banana", 4.0, 3, 12.0)
private val productItems : MutableList<Product> = mutableListOf(product1, product2, product1, product2, product1, product2, product1, product2, product1, product2)

class ShoppingActivity : AppCompatActivity() {
    var adapter = BasketAdapter(productItems)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping)

        val rv = findViewById<RecyclerView>(R.id.rv_basket)

        val orientation = if (Configuration.ORIENTATION_PORTRAIT == resources.configuration.orientation) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL
        rv.layoutManager = LinearLayoutManager(this, orientation, false)
        rv.adapter = adapter

        val totalView by lazy {findViewById<TextView>(R.id.total_price)}
        var sum = 0.0
        productItems.forEach{sum += it.price}
        totalView.text = "$sum€"
BasketAdapter(productItems)
        val addBtn by lazy {findViewById<FloatingActionButton>(R.id.add_item)}
        addBtn.setOnClickListener{
            productItems.add(Product(R.drawable.icon, "new", 40.0, 1, 40.0))
            adapter.notifyItemInserted(productItems.size-1)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

}

class BasketAdapter(private val productItems : MutableList<Product>) : RecyclerView.Adapter<BasketAdapter.ProductItem>() {


    class ProductItem(val item: View) :  RecyclerView.ViewHolder(item) {
        private val icon: ImageView = item.findViewById(R.id.item_icon)
        private val name: TextView = item.findViewById(R.id.item_name)
        private val price: TextView = item.findViewById(R.id.item_price)
        private val qnt: TextView = item.findViewById(R.id.item_qnt)
        private val total: TextView = item.findViewById(R.id.item_total_price)
        internal val delete: ImageButton = item.findViewById(R.id.delete_btn)


        fun bindData(product: Product) {
            icon.setImageResource(product.imgRes)
            name.text = product.name
            price.text = "Price: ${product.price} €"
            qnt.text = "${product.qnt} x"
            total.text = "${product.total} €"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, vType: Int): ProductItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.basket_item, parent, false)
        return ProductItem(view)
    }

    override fun onBindViewHolder(holder: ProductItem, pos: Int) {
        holder.bindData(productItems[pos])

        holder.delete.setOnClickListener(View.OnClickListener {
            // remove your item from data base
            productItems.removeAt(pos) // remove the item from list
            notifyItemRemoved(pos) // notify the adapter about the removed item
        })
    }

    override fun getItemCount(): Int {
        return productItems.size
    }

    fun getItem(pos: Int): Product {
        return productItems[pos]
    }

    fun addProductItem(product : Product) {

    }
}


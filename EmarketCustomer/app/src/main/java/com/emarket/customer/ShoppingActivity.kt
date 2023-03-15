package com.emarket.customer

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.model.Product

val product1 = Product(R.drawable.icon, "Apple", 3.0, 1, 3.0)
val product2 = Product(R.drawable.icon, "Banana", 4.0, 3, 12.0)


class ShoppingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping)

        val rv = findViewById<RecyclerView>(R.id.rv_basket)

        val orientation = if (Configuration.ORIENTATION_PORTRAIT == resources.configuration.orientation) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL
        rv.layoutManager = LinearLayoutManager(this, orientation, false)
        rv.adapter = BasketAdapter()
    }


}

class BasketAdapter : RecyclerView.Adapter<BasketAdapter.ProductItem>() {

    private val productItems : Array<Product> = arrayOf(product1, product2, product1, product2, product1, product2, product1, product2, product1, product2)


    class ProductItem(val item: View) :  RecyclerView.ViewHolder(item) {
        private val icon: ImageView = item.findViewById(R.id.item_icon)
        private val name: TextView = item.findViewById(R.id.item_name)
        private val price: TextView = item.findViewById(R.id.item_price)
        private val qnt: TextView = item.findViewById(R.id.item_qnt)
        private val total: TextView = item.findViewById(R.id.item_total_price)


        fun bindData(product: Product) {
            icon.setImageResource(product.imgRes)
            name.text = product.name
            println(product.price)
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


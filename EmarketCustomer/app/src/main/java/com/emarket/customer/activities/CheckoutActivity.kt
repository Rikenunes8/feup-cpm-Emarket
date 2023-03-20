package com.emarket.customer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.R
import com.emarket.customer.models.Product

class CheckoutActivity : AppCompatActivity() {
    private var vouchers = mutableListOf(15, 15, 15, 15, 15)
    private val productItems : MutableList<Product> = mutableListOf(product1, product2, product1, product2, product1, product2, product1, product2, product1, product2)

    var voucherAdapter = VoucherAdapter(vouchers)
    var basketAdapter = CheckoutBasketAdapter(productItems)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        //TODO: get products from last activity
        //productItems = intent.getParcelableArrayExtra("PRODUCTS") as MutableList<Product>

        val voucherView = findViewById<RecyclerView>(R.id.rv_voucher)
        voucherView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        voucherView.adapter = voucherAdapter
        val basketView = findViewById<RecyclerView>(R.id.rv_basket)
        basketView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        basketView.adapter = basketAdapter

    }
}

class CheckoutBasketAdapter(private val productItems : MutableList<Product>) : RecyclerView.Adapter<CheckoutBasketAdapter.Item>() {


    class Item(val item: View) :  RecyclerView.ViewHolder(item) {
        private val icon: ImageView = item.findViewById(R.id.item_icon)
        private val name: TextView = item.findViewById(R.id.item_name)
        private val price: TextView = item.findViewById(R.id.item_price)
        private val qnt: TextView = item.findViewById(R.id.item_qnt)
        private val total: TextView = item.findViewById(R.id.item_total_price)

        fun bindData(product: Product) {
            icon.setImageResource(product.imgRes)
            name.text = product.name
            price.text = "Price: ${product.price} €"
            qnt.text = "${product.qnt} x"
            total.text = "${product.total} €"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, vType: Int): Item {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.basket_item, parent, false)
        return Item(view)
    }

    override fun onBindViewHolder(holder: Item, pos: Int) {
        holder.bindData(productItems[pos])
    }

    override fun getItemCount(): Int {
        return productItems.size
    }

}

class VoucherAdapter(private val vouchers : MutableList<Int>) : RecyclerView.Adapter<VoucherAdapter.ProductItem>() {


    class ProductItem(val item: View) :  RecyclerView.ViewHolder(item) {
        private val discount: TextView = item.findViewById(R.id.voucher_discount)

        fun bindData(discount_value: Int) {
            discount.text = "$discount_value %"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, vType: Int): ProductItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.voucher, parent, false)
        return ProductItem(view)
    }

    override fun onBindViewHolder(holder: ProductItem, pos: Int) {
        holder.bindData(vouchers[pos])
    }

    override fun getItemCount(): Int {
        return vouchers.size
    }

}
package com.emarket.customer.activities

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.R
import com.emarket.customer.models.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class CheckoutActivity : AppCompatActivity() {
    private var vouchers = mutableListOf(15, 15, 15, 15, 15)
    private lateinit var productItems : MutableList<Product>
    private val accAmount = 13.04

    private lateinit var voucherAdapter : VoucherAdapter
    private lateinit var basketAdapter: CheckoutBasketAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val sharedPreferences = getSharedPreferences("ShoppingItems", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("list", null)
        productItems = Gson().fromJson(json, object : TypeToken<MutableList<Product>>() {}.type)

        var voucherAdapter = VoucherAdapter(vouchers)
        var basketAdapter = CheckoutBasketAdapter(productItems)


        val voucherView = findViewById<RecyclerView>(R.id.rv_voucher)
        voucherView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        voucherView.adapter = voucherAdapter
        val basketView = findViewById<RecyclerView>(R.id.rv_basket)
        basketView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        basketView.adapter = basketAdapter

        val accAmountView = findViewById<TextView>(R.id.acc_amount)
        accAmountView.text = "$accAmount€"

        val totalView = findViewById<TextView>(R.id.total_price)
        val sum = productItems.fold(0.0) { total, product -> total + product.price }
        totalView.text = "$sum€"

        val discountCheck = findViewById<CheckBox>(R.id.discount)
        discountCheck.setOnCheckedChangeListener { _, isChecked ->
            val discountView = findViewById<TextView>(R.id.discount_price)
            totalView.paintFlags = if (isChecked) Paint.STRIKE_THRU_TEXT_FLAG else 0

            discountView.text = if (isChecked) "${sum - accAmount}€" else ""
        }

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.checkout_item, parent, false)
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
    private var checkedPosition = -1

    class ProductItem(private val item: View) : RecyclerView.ViewHolder(item) {
        private val discount: TextView = item.findViewById(R.id.voucher_discount)

        fun bindData(discount_value: Int, position: Int, checkedPosition: Int, listener: OnItemClickListener) {
            discount.text = "$discount_value%"

            val cardView: CardView = item.findViewById(R.id.voucher_card)

            if (position == checkedPosition) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(item.context, R.color.light_green))
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(item.context, R.color.white))
            }

            item.setOnClickListener { view ->
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductItem {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.voucher, parent, false)
        return ProductItem(view)
    }

    override fun onBindViewHolder(holder: ProductItem, position: Int) {
        holder.bindData(vouchers[position], position, checkedPosition, object : OnItemClickListener {
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
}

package com.emarket.customer.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.R
import com.emarket.customer.Utils.showToast
import com.emarket.customer.models.Product
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

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
        val sum = productItems.fold(0.0) { total, product -> total + product.price }
        totalView.text = "$sum€"

        val addBtn by lazy {findViewById<FloatingActionButton>(R.id.add_item)}
        addBtn.setOnClickListener{
            productItems.add(0, Product(R.drawable.icon, "new", 40.0, 1, 40.0))
            adapter.notifyItemInserted(0)
            rv.scrollToPosition(0)
        }

        addBtn.setOnLongClickListener {
            if (!requestCameraPermission())
                read.launch(IntentIntegrator(this).createScanIntent())
            return@setOnLongClickListener true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requestCameraPermission(): Boolean {
        val REQUEST_CAMERA_PERMISSION = 1
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permission == PackageManager.PERMISSION_GRANTED) return false
        val requests = arrayOf(Manifest.permission.CAMERA)
        ActivityCompat.requestPermissions(this, requests, REQUEST_CAMERA_PERMISSION)
        return true
    }

    private val read = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val intentResult : IntentResult? = IntentIntegrator.parseActivityResult(it.resultCode, it.data)
        if (intentResult != null) {
            if (intentResult.contents != null) {
                showToast(this, "Success")
                processQRCode(intentResult)
            } else {
                showToast(this, "Scan failed")
            }
        }
    }

    private fun processQRCode(result : IntentResult) {
        // TODO do something useful
        Log.d("Print", "QR Code Content: ${result.contents}")
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
            val itemPosition = holder.adapterPosition
            productItems.removeAt(itemPosition) // remove the item from list
            notifyItemRemoved(itemPosition)
        })
    }

    override fun getItemCount(): Int {
        return productItems.size
    }
}


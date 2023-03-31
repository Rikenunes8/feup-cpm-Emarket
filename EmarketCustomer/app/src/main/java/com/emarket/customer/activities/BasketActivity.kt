package com.emarket.customer.activities
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.Utils.showToast
import com.emarket.customer.controllers.ProductsListAdapter
import com.emarket.customer.models.Product
import com.emarket.customer.models.ProductDTO
import com.emarket.customer.services.CryptoService.Companion.constructRSAPubKey
import com.emarket.customer.services.CryptoService.Companion.verifySignature
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.util.*
import kotlin.concurrent.thread

val product1 = Product(R.drawable.icon, "1", "Apple", 3.25)
val product2 = Product(R.drawable.icon, "2", "Banana", 4.99)
private val productItems : MutableList<Product> = mutableListOf(product1, product2)

data class ProductSignature (
    val product : String,
    val signature : String
)

class BasketActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CAMERA_PERMISSION = 1
    }

    private var adapter = ProductsListAdapter(productItems) { updateTotal() }
    private val rv by lazy { findViewById<RecyclerView>(R.id.rv_basket) }
    private val addBtn by lazy {findViewById<FloatingActionButton>(R.id.add_item)}
    private val totalView by lazy {findViewById<TextView>(R.id.total_price)}
    private val checkoutBtn by lazy {findViewById<Button>(R.id.checkout_btn)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basket)

        val orientation = if (Configuration.ORIENTATION_PORTRAIT == resources.configuration.orientation) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL
        rv.layoutManager = LinearLayoutManager(this, orientation, false)
        rv.adapter = adapter

        updateTotal()

        addBtn.setOnClickListener {
            if (!requestCameraPermission())
                readQRCode.launch(IntentIntegrator(this).createScanIntent())
        }
        // TODO: Remove bypass to add a fake product
        addBtn.setOnLongClickListener {
            val newProduct = Product(R.drawable.icon, "0", "FAKE", 40.0)
            val oldProduct = productItems.find { it.uuid == newProduct.uuid }
            if (oldProduct != null) { oldProduct.qnt++;updateProduct(oldProduct) }
            else { addProduct(newProduct) }
            return@setOnLongClickListener true
        }

        checkoutBtn.setOnClickListener {
            val sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putString(Constants.BASKET_ITEMS, Gson().toJson(productItems))
                apply()
            }
            startActivity(Intent(this, CheckoutActivity::class.java))
        }
    }

    private fun addProduct(product: Product) {
        productItems.add(0, product)
        adapter.notifyItemInserted(0)
        rv.scrollToPosition(0)
        updateTotal()
    }

    private fun updateProduct(newProduct : Product) {
        productItems.forEachIndexed { index, product -> if (product.uuid == newProduct.uuid) adapter.notifyItemChanged(index)}
        updateTotal()
    }

    private fun updateTotal() {
        val sum = productItems.fold(0.0) { total, product -> total + product.price * product.qnt }
        totalView.text = getString(R.string.template_price, sum)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
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
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permission == PackageManager.PERMISSION_GRANTED) return false
        val requests = arrayOf(Manifest.permission.CAMERA)
        ActivityCompat.requestPermissions(this, requests, REQUEST_CAMERA_PERMISSION)
        return true
    }

    private val readQRCode = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val intentResult : IntentResult? = IntentIntegrator.parseActivityResult(it.resultCode, it.data)
        if (intentResult != null) {
            if (intentResult.contents != null) {
                processQRCode(intentResult)
            } else {
                showToast(this, "Scan failed")
            }
        }
    }

    private fun processQRCode(result : IntentResult) {
        try {
            val sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)
            val keyData = sharedPreferences.getString(Constants.SERVER_PUB_KEY, null)!!
            val productSign = Gson().fromJson(result.contents, ProductSignature::class.java)

            val productBytes = productSign.product.toByteArray(Charsets.UTF_8)
            val serverPubKey = constructRSAPubKey(keyData)
            val signature = Base64.getDecoder().decode(productSign.signature)
            val verified = verifySignature(productBytes, signature, serverPubKey)

            if (verified == null || !verified) {
                showToast(this, "Unreliable QR code")
                return
            }

            val newProductDTO = Gson().fromJson(productSign.product, ProductDTO::class.java)
            val oldProduct = productItems.find { it.uuid == newProductDTO.uuid }
            if (oldProduct != null) {
                oldProduct.qnt++
                updateProduct(oldProduct)
            } else {
                val newProduct = Product(R.drawable.icon, newProductDTO.uuid, newProductDTO.name, newProductDTO.price)
                addProduct(newProduct)
                thread(start=true) { dbLayer.addProduct(newProduct) }
            }
        } catch (e: java.lang.Exception) {
            Log.e("QRCode", e.toString())
            showToast(this, "Bad QR code format")
        }
    }
}

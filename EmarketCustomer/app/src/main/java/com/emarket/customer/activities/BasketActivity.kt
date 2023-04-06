package com.emarket.customer.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.Utils.fetchUserData
import com.emarket.customer.Utils.showToast
import com.emarket.customer.activities.profile.ProfileActivity
import com.emarket.customer.controllers.ProductsListAdapter
import com.emarket.customer.models.Product
import com.emarket.customer.models.ProductDTO
import com.emarket.customer.services.CryptoService.Companion.constructRSAPubKey
import com.emarket.customer.services.CryptoService.Companion.verifySignature
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.util.*
import kotlin.concurrent.thread

data class ProductSignature (
    val product : String,
    val signature : String
)

class BasketActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CAMERA_PERMISSION = 1
        const val MAXIMUM_NUMBER_OF_ITEMS = 10
    }

    private val rv by lazy { findViewById<RecyclerView>(R.id.rv_basket) }
    private val addBtn by lazy {findViewById<FloatingActionButton>(R.id.add_item)}
    private val totalView by lazy {findViewById<TextView>(R.id.total_price)}
    private val checkoutBtn by lazy {findViewById<Button>(R.id.checkout_btn)}

    private lateinit var adapter : ProductsListAdapter
    private lateinit var productItems : MutableList<Product>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("Create")
        setContentView(R.layout.activity_basket)
        productItems = if (savedInstanceState == null) mutableListOf() else {
            val productItemsJson = savedInstanceState.getString(Constants.BASKET_ITEMS)
            println("Restore in create")
            Gson().fromJson(productItemsJson, object : TypeToken<MutableList<Product>>() {}.type)
        }

        adapter = ProductsListAdapter(productItems) { enableAddProduct(); enableCheckout(); updateTotal() }
        rv.adapter = adapter

        enableAddProduct()
        enableCheckout()
        updateTotal()

        addBtn.setOnClickListener {
            if (!requestCameraPermission()) {
                readQRCode.launch(IntentIntegrator(this).createScanIntent())
            }
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

    override fun onStart() {
        super.onStart()
        println("Start")
    }

    override fun onPause() {
        super.onPause()
        println("Pause")
    }

    override fun onStop() {
        super.onStop()
        println("Stop")
    }

    override fun onResume() {
        super.onResume()
        println("Resume")
    }

    override fun onRestart() {
        super.onRestart()
        println("Restart")
        fetchUserData(this, complete = false)
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(Constants.BASKET_ITEMS, Gson().toJson(productItems))
        println("saving")
        super.onSaveInstanceState(outState)
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
            println(newProductDTO)
            val oldProduct = productItems.find { it.uuid == newProductDTO.uuid }
            if (oldProduct != null) {
                oldProduct.quantity++
                updateProduct(oldProduct)
            } else {
                val newProduct = Product(newProductDTO.uuid, newProductDTO.name, newProductDTO.price, newProductDTO.url)
                addProduct(newProduct)
                thread(start=true) { dbLayer.addProduct(newProduct) }
                enableAddProduct()
                enableCheckout()
            }
        } catch (e: java.lang.Exception) {
            Log.e("QRCode", e.toString())
            showToast(this, "Bad QR code format")
        }
    }

    private fun enableCheckout() {
        checkoutBtn.isEnabled = productItems.isNotEmpty()
        checkoutBtn.alpha = if (checkoutBtn.isEnabled) 1f else .5f
    }
    private fun enableAddProduct() {
        val numberOfItems = productItems.fold(0) { count, product -> count + product.quantity }
        addBtn.isEnabled = numberOfItems < MAXIMUM_NUMBER_OF_ITEMS
        addBtn.alpha = if (addBtn.isEnabled) 1f else .5f
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
        val sum = productItems.fold(0.0) { total, product -> total + product.price * product.quantity }
        totalView.text = getString(R.string.template_price, sum)
    }
}

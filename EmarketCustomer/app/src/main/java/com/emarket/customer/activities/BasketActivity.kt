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
import com.emarket.customer.Utils.showToast
import com.emarket.customer.activities.profile.ProfileActivity
import com.emarket.customer.controllers.Fetcher.Companion.fetchUserData
import com.emarket.customer.controllers.adapters.ProductsListAdapter
import com.emarket.customer.controllers.ShakeDetector
import com.emarket.customer.models.Product
import com.emarket.customer.models.ProductDTO
import com.emarket.customer.services.CryptoService
import com.emarket.customer.services.NetworkService
import com.emarket.customer.services.RequestType
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.concurrent.thread

class BasketActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CAMERA_PERMISSION = 1
        const val MAXIMUM_NUMBER_OF_ITEMS = 10
    }

    private val shakeDetector by lazy { ShakeDetector(this, ::startScanUponPermission) }

    private val rv by lazy { findViewById<RecyclerView>(R.id.rv_basket) }
    private val addBtn by lazy {findViewById<FloatingActionButton>(R.id.add_item)}
    private val totalView by lazy {findViewById<TextView>(R.id.total_price)}
    private val checkoutBtn by lazy {findViewById<Button>(R.id.checkout_btn)}

    private lateinit var adapter : ProductsListAdapter
    private lateinit var productItems : MutableList<Product>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basket)
        productItems = if (savedInstanceState == null) mutableListOf() else {
            val productItemsJson = savedInstanceState.getString(Constants.BASKET_ITEMS)
            Gson().fromJson(productItemsJson, object : TypeToken<MutableList<Product>>() {}.type)
        }

        adapter = ProductsListAdapter(productItems) { enableAddProduct(); enableCheckout(); updateTotal() }
        rv.adapter = adapter

        enableAddProduct()
        enableCheckout()
        updateTotal()

        addBtn.setOnClickListener { startScanUponPermission() }

        checkoutBtn.setOnClickListener {
            val sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putString(Constants.BASKET_ITEMS, Gson().toJson(productItems))
                apply()
            }
            startActivity(Intent(this, CheckoutActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        shakeDetector.startSensing()
    }

    override fun onPause() {
        super.onPause()
        shakeDetector.stopSensing()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu?.removeItem(R.id.action_settings); // remove the settings option
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_profile -> {
                fetchUserData(this, complete = false)
                startActivity(Intent(this, ProfileActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(Constants.BASKET_ITEMS, Gson().toJson(productItems))
        super.onSaveInstanceState(outState)
    }

    private fun startScanUponPermission() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startScan()
        } else {
            val requests = arrayOf(Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(this, requests, REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showToast(this, "Camera permission not granted!")
                } else {
                    startScan()
                }
            }
        }
    }

    private fun startScan() {
        val intentIntegrator = IntentIntegrator(this).apply {
            setPrompt(getString(R.string.scanner_prompt))
            setOrientationLocked(false)
        }
        readQRCode.launch(intentIntegrator.createScanIntent())
    }

    private val readQRCode = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val intentResult : IntentResult? = IntentIntegrator.parseActivityResult(it.resultCode, it.data)
        if (intentResult != null && intentResult.contents != null) {
            processQRCode(intentResult)
        }
    }

    private fun processQRCode(result : IntentResult) {
        try {
            val cert = CryptoService.loadCertificate(Constants.SERVER_CERTIFICATE)
            val serverPubKey = cert?.publicKey
            val qrContent = result.contents.toByteArray(StandardCharsets.ISO_8859_1)
            val content = CryptoService.decryptFromServerContent(qrContent, serverPubKey)

            if (content == null) {
                showToast(this, getString(R.string.error_invalid_qrcode))
                return
            }

            val tag = ByteBuffer.wrap(content)
            val tId = tag.int
            if (tId != Constants.TAG_ID) {
                showToast(this, getString(R.string.error_invalid_qrcode))
                return
            }

            val id = UUID(tag.long, tag.long).toString()
            val euros = tag.int
            val cents = tag.int
            val price = euros + cents / 100.0
            val bName = ByteArray(tag.get().toInt())
            tag[bName]
            val name = String(bName, StandardCharsets.ISO_8859_1)

            processProduct(id, name, price)
        } catch (e: java.lang.Exception) {
            Log.e("QRCode", e.toString())
            showToast(this, "Bad QR code format")
        }
    }

    private fun processProduct(id: String, name: String, price: Double) {
        thread(start = true) {
            val response = NetworkService.makeRequest(
                RequestType.GET,
                Constants.SERVER_URL + Constants.PRODUCT_ENDPOINT + "/$id"
            )

            val jsonResponse = JSONObject(response)
            val productDTO: ProductDTO
            if (jsonResponse.has("error")) {
                val auxProduct = dbLayer.getProduct(id)
                productDTO = auxProduct ?: ProductDTO(id, name, price, null)
                Log.e("QRCode", jsonResponse.getString("error"))
            } else {
                val product = jsonResponse.get("product").toString()
                productDTO = Gson().fromJson(product, ProductDTO::class.java)
            }

            val oldProduct = productItems.find { it.uuid == productDTO.uuid }
            if (oldProduct != null) {
                if (productDTO.url != null && oldProduct.url == null) {
                    oldProduct.url = productDTO.url
                }
                oldProduct.quantity++
                runOnUiThread { updateProduct(oldProduct) }
            } else {
                val newProduct = Product(productDTO.uuid, productDTO.name, productDTO.price, productDTO.url)
                dbLayer.addProduct(newProduct)
                runOnUiThread { addProduct(newProduct) }
            }

            runOnUiThread {
                enableAddProduct()
                enableCheckout()
            }
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

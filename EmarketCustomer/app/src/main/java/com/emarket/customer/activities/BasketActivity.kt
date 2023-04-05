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
import com.emarket.customer.Utils
import com.emarket.customer.Utils.fetchDataFromDatabase
import com.emarket.customer.Utils.showToast
import com.emarket.customer.activities.authentication.UserResponse
import com.emarket.customer.activities.profile.ProfileActivity
import com.emarket.customer.controllers.ProductsListAdapter
import com.emarket.customer.models.Product
import com.emarket.customer.models.ProductDTO
import com.emarket.customer.models.UserViewModel
import com.emarket.customer.models.updateUserData
import com.emarket.customer.services.CryptoService.Companion.constructRSAPubKey
import com.emarket.customer.services.CryptoService.Companion.verifySignature
import com.emarket.customer.services.NetworkService
import com.emarket.customer.services.RequestType
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.net.URLEncoder
import java.util.*
import kotlin.concurrent.thread

/*
val product1 = Product("1", "Banana", 1.15)
val product2 = Product("2", "Apple", 2.50)
val product3 = Product("3", "Pear", 1.75)
val product4 = Product("4", "Microwave", 49.99)
private var productItems : MutableList<Product> = mutableListOf(product1, product2, product3, product4)*/

private var productItems = mutableListOf<Product>()

data class ProductSignature (
    val product : String,
    val signature : String
)

class BasketActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CAMERA_PERMISSION = 1
        const val MAXIMUM_NUMBER_OF_ITEMS = 10
    }

    private lateinit var adapter : ProductsListAdapter
    private val rv by lazy { findViewById<RecyclerView>(R.id.rv_basket) }
    private val addBtn by lazy {findViewById<FloatingActionButton>(R.id.add_item)}
    private val totalView by lazy {findViewById<TextView>(R.id.total_price)}
    private val checkoutBtn by lazy {findViewById<Button>(R.id.checkout_btn)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basket)
        if (savedInstanceState != null) {
            val productItemsJson = savedInstanceState.getString(Constants.BASKET_ITEMS)
            productItems = Gson().fromJson(productItemsJson, object : TypeToken<MutableList<Product>>() {}.type)
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
        // TODO: Remove bypass to add a fake product
        addBtn.setOnLongClickListener {
            val newProduct = Product("0", "FAKE", 40.0)
            val oldProduct = productItems.find { it.uuid == newProduct.uuid }
            if (oldProduct != null) { oldProduct.quantity++;updateProduct(oldProduct) }
            else { addProduct(newProduct) }
            enableAddProduct()
            enableCheckout()
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

    override fun onResume() {
        super.onResume()
        fetchUserData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(Constants.BASKET_ITEMS, Gson().toJson(productItems))
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

    /**
     * Updates the user data in the database and the shared preferences
     */
    private fun fetchUserData() {
        thread(start = true) {
            val user = UserViewModel(this.application).user!!
            val date = dbLayer.getLastTransaction()?.date

            val url = Constants.SERVER_URL + Constants.USER_ENDPOINT +
                    "?user=${URLEncoder.encode(user.userId)}" + "&date=${URLEncoder.encode(date)}"
            val response = NetworkService.makeRequest(RequestType.GET, url, null)
            val userData = Gson().fromJson(response, UserResponse::class.java)
            if (userData.error != null) {
                Log.e("LoginActivity", "Error:  ${userData.error}")
                runOnUiThread { showToast(this, getString(R.string.error_fetching_user_information)) }
            } else {
                updateUserData(userData)
                fetchDataFromDatabase()
            }
        }
    }

}

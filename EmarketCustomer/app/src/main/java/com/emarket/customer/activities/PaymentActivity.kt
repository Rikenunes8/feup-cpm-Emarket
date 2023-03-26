package com.emarket.customer.activities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.Utils.getAttributeColor
import com.emarket.customer.Utils.showToast
import com.emarket.customer.models.Transaction
import com.emarket.customer.models.User
import com.emarket.customer.models.UserViewModel
import com.emarket.customer.services.CryptoService.Companion.getPrivKey
import com.emarket.customer.services.CryptoService.Companion.signContent
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.concurrent.thread

data class Payment(
    val userUUID: String,
    val transaction: Transaction
)

class PaymentActivity : AppCompatActivity() {
    private val qrCodeImageview by lazy { findViewById<ImageView>(R.id.payment_qrcode_iv) }
    private val foregroundColor by lazy { getColor(getAttributeColor(this, com.google.android.material.R.attr.colorOnSecondary)) }
    private val backgroundColor by lazy { getColor(getAttributeColor(this, com.google.android.material.R.attr.colorSecondaryVariant)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val transactionJSON = intent.getStringExtra("Transaction")!!
        val transaction = Gson().fromJson(transactionJSON, Transaction::class.java)

        // TODO remove this when the properties of transaction were not hardcoded ---
        Log.d("Transaction", transaction.voucher.toString())
        Log.d("Transaction", transaction.discounted.toString())
        Log.d("Transaction", transaction.products.size.toString())
        // TODO ---------------------------------------------------------------------

        val storedUser = UserViewModel(this.application).user
        val userUUID = storedUser!!.userId
        val qrContent = genQRCode(userUUID, transaction)
        thread(start = true) {
            try {
                val bitmap = encodeAsBitmap(qrContent, foregroundColor, backgroundColor)
                runOnUiThread { qrCodeImageview.setImageBitmap(bitmap) }
            } catch (e: Exception) {
                showToast(this, getString(R.string.error_qrcode_generation))
                finish()
            }
        }
    }

    private fun genQRCode(userUUID: String, transaction: Transaction) : String {
        val dataJSON = Gson().toJson(Payment(userUUID, transaction))
        val dataByteArray = dataJSON.toByteArray()
        val signature = signContent(dataByteArray, getPrivKey())!!
        val signatureEncoded = Base64.getEncoder().encodeToString(signature)
        val len = 4 + 4 + signatureEncoded.length + dataJSON.length
        val tag = ByteBuffer.allocate(len).apply {  // building an array of bytes (binary)
            putInt(signatureEncoded.length)
            put(signatureEncoded.toByteArray(StandardCharsets.ISO_8859_1))
            putInt(dataJSON.length)
            put(dataJSON.toByteArray(StandardCharsets.ISO_8859_1))
        }

        return String(tag.array(), StandardCharsets.ISO_8859_1)
    }

    private fun encodeAsBitmap(str: String, foregroundColor : Int, backgroundColor : Int): Bitmap? {
        val DIMENSION = 1000

        val hints = Hashtable<EncodeHintType, String>().also {
            it[EncodeHintType.CHARACTER_SET] = "ISO-8859-1"
        }
        val result = try {
            MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, DIMENSION, DIMENSION, hints)
        } catch (iae: IllegalArgumentException) {
            return null
        }
        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (result.get(x, y)) foregroundColor else backgroundColor
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
    }
}
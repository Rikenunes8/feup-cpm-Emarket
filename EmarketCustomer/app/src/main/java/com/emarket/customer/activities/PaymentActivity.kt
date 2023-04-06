package com.emarket.customer.activities

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import com.emarket.customer.R
import com.emarket.customer.Utils
import com.emarket.customer.Utils.showToast
import com.emarket.customer.models.Basket
import com.emarket.customer.models.UserViewModel
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.util.*
import kotlin.concurrent.thread

data class Payment(
    val userUUID: String,
    val basket: Basket
)

class PaymentActivity : AppCompatActivity() {
    private val qrCodeImageview by lazy { findViewById<ImageView>(R.id.payment_qrcode_iv) }
    private val finishPaymentBtn by lazy { findViewById<ImageButton>(R.id.finish_payment_btn) }

    private val foregroundColor by lazy { getColor(R.color.dark_gray) }
    private val backgroundColor by lazy { getColor(R.color.light_gray) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val basketJSON = intent.getStringExtra("Basket")!!
        val basket = Gson().fromJson(basketJSON, Basket::class.java)

        val storedUser = UserViewModel(this.application).user
        val userUUID = storedUser!!.userId
        val paymentJson = Gson().toJson(Payment(userUUID, basket))
        val qrContent = Utils.genQRCode(paymentJson)

        finishPaymentBtn.setOnClickListener {
            startActivity(Intent(this, BasketActivity::class.java), null)
            finish()
        }

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
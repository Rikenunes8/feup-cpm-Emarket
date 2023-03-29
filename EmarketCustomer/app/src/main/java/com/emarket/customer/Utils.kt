package com.emarket.customer

import android.content.Context
import android.util.TypedValue
import android.widget.Toast
import com.emarket.customer.activities.Data
import com.emarket.customer.activities.Payment
import com.emarket.customer.models.Transaction
import com.emarket.customer.services.CryptoService
import com.google.gson.Gson
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*


object Utils {

    /**
     * Hash the password using SHA-256
     * @param password the password to hash
     * @return the hashed password
     */
    fun hashPassword(password: String): String {
        val bytes = password.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun showToast(ctx: Context, message : String?) {
        Toast.makeText(ctx, message, Toast.LENGTH_LONG).show()
    }

    fun getAttributeColor(context: Context, attributeId : Int) : Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attributeId, typedValue, true)
        return typedValue.resourceId
    }

    /**
     * Generate the QR code content signed with the user's private key
     * @param jsonData the data to sign and put in qrcode
     */
    fun genQRCode(jsonData: String) : String {
        val dataByteArray = jsonData.toByteArray()
        val signature = CryptoService.signContent(dataByteArray, CryptoService.getPrivKey())!!
        val signatureEncoded = Base64.getEncoder().encodeToString(signature)
        val data = Gson().toJson(Data(signatureEncoded, jsonData))
        println(data.toByteArray().decodeToString())

        return String(data.toByteArray(), StandardCharsets.ISO_8859_1)
    }
}
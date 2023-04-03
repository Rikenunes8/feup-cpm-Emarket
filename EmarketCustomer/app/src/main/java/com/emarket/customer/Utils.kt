package com.emarket.customer

import android.content.Context
import android.util.TypedValue
import android.widget.Toast
import com.emarket.customer.activities.Data
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
        val signatureEncoded = signContent(jsonData)
        val data = Gson().toJson(Data(signatureEncoded, jsonData))

        return String(data.toByteArray(), StandardCharsets.ISO_8859_1)
    }

    /**
     * Signs content with the user's private key
     */
    fun signContent(content: String) : String {
        val dataByteArray = content.toByteArray()
        val signature = CryptoService.signContent(dataByteArray, CryptoService.getPrivKey())!!
        return Base64.getEncoder().encodeToString(signature)
    }
}
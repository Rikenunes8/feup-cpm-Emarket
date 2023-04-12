package com.emarket.customer

import android.content.Context
import android.util.TypedValue
import android.widget.Toast
import com.emarket.customer.services.CryptoService
import com.google.gson.Gson
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class DataSigned(
    val signature: String,
    val data: String
)

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
        val signatureEncoded = getSignature(jsonData)
        val data = Gson().toJson(DataSigned(signatureEncoded, jsonData))

        return String(data.toByteArray(), StandardCharsets.ISO_8859_1)
    }

    /**
     * Get the signature of the data
     * @param jsonData the data to sign
     * @return the signature of the data
     */
    fun getSignature(jsonData: String): String {
        val dataByteArray = jsonData.toByteArray()
        val signature = CryptoService.signContent(dataByteArray, CryptoService.getPrivKey())!!
        return Base64.getEncoder().encodeToString(signature)
    }

    fun formatDate(date: String) : String {
        val timestamp = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm:ss"))
        return timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"))
    }
}

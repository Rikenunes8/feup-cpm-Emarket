package com.emarket.customer

import android.content.Context
import android.nfc.NfcAdapter
import android.util.TypedValue
import android.widget.Toast
import com.emarket.customer.services.CryptoService
import java.security.MessageDigest
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
     * Get the signature of the data
     * @param data the data to sign
     * @return the signature of the data encoded in base64
     */
    fun getSignature(data: String): String {
        val dataByteArray = data.toByteArray()
        val signature = CryptoService.signContent(dataByteArray, CryptoService.getPrivKey())!!
        return Base64.getEncoder().encodeToString(signature)
    }

    fun formatDate(date: String) : String {
        val timestamp = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm:ss"))
        return timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"))
    }

    fun hexStringToByteArray(s: String): ByteArray {
        val data = ByteArray(s.length / 2)
        var k = 0
        while (k < s.length) {
            data[k/2] = ((Character.digit(s[k], 16) shl 4) + Character.digit(s[k+1], 16)).toByte()
            k += 2
        }
        return data
    }

    fun hasNfc(ctx: Context) : Boolean? {
        val adapter = NfcAdapter.getDefaultAdapter(ctx)
        return if (adapter != null && adapter.isEnabled) true
        else if (adapter != null && !adapter.isEnabled) false
        else null
    }
}

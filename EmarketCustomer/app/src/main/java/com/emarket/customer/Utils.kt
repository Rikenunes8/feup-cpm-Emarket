package com.emarket.customer

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey


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
}
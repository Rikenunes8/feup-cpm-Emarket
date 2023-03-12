package com.emarket.customer

import android.util.Log
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey


object Utils {

    /**
     * Get the public key modulus and exponent
     */
    fun getPubKey(): PublicKey? {
        try {
            val entry = KeyStore.getInstance(Constants.ANDROID_KEYSTORE).run {
                load(null)
                getEntry(Constants.STORE_KEY, null)
            }
            return (entry as KeyStore.PrivateKeyEntry).certificate.publicKey
        } catch (ex: Exception) {
            Log.e("getPubKey", ex.message ?: "")
        }
        return null
    }

    /**
     * Get the private exponent of the private key
     */
    public fun getPrivKey(): PrivateKey? {
        var priv: PrivateKey? = null
        try {
            val entry = KeyStore.getInstance(Constants.ANDROID_KEYSTORE).run {
                load(null)
                getEntry(Constants.STORE_KEY, null)
            }
            priv = (entry as KeyStore.PrivateKeyEntry).privateKey
        }
        catch (ex: Exception) {
            Log.e("getPrivExp", ex.message ?: "")
        }
        return priv
    }
}
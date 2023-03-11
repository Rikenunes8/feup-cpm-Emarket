package com.emarket.customer

import android.util.Log
import java.security.KeyStore
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

data class PubKey(var modulus: ByteArray, var exponent: ByteArray)

object Utils {

    /**
     * Get the public key modulus and exponent
     */
    fun getPubKey(): PubKey {
        val pKey = PubKey(ByteArray(0), ByteArray(0))
        try {
            val entry = KeyStore.getInstance(Constants.ANDROID_KEYSTORE).run {
                load(null)
                getEntry(Constants.keyname, null)
            }
            val pub = (entry as KeyStore.PrivateKeyEntry).certificate.publicKey
            pKey.modulus = (pub as RSAPublicKey).modulus.toByteArray()
            pKey.exponent = (pub as RSAPublicKey).publicExponent.toByteArray()
        }
        catch (ex: Exception) {
            Log.e("getPubKey", ex.message ?: "")
        }
        return pKey
    }

    /**
     * Get the private exponent of the private key
     */
    fun getPrivExp(): ByteArray {
        var exp = ByteArray(0)
        try {
            val entry = KeyStore.getInstance(Constants.ANDROID_KEYSTORE).run {
                load(null)
                getEntry(Constants.keyname, null)
            }
            val priv = (entry as KeyStore.PrivateKeyEntry).privateKey
            exp = (priv as RSAPrivateKey).privateExponent.toByteArray()
        }
        catch (ex: Exception) {
            Log.e("getPrivExp", ex.message ?: "")
        }
        return exp
    }
}
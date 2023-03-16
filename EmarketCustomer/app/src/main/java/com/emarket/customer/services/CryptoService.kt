package com.emarket.customer.services

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.util.Log
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.Utils
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*
import javax.security.auth.x500.X500Principal

class CryptoService {
    companion object {
        /**
         * Check if the key pair is already present in the Android Key Store
         */
        private fun keysPresent(): Boolean {
            val entry = KeyStore.getInstance(Constants.ANDROID_KEYSTORE).run {
                load(null)
                getEntry(Constants.STORE_KEY, null)
            }
            return (entry != null)
        }

        /**
         * Generate a new key pair and store it in the Android Key Store
         */
        fun generateAndStoreKeys(ctx: Context, successMsg: String): Boolean {
            try {
                if (!keysPresent()) {
                    val spec = KeyPairGeneratorSpec.Builder(ctx)
                        .setKeySize(Constants.KEY_SIZE)
                        .setAlias(Constants.STORE_KEY)
                        .setSubject(X500Principal("CN=" + Constants.STORE_KEY))
                        .setSerialNumber(BigInteger.valueOf(Constants.serialNr))
                        .setStartDate(GregorianCalendar().time)
                        .setEndDate(GregorianCalendar().apply { add(Calendar.YEAR, 10) }.time)
                        .build()
                    KeyPairGenerator.getInstance(Constants.KEY_ALGO, Constants.ANDROID_KEYSTORE).run {
                        initialize(spec)
                        generateKeyPair()
                    }
                } else {
                    Utils.showToast(ctx, successMsg)
                    Log.e("RegisterActivity", "Key pair already present")
                    return false
                }
            }
            catch (ex: Exception) {
                Utils.showToast(ctx, ex.message)
                return false
            }
            return true
        }

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
        fun getPrivKey(): PrivateKey? {
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
}
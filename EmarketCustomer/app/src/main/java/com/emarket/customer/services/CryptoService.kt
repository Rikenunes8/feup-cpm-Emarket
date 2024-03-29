package com.emarket.customer.services

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.util.Log
import com.emarket.customer.Constants
import com.emarket.customer.Utils
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal

class CryptoService {
    companion object {
        /**
         * Check if the key pair is already present in the Android Key Store
         */
        fun keysPresent(): Boolean {
            val entry = KeyStore.getInstance(Constants.ANDROID_KEYSTORE).run {
                load(null)
                getEntry(Constants.STORE_KEY, null)
            }
            return (entry != null)
        }

        /**
         * Generate a new key pair and store it in the Android Key Store
         */
        fun generateAndStoreKeys(ctx: Context, errorMsg: String): Boolean {
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
                    Utils.showToast(ctx, errorMsg)
                    Log.e("RegisterActivity", errorMsg)
                    return false
                }
            }
            catch (ex: Exception) {
                Utils.showToast(ctx, ex.message)
                return false
            }
            return true
        }

        fun encryptContent(content: ByteArray, key: PublicKey?) : ByteArray? {
            if (content.isEmpty()) return null
            if (key == null) return null

            try {
                val result = Cipher.getInstance(Constants.ENC_ALGO).run {
                    init(Cipher.ENCRYPT_MODE, key)
                    doFinal(content)
                }
                return result
            } catch (e: Exception) {
                e.message?.let { Log.e("ENCRYPT", it) }
            }
            return null
        }

        fun decryptContent(content: ByteArray, key: PrivateKey?) : String? {
            if (content.isEmpty()) return null
            if (key == null) return null
            try {
                val result = Cipher.getInstance(Constants.ENC_ALGO).run {
                    init(Cipher.DECRYPT_MODE, key)
                    doFinal(content)
                }
                return String(result, StandardCharsets.UTF_8)
            }
            catch (e: Exception) {
                e.message?.let { Log.e("DECRYPT", it) }
            }
            return null
        }

        fun decryptFromServerContent(content: ByteArray, key: PublicKey?) : ByteArray? {
            if (content.isEmpty()) return null
            if (key == null) return null
            try {
                return Cipher.getInstance(Constants.ENC_ALGO).run {
                    init(Cipher.DECRYPT_MODE, key)
                    doFinal(content)
                }
            }
            catch (e: Exception) {
                e.message?.let { Log.e("DECRYPT", it) }
            }
            return null
        }

        fun signContent(content : ByteArray, key: PrivateKey?) : ByteArray? {
            if (content.isEmpty()) return null
            if (key == null) return null
            try {
                val result = Signature.getInstance(Constants.SIGN_ALGO).run {
                    initSign(key)
                    update(content)
                    sign()
                }
                return result
            }
            catch  (e: Exception) {
                e.message?.let { Log.e("SIGN", it) }
            }
            return null
        }

        fun verifySignature(content: ByteArray, signature: ByteArray, key : PublicKey?) : Boolean? {
            if (content.isEmpty()) return null
            if (key == null) return null
            try {
                val verified = Signature.getInstance(Constants.SIGN_ALGO).run {
                    initVerify(key)
                    update(content)
                    verify(signature)
                }
                return verified
            }
            catch (e: Exception) {
                e.message?.let { Log.e("VERIFY", it) }
            }
            return null
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
                priv = null
            }
            return priv
        }

        fun publicKeyToPKCS1(pubKey : PublicKey) : String {
            val encPubKey = Base64.getEncoder().encodeToString(pubKey.encoded)
            return "-----BEGIN PUBLIC KEY-----\n$encPubKey\n-----END PUBLIC KEY-----\n"
        }

        fun storeCertificate(name: String, cert: String) {
            val certFactory = CertificateFactory.getInstance("X509")
            val certStream = certFactory.generateCertificate(cert.byteInputStream())
            KeyStore.getInstance(Constants.ANDROID_KEYSTORE).apply {
                load(null)
                setCertificateEntry(name, certStream)
            }
        }

        fun loadCertificate(name: String) : Certificate? {
            return KeyStore.getInstance(Constants.ANDROID_KEYSTORE).run {
                load(null)
                getCertificate(name)
            }
        }
    }
}

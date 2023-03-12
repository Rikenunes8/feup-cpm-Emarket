package com.emarket.customer

object Constants {
    const val KEY_SIZE = 512
    const val ANDROID_KEYSTORE = "AndroidKeyStore"
    const val KEY_ALGO = "RSA"
    const val SIGN_ALGO = "SHA256WithRSA"
    const val ENC_ALGO = "RSA/ECB/PKCS1Padding"
    const val keyname = "EmarketKey"
    const val serialNr = 1234567890L
    const val serverUrl = "http://192.168.1.64:5000/"
}

/*
 *    Constant strings
 */
object InStrings {
    const val haveKeys = "generated"
    const val notHaveKeys = "not generated"
    const val beginCert = "-----BEGIN CERTIFICATE-----\n"
    const val endCert = "-----END CERTIFICATE-----\n"
    const val showKeysFormat = "Modulus(%d):\n%s\nExponent: %s\nPrivate Exponent(%d):\n%s"
    const val contentFormat = "Content(%d):\n%s"
    const val encFormat = "Encrypted(%d):\n%s"
    const val decFormat = "Decrypted(%s):\n%s"
    const val signFormat = "Signature(%s):\n%s"
    const val certFormat = "(DER:%d):\n%s\n\nPEM(b64:%d):\n%s\n%s"
}

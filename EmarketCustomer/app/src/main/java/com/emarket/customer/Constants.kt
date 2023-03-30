package com.emarket.customer

object Constants {
    // Keys
    const val ENC_ALGO = "RSA/ECB/PKCS1Padding"
    const val SIGN_ALGO = "SHA256WithRSA"
    const val KEY_ALGO = "RSA"
    const val KEY_SIZE = 512
    const val STORE_KEY = "EmarketKey"
    const val ANDROID_KEYSTORE = "AndroidKeyStore"
    const val serialNr = 1234567890L

    // Shared preferences
    const val SHARED_PREFERENCES = "EmarketSharedPref"
    const val USER_KEY = "USER_KEY"
    const val SERVER_PUB_KEY = "SERVER_PUB_KEY"
    const val BASKET_ITEMS = "BASKET_ITEMS"

    // Server connection
    const val SERVER_URL = "http://192.168.1.207:5000/"
    const val REGISTER_ENDPOINT = "register"

}

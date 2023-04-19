package com.emarket.customer

object Constants {
    // Product Qr code Tag
    const val TAG_ID = 0x41636D65                  // equal to "Acme"

    // Keys
    const val ENC_ALGO = "RSA/ECB/PKCS1Padding"
    const val SIGN_ALGO = "SHA256WithRSA"
    const val KEY_ALGO = "RSA"
    const val KEY_SIZE = 512
    const val STORE_KEY = "EmarketKey"
    const val ANDROID_KEYSTORE = "AndroidKeyStore"
    const val SERVER_CERTIFICATE = "SERVER_CERTIFICATE"
    const val serialNr = 1234567890L

    // Shared preferences
    const val SHARED_PREFERENCES = "EmarketSharedPref"
    const val PREF_SEND_ENABLED = "SendEnabled"
    const val USER_KEY = "USER_KEY"
    const val BASKET_ITEMS = "BASKET_ITEMS"
    const val NOTIFICATIONS_ENABLED = "NOTIFICATIONS_ENABLED"
    const val IS_QRCODE = "IS_QRCODE"
    const val PAYMENT = "PAYMENT"

    // Server connection
    const val SERVER_URL = "http://192.168.1.9:5000/"
    const val REGISTER_ENDPOINT = "register"
    const val PRODUCT_ENDPOINT = "product"
    const val USER_ENDPOINT = "user"

    const val ACTION_CARD_DONE = "CMD_PROCESSING_DONE"
}

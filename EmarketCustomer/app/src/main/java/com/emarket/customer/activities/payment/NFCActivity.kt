package com.emarket.customer.activities.payment

import android.os.Bundle
import android.util.Log
import com.emarket.customer.R

class NFCActivity : PaymentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_payment_nfc)

        val paymentJson = getPaymentJson()
        Log.e("NFCActivity", paymentJson)
    }

}

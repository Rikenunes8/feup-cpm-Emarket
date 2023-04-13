package com.emarket.customer.activities.payment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.ImageButton
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.Utils
import com.emarket.customer.activities.BasketActivity
import com.emarket.customer.controllers.Fetcher

class PaymentNfcActivity : AppCompatActivity() {
    private val finishPaymentBtn by lazy { findViewById<ImageButton>(R.id.finish_payment_btn) }

    private val broadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            finishPaymentBtn.visibility = View.VISIBLE
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_nfc)

        finishPaymentBtn.setOnClickListener {
            Fetcher.fetchUserData(this, complete = false, force = true)
            startActivity(Intent(this, BasketActivity::class.java))
            finish()
        }

        val paymentJson = Utils.buildPayment(this.application, intent)
        val nfcContent = Utils.signDataJson(paymentJson)
        PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().putString(Constants.PAYMENT, nfcContent).apply()
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().putBoolean(Constants.PREF_SEND_ENABLED, true).apply()
        val intentFilter = IntentFilter(Constants.ACTION_CARD_DONE)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().putBoolean(Constants.PREF_SEND_ENABLED, false).apply()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReceiver)
    }
}
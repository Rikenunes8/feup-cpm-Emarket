package com.emarket.customer.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.Utils
import com.emarket.customer.models.Basket
import com.emarket.customer.models.UserViewModel
import com.google.gson.Gson

class PaymentNfcActivity : AppCompatActivity() {
    private val broadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            Toast.makeText(this@PaymentNfcActivity, "NFC link lost", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_nfc)

        val paymentJson = Utils.buildPayment(this.application, intent)
        val nfcContent = Utils.signDataJson(paymentJson)
        Log.d("PaymentNFC", nfcContent)
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
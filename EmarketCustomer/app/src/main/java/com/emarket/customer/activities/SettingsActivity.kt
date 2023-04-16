package com.emarket.customer.activities

import android.content.Context
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.Utils.hasNfc

class SettingsActivity : AppCompatActivity() {

    private val cardView by lazy { findViewById<CardView>(R.id.card_settings) }
    private val moreToggle by lazy { findViewById<ImageButton>(R.id.more_toggle) }
    private val checkoutRg by lazy { findViewById<RadioGroup>(R.id.checkout_rg) }
    private val notificationsSwitch by lazy { findViewById<Switch>(R.id.notifications) }
    private val nfcRadio by lazy { findViewById<RadioButton>(R.id.nfc_radio) }
    private val qrcodeRadio by lazy { findViewById<RadioButton>(R.id.qrcode_radio) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)

        moreToggle.setOnClickListener {
            TransitionManager.beginDelayedTransition(cardView, AutoTransition())
            if (checkoutRg.visibility == View.VISIBLE) {
                checkoutRg.visibility = View.GONE
                moreToggle.setImageResource(R.drawable.arrow_down)
            } else {
                checkoutRg.visibility = View.VISIBLE
                moreToggle.setImageResource(R.drawable.arrow_up)
            }
        }

        notificationsSwitch.isChecked = sharedPreferences.getBoolean(Constants.NOTIFICATIONS_ENABLED, true)
        notificationsSwitch.setOnClickListener {
            sharedPreferences.edit().apply {
                putBoolean(Constants.NOTIFICATIONS_ENABLED, notificationsSwitch.isChecked)
                apply()
            }
        }

        val isQrcode = sharedPreferences.getBoolean(Constants.IS_QRCODE, true)

        qrcodeRadio.isChecked = isQrcode
        qrcodeRadio.setOnClickListener {
            sharedPreferences.edit().apply {
                putBoolean(Constants.IS_QRCODE, qrcodeRadio.isChecked)
                apply()
            }
        }

        nfcRadio.isEnabled = hasNfc(baseContext) != null
        nfcRadio.isChecked = !isQrcode
        nfcRadio.setOnClickListener {
            sharedPreferences.edit().apply {
                putBoolean(Constants.IS_QRCODE, !nfcRadio.isChecked)
                apply()
            }
        }
    }
}
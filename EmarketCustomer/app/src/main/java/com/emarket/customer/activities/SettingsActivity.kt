package com.emarket.customer.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.activities.profile.ProfileActivity
import com.emarket.customer.controllers.Fetcher

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
        nfcRadio.isChecked = !isQrcode
        nfcRadio.setOnClickListener {
            sharedPreferences.edit().apply {
                putBoolean(Constants.IS_QRCODE, !nfcRadio.isChecked)
                apply()
            }
        }

        qrcodeRadio.isChecked = isQrcode
        qrcodeRadio.setOnClickListener {
            sharedPreferences.edit().apply {
                putBoolean(Constants.IS_QRCODE, qrcodeRadio.isChecked)
                apply()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_shop -> {
                intent = Intent(this, BasketActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                return true
            }
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
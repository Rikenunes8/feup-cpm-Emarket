package com.emarket.customer.activities

import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.emarket.customer.R
import com.emarket.customer.activities.profile.ProfileActivity
import com.emarket.customer.controllers.Fetcher

class SettingsActivity : AppCompatActivity() {

    private val cardView by lazy { findViewById<CardView>(R.id.card_settings) }
    private val moreToggle by lazy { findViewById<ImageButton>(R.id.more_toggle) }
    private val checkoutRg by lazy { findViewById<RadioGroup>(R.id.checkout_rg) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        moreToggle.setOnClickListener { view ->
            if (checkoutRg.visibility === View.VISIBLE) {
                TransitionManager.beginDelayedTransition(cardView, AutoTransition())
                checkoutRg.visibility = View.GONE
                moreToggle.setImageResource(R.drawable.arrow_down)
            } else {
                TransitionManager.beginDelayedTransition(cardView, AutoTransition())
                checkoutRg.visibility = View.VISIBLE
                moreToggle.setImageResource(R.drawable.arrow_up)
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
                Fetcher.fetchUserData(this, complete = false)
                intent = Intent(this, BasketActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                return true
            }
            R.id.action_profile -> {
                Fetcher.fetchUserData(this, complete = false)
                startActivity(Intent(this, ProfileActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
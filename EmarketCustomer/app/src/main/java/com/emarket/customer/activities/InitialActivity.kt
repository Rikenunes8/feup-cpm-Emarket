package com.emarket.customer.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.models.User
import com.emarket.customer.services.CryptoService
import com.google.gson.Gson

class InitialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial)

        // check if user already registered
        val sharedPref = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val user = Gson().fromJson(sharedPref.getString(Constants.USER_KEY, null), User::class.java)
        if (user != null && CryptoService.keysPresent()) {
            // user already registered
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            // user not registered
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        finish()
    }
}

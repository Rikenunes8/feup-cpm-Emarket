package com.emarket.customer.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.models.User
import com.emarket.customer.models.UserViewModel
import com.emarket.customer.services.CryptoService
import com.google.gson.Gson

class InitialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial)

        // check if user already registered
        val user = UserViewModel(this.application).user
        if (CryptoService.keysPresent() && user != null) {
            // user already registered
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            // user not registered
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }


}
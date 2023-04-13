package com.emarket.customer.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.emarket.customer.Database
import com.emarket.customer.R
import com.emarket.customer.activities.authentication.LoginActivity
import com.emarket.customer.activities.authentication.RegisterActivity
import com.emarket.customer.models.UserViewModel
import com.emarket.customer.services.CryptoService

lateinit var dbLayer : Database

class InitialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial)
        supportActionBar?.hide()

        dbLayer = Database(applicationContext)

        val user = UserViewModel(this.application).user
        val isRegistered = CryptoService.keysPresent() && user != null

        Handler(Looper.getMainLooper()).postDelayed({
            if (isRegistered) startActivity(Intent(this, LoginActivity::class.java))
            else startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }, 1000)

    }
}

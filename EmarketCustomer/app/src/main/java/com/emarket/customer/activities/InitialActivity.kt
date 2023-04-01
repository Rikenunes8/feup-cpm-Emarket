package com.emarket.customer.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.emarket.customer.Database
import com.emarket.customer.R
import com.emarket.customer.models.Transaction
import com.emarket.customer.models.UserViewModel
import com.emarket.customer.models.Voucher
import com.emarket.customer.services.CryptoService

lateinit var dbLayer : Database
lateinit var vouchers : MutableList<Voucher>
lateinit var transactions : MutableList<Transaction>

class InitialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial)

        dbLayer = Database(applicationContext)

        // check if user already registered
        val user = UserViewModel(this.application).user
        if (CryptoService.keysPresent() && user != null) {
            // user already registered
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            // user not registered
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        finish()
    }
}

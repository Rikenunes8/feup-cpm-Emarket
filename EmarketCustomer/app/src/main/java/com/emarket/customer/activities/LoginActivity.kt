package com.emarket.customer.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.emarket.customer.Utils.showToast
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.Utils
import com.emarket.customer.models.User
import com.google.gson.Gson

class LoginActivity : AppCompatActivity() {

    val loginBtn by lazy { findViewById<Button>(R.id.btn_login) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginBtn.setOnClickListener {
            val sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)

            val storedUser = sharedPreferences.getString(Constants.USER_KEY, null)?.let {
                Gson().fromJson(it, User::class.java)
            }

            val nickname = findViewById<EditText>(R.id.edt_log_nickname).text.toString()
            val pass = findViewById<EditText>(R.id.edt_log_pass).text.toString()

            // just a double check
            if (storedUser != null) {
                if (storedUser.nickname == nickname && storedUser.password == Utils.hashPassword(
                        pass
                    )
                ) {
                    // login successful
                    showToast(this, "Login successful")
                    startActivity(Intent(this, BasketActivity::class.java))
                } else {
                    // login failed
                    showToast(this, getString(R.string.log_invalid_credentials))
                }
            } else {
                // user not registered
                // THIS SHOULD NEVER HAPPEN
                showToast(this, "User not registered")
            }

        }

    }
}
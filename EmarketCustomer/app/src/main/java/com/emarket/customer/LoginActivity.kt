package com.emarket.customer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
                if (storedUser.nickname == nickname && storedUser.password == Utils.hashPassword(pass)) {
                    // login successful
                    Toast.makeText(this, "Login successful", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, ShoppingActivity::class.java))
                } else {
                    // login failed
                    Toast.makeText(this, getString(R.string.log_invalid_credentials), Toast.LENGTH_LONG).show()
                }
            } else {
                // user not registered
                // THIS SHOULD NEVER HAPPEN
                Toast.makeText(this, "User not registered", Toast.LENGTH_LONG).show()
            }

        }

    }
}
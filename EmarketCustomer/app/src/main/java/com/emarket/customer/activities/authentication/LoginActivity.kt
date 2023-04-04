package com.emarket.customer.activities.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.emarket.customer.Constants
import com.emarket.customer.Utils.showToast
import com.emarket.customer.R
import com.emarket.customer.Utils
import com.emarket.customer.activities.BasketActivity
import com.emarket.customer.activities.dbLayer
import com.emarket.customer.activities.transactions
import com.emarket.customer.activities.vouchers
import com.emarket.customer.models.Transaction
import com.emarket.customer.models.UserViewModel
import com.emarket.customer.models.Voucher
import com.emarket.customer.models.updateUserData
import com.emarket.customer.services.NetworkService
import com.emarket.customer.services.RequestType
import com.google.gson.Gson
import java.net.URLEncoder
import kotlin.concurrent.thread

data class UserResponse (
    val totalSpent : Double,
    val amountToDiscount : Double,
    val vouchers : List<Voucher>,
    val transactions : List<Transaction>,
    val error: String?
)

class LoginActivity : AppCompatActivity() {

    private val loginBtn by lazy { findViewById<Button>(R.id.btn_login) }
    private val nicknameTv by lazy { findViewById<EditText>(R.id.edt_log_nickname) }
    private val passwordTv by lazy { findViewById<EditText>(R.id.edt_log_pass) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginBtn.setOnClickListener {
            val storedUser = UserViewModel(this.application).user
            val nickname = nicknameTv.text.toString()
            val pass = passwordTv.text.toString()

            // just a double check
            if (storedUser != null) {
                if (storedUser.nickname == nickname && storedUser.password == Utils.hashPassword(pass)) {
                    // login successful
                    fetchUserData()
                    showToast(this, "Login successful")
                    startActivity(Intent(this, BasketActivity::class.java))
                    finish()
                } else {
                    // login failed
                    showToast(this, getString(R.string.error_invalid_credentials))
                }
            } else {
                // user not registered
                // THIS SHOULD NEVER HAPPEN
                showToast(this, "User not registered")
            }
        }
    }

    private fun fetchDatabase() {
        vouchers = dbLayer.getVouchers(onlyUnused = true)
        transactions = dbLayer.getTransactions()
    }

    private fun fetchUserData() {
        thread(start = true) {
            try {
                val userId = UserViewModel(this.application).user?.userId!!
                val endpoint = Constants.SERVER_URL + Constants.USER_ENDPOINT + "?user=${URLEncoder.encode(userId)}"
                val response = NetworkService.makeRequest(RequestType.GET, endpoint)

                val data = Gson().fromJson(response, UserResponse::class.java)
                if (data.error != null) throw Exception()
                
                updateUserData(data)
            } catch (e: Exception) {
                runOnUiThread { showToast(this, getString(R.string.error_fetching_user_information)) }
            }

            fetchDatabase()
        }
    }
}
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
import com.emarket.customer.services.NetworkService
import com.emarket.customer.services.RequestType
import com.google.gson.Gson
import java.net.URLEncoder
import kotlin.concurrent.thread

data class TransactionsResponse(
    val transactions : List<Transaction>,
    val error: String?
)
data class VouchersResponse(
    val vouchers : List<Voucher>?,
    val error : String?
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
                    fetchData()
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
        vouchers = dbLayer.getVouchers(onlyUnUsed = true)
        transactions = dbLayer.getTransactions()
    }

    private fun fetchData() {
        thread(start = true) {
            val userId = UserViewModel(this.application).user?.userId!!
            fetchTransactions(URLEncoder.encode(userId))
            fetchVouchers(URLEncoder.encode(userId))
            fetchDatabase()
        }
    }

    private fun fetchTransactions(userId: String) {
        val response = NetworkService.makeRequest(
            RequestType.GET,
            Constants.SERVER_URL + Constants.TRANSACTIONS_ENDPOINT + "?user=$userId"
        )
        val data = Gson().fromJson(response, TransactionsResponse::class.java)
        if (data.error != null) {
            runOnUiThread { showToast(this, getString(R.string.error_fetching_transactions)) }
            return
        }

        // TODO: only add new transactions to the database (check date of last transaction or
        //   iterate over the list and check if the transaction is already in the database to be safer)
        dbLayer.cleanTransactions()
        data.transactions.forEach { dbLayer.addTransaction(it) }
    }

    private fun fetchVouchers(userId: String) {
        val response = NetworkService.makeRequest(
            RequestType.GET,
            Constants.SERVER_URL + Constants.VOUCHERS_ENDPOINT + "?user=$userId"
        )
        val data = Gson().fromJson(response, VouchersResponse::class.java)
        if (data.error != null) {
            runOnUiThread { showToast(this, getString(R.string.error_fetching_vouchers)) }
            return
        }

        // dbLayer.cleanVouchers()
        data.vouchers?.forEach {
            dbLayer.addVoucher(it)
        }
    }
}
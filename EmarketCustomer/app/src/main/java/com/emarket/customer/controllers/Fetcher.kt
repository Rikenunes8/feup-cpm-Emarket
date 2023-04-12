package com.emarket.customer.controllers

import android.app.Activity
import android.content.Intent
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.Utils
import com.emarket.customer.activities.TransactionDetailsActivity
import com.emarket.customer.activities.authentication.UserResponse
import com.emarket.customer.activities.dbLayer
import com.emarket.customer.models.Transaction
import com.emarket.customer.models.UserViewModel
import com.emarket.customer.models.Voucher
import com.emarket.customer.models.updateUserData
import com.emarket.customer.services.NetworkService
import com.emarket.customer.services.NotificationService
import com.emarket.customer.services.RequestType
import com.google.gson.Gson
import java.net.URLEncoder
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.thread



class Fetcher {
    companion object {
        private const val MIN_TIME_BETWEEN_REQUESTS = 30000 // milliseconds
        private var lastUpdate : Long? = null
        var vouchers : MutableList<Voucher> = mutableListOf()
        var transactions : MutableList<Transaction> = mutableListOf()
        /**
         * Fetch vouchers and transactions from the database
         */
        private fun fetchDataFromDatabase() {
            vouchers = dbLayer.getVouchers(onlyUnused = true)
            transactions = dbLayer.getTransactions()
        }

        /**
         * Updates the user data in the database and the shared preferences
         */
        fun fetchUserData(activity: Activity, complete : Boolean = true, force : Boolean = false) {
            val elapsedTime = Date().time - (lastUpdate ?: 0)
            if (!force && elapsedTime < MIN_TIME_BETWEEN_REQUESTS) return
            thread(start = true) {
                var newTransaction = false
                try {
                    val user = UserViewModel(activity.application).user!!
                    var query = "?user=${URLEncoder.encode(user.userId)}"
                    if (!complete) {
                        val date = dbLayer.getLastTransaction()?.date
                        if (date != null) query += "&date=${URLEncoder.encode(date)}"
                    }
                    val url = Constants.SERVER_URL + Constants.USER_ENDPOINT + query

                    val response = NetworkService.makeRequest(RequestType.GET, url)
                    val userData = Gson().fromJson(response, UserResponse::class.java)
                    if (userData.error != null) throw Exception()

                    newTransaction = updateUserData(activity.application, userData, cleanTransactions = complete)
                    lastUpdate = Date().time
                } catch (e: Exception) {
                    activity.runOnUiThread { Utils.showToast(activity, activity.getString(R.string.error_fetching_user_information)) }
                }
                fetchDataFromDatabase()
                if (newTransaction) {
                    val lastTransaction = transactions[0]
                    val total = lastTransaction.total - ( lastTransaction.discounted ?: 0.0)
                    val intent = Intent(activity, TransactionDetailsActivity::class.java).apply {
                        putExtra("transaction", Gson().toJson(lastTransaction))
                    }
                    NotificationService.sendNotification(
                        activity,
                        activity.getString(R.string.template_notification_title, total),
                        activity.getString(R.string.notification_message),
                        intent
                    )
                }
            }
        }
    }
}
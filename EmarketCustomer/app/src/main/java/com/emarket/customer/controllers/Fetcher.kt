package com.emarket.customer.controllers

import android.app.Activity
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.Utils
import com.emarket.customer.activities.authentication.UserResponse
import com.emarket.customer.activities.dbLayer
import com.emarket.customer.models.Transaction
import com.emarket.customer.models.UserViewModel
import com.emarket.customer.models.Voucher
import com.emarket.customer.models.updateUserData
import com.emarket.customer.services.NetworkService
import com.emarket.customer.services.RequestType
import com.google.gson.Gson
import java.net.URLEncoder
import kotlin.concurrent.thread



class Fetcher {
    companion object {
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
        fun fetchUserData(activity: Activity, complete : Boolean = true) {
            thread(start = true) {
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

                    updateUserData(activity.application, userData, cleanTransactions = complete)
                } catch (e: Exception) {
                    activity.runOnUiThread { Utils.showToast(activity, activity.getString(R.string.error_fetching_user_information)) }
                }
                fetchDataFromDatabase()
            }
        }
    }
}
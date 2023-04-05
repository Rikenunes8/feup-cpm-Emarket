package com.emarket.customer.models

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.emarket.customer.Constants
import com.emarket.customer.activities.appApplication
import com.emarket.customer.activities.authentication.UserResponse
import com.emarket.customer.activities.dbLayer
import com.google.gson.Gson

data class User(
    val userId: String,
    var name: String,
    val nickname: String,
    val password: String,
    var cardNumber: String,
    var amountToDiscount: Double = 0.0,
    var totalSpent : Double = 0.0
)

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences =
        application.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)

    var user: User?
        get() = Gson().fromJson(sharedPreferences.getString(Constants.USER_KEY, null), User::class.java)
        set(value) {
            sharedPreferences.edit().putString(Constants.USER_KEY, Gson().toJson(value)).apply()
        }
}

/**
 * Update the user data in the database and in the shared preferences
 * with the data from the server.
 * @param data the data from the server
 * @see UserResponse
 */
fun updateUserData(data: UserResponse) {
    data.vouchers.forEach { dbLayer.addVoucher(it) }
    data.transactions.forEach { dbLayer.addTransaction(it) }

    val prevUser = UserViewModel(appApplication).user!!
    prevUser.amountToDiscount = data.amountToDiscount
    prevUser.totalSpent = data.totalSpent
    UserViewModel(appApplication).user = prevUser
}
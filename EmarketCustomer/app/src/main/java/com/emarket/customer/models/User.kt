package com.emarket.customer.models

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.emarket.customer.Constants
import com.google.gson.Gson

data class User(
    val userId: String,
    var name: String,
    val nickname: String,
    val password: String,
    val cardNumber: String
)

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences =
        application.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)

    var user: User
        get() = Gson().fromJson(sharedPreferences.getString(Constants.USER_KEY, null), User::class.java)
        set(value) {
            sharedPreferences.edit().putString(Constants.USER_KEY, Gson().toJson(value)).apply()
        }
}
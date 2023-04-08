package com.emarket.customer.activities.payment

import androidx.appcompat.app.AppCompatActivity
import com.emarket.customer.models.Basket
import com.emarket.customer.models.UserViewModel
import com.google.gson.Gson

open class PaymentActivity : AppCompatActivity() {

    fun getPaymentJson() : String {
        val basketJSON = intent.getStringExtra("Basket")!!
        val basket = Gson().fromJson(basketJSON, Basket::class.java)
        val storedUser = UserViewModel(this.application).user
        val userUUID = storedUser!!.userId
        return Gson().toJson(Payment(userUUID, basket))
    }

}

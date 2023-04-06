package com.emarket.customer

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.widget.Toast
import com.emarket.customer.activities.authentication.UserResponse
import com.emarket.customer.activities.dbLayer
import com.emarket.customer.activities.transactions
import com.emarket.customer.activities.vouchers
import com.emarket.customer.models.UserViewModel
import com.emarket.customer.models.updateUserData
import com.emarket.customer.services.CryptoService
import com.emarket.customer.services.NetworkService
import com.emarket.customer.services.RequestType
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import kotlin.concurrent.thread

data class DataSigned(
    val signature: String,
    val data: String
)

object Utils {

    /**
     * Hash the password using SHA-256
     * @param password the password to hash
     * @return the hashed password
     */
    fun hashPassword(password: String): String {
        val bytes = password.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun showToast(ctx: Context, message : String?) {
        Toast.makeText(ctx, message, Toast.LENGTH_LONG).show()
    }

    fun getAttributeColor(context: Context, attributeId : Int) : Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attributeId, typedValue, true)
        return typedValue.resourceId
    }

    /**
     * Generate the QR code content signed with the user's private key
     * @param jsonData the data to sign and put in qrcode
     */
    fun genQRCode(jsonData: String) : String {
        val signatureEncoded = getSignature(jsonData)
        val data = Gson().toJson(DataSigned(signatureEncoded, jsonData))

        return String(data.toByteArray(), StandardCharsets.ISO_8859_1)
    }

    /**
     * Get the signature of the data
     * @param jsonData the data to sign
     * @return the signature of the data
     */
    fun getSignature(jsonData: String): String {
        val dataByteArray = jsonData.toByteArray()
        val signature = CryptoService.signContent(dataByteArray, CryptoService.getPrivKey())!!
        return Base64.getEncoder().encodeToString(signature)
    }

    /**
     * Fetch vouchers and transactions from the database
     */
    fun fetchDataFromDatabase() {
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
                activity.runOnUiThread { showToast(activity, activity.getString(R.string.error_fetching_user_information)) }
            }
            fetchDataFromDatabase()
        }
    }
}

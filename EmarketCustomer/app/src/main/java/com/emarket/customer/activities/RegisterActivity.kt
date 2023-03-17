package com.emarket.customer.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.Utils
import com.emarket.customer.Utils.showToast
import com.emarket.customer.models.User
import com.emarket.customer.services.CryptoService.Companion.decryptContent
import com.emarket.customer.services.CryptoService.Companion.generateAndStoreKeys
import com.emarket.customer.services.CryptoService.Companion.getPrivKey
import com.emarket.customer.services.CryptoService.Companion.getPubKey
import com.emarket.customer.services.CryptoService.Companion.publicKeyToPKCS1
import com.emarket.customer.services.NetworkService
import com.emarket.customer.services.RequestType
import com.google.gson.Gson
import org.json.JSONObject
import java.security.KeyStore
import java.security.PublicKey
import java.util.*
import kotlin.concurrent.thread

data class ServerResponse (
    val uuid: String,
    val serverPubKey: String
)

data class CustomerRegistrationBody (
    val pubKey: String,
    val cardNo : String
)

class RegisterActivity : AppCompatActivity() {

    private val loadingIcon by lazy { findViewById<ProgressBar>(R.id.loading_icon) }
    private val registerButton by lazy { findViewById<Button>(R.id.btn_reg_submit) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerButton.setOnClickListener {

            if (!validateInputData()) return@setOnClickListener

            val cardEditText = findViewById<EditText>(R.id.edt_reg_card)
            val card = cardEditText.text.toString()

            // generate key pair
            if (generateAndStoreKeys(this, getString(R.string.already_registered_err))) {
                val pubKey = getPubKey()
                if (pubKey != null) {
                    startLoading()
                    sendRegistrationData(pubKey, card) // send registration data to server
                } else {
                    Log.e("RegisterActivity", getString(R.string.error_getting_keys))
                    showToast(this, getString(R.string.error_getting_keys))
                }
            }
        }
    }

    /**
     * Validates all the input data
     * @return true if all the data is valid, false otherwise
     */
    private fun validateInputData(): Boolean {
        if (!validateData("Name", R.id.edt_reg_name)) return false
        if (!validateData("Nickname", R.id.edt_reg_nick)) return false
        if (!validateData("Password", R.id.edt_reg_pass)) return false
        if (!validateData("Card no.", R.id.edt_reg_card)) return false

        return true
    }

    /**
     * Validate the input data of the input view
     * @param paramName the name of the parameter
     * @param viewId the id of the view
     * @return true if the data is valid, false otherwise
     */
    private fun validateData(paramName: String, viewId: Int): Boolean {
        val editText = findViewById<EditText>(viewId)
        val value = editText.text.toString()
        if (value.isEmpty()) {
            editText.error = "$paramName is required"
            editText.requestFocus()
            return false
        }
        return true
    }


    /**
     * Send the registration data to the server
     * @param pubKey public key of the user
     * @param cardNo card number of the user
     * @return the response from the server (json string)
     */
    private fun sendRegistrationData(pubKey: PublicKey, cardNo: String) {
        val jsonInputString = Gson().toJson(CustomerRegistrationBody(
            publicKeyToPKCS1(pubKey),
            cardNo
        )).toString()

        thread(start = true) {
            try {
                val response = NetworkService.makeRequest(
                    RequestType.POST,
            Constants.SERVER_URL + Constants.REGISTER_ENDPOINT,
                    jsonInputString
                )

                val jsonResponse = JSONObject(response)
                if (jsonResponse.has("error")) {
                    runOnUiThread { showToast(this@RegisterActivity, jsonResponse.getString("error")) }
                    runOnUiThread { stopLoading() }

                    throw Exception(jsonResponse.getString("error"))
                }

                val serverResp = Gson().fromJson(jsonResponse.toString(), ServerResponse::class.java)
                val uuidEncoded = serverResp.uuid
                val serverPubKey = serverResp.serverPubKey

                val encryptedUUID = Base64.getDecoder().decode(uuidEncoded)
                val uuid = decryptContent(encryptedUUID, getPrivKey())!!

                val name = findViewById<EditText>(R.id.edt_reg_name).text.toString()
                val nickname = findViewById<EditText>(R.id.edt_reg_nick).text.toString()
                val password = findViewById<EditText>(R.id.edt_reg_pass).text.toString()

                val user = User(uuid, name, nickname, Utils.hashPassword(password), cardNo)

                savePersistently(user, serverPubKey)
                startActivity(Intent(this, LoginActivity::class.java))
            } catch (ex: Exception) {
                Log.e("RegisterActivity","ERROR: " + ex.message!!)

                // remove the key pair from the Android Key Store if registration failed
                KeyStore.getInstance(Constants.ANDROID_KEYSTORE).run {
                    load(null)
                    deleteEntry(Constants.STORE_KEY)
                }
            }

            runOnUiThread { stopLoading() }

        }
    }

    /**
     * Save the public key and user uuid persistently
     */
    private fun savePersistently(user: User, serverPubKey: String) {
        // Store the UUID and server public key
        val sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString(Constants.USER_KEY, Gson().toJson(user))

        // TODO: check if this should be a string and if it is ok to store in SharedPreferences
        editor.putString(Constants.SERVER_PUB_KEY, serverPubKey)
        editor.apply()
    }

    private fun startLoading() {
        // show progress bar and hide register button
        loadingIcon.visibility = View.VISIBLE
        registerButton.visibility = View.GONE
    }
    private fun stopLoading() {
        // show register button and hide progress bar
        registerButton.visibility = View.VISIBLE
        loadingIcon.visibility = View.GONE
    }
}
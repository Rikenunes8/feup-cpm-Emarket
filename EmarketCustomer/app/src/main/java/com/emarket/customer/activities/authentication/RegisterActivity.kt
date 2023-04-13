package com.emarket.customer.activities.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.Utils
import com.emarket.customer.Utils.showToast
import com.emarket.customer.controllers.CardNumberEditTextController
import com.emarket.customer.models.User
import com.emarket.customer.services.CryptoService
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
    val serverPubKey: String,
    val certificate: String
)

data class CustomerRegistrationBody (
    val pubKey: String,
    val cardNo : String
)

class RegisterActivity : AppCompatActivity() {

    private val loadingIcon by lazy { findViewById<ProgressBar>(R.id.loading_icon) }
    private val registerButton by lazy { findViewById<Button>(R.id.btn_reg_submit) }
    private val nameEditText by lazy { findViewById<EditText>(R.id.edt_reg_name) }
    private val nickEditText by lazy { findViewById<EditText>(R.id.edt_reg_nick) }
    private val passEditText by lazy { findViewById<EditText>(R.id.edt_reg_pass) }
    private val cardEditText by lazy { findViewById<EditText>(R.id.edt_reg_card) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerButton.setOnClickListener {
            if (!validateInputData()) return@setOnClickListener

            // generate key pair
            if (generateAndStoreKeys(this, getString(R.string.error_already_registered))) {
                val pubKey = getPubKey()
                if (pubKey != null) {
                    startLoading()
                    sendRegistrationData(pubKey, cardEditText.text.toString()) // send registration data to server
                } else {
                    Log.e("RegisterActivity", getString(R.string.error_getting_keys))
                    showToast(this, getString(R.string.error_getting_keys))
                }
            }
        }

        cardEditText.addTextChangedListener(CardNumberEditTextController())
    }

    /**
     * Validates all the input data
     * @return true if all the data is valid, false otherwise
     */
    private fun validateInputData(): Boolean {
        if (!validateData("Name", nameEditText)) return false
        if (!validateData("Nickname", nickEditText)) return false
        if (!validateData("Password", passEditText)) return false
        if (!validateData("Card no.", cardEditText)) return false

        return true
    }

    /**
     * Validate the input data of the input view
     * @param paramName the name of the parameter
     * @param editText the edit text view
     * @return true if the data is valid, false otherwise
     */
    private fun validateData(paramName: String, editText: EditText): Boolean {
        val value = editText.text.toString()
        if (value.isEmpty()) {
            editText.error = "$paramName is required"
            editText.requestFocus()
            return false
        } else if (paramName == "Card no." && value.length != 19) {
            editText.error = "$paramName should have 16 digits"
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
                val certificate = serverResp.certificate

                val encryptedUUID = Base64.getDecoder().decode(uuidEncoded)
                val uuid = decryptContent(encryptedUUID, getPrivKey())!!

                val name = findViewById<EditText>(R.id.edt_reg_name).text.toString()
                val nickname = findViewById<EditText>(R.id.edt_reg_nick).text.toString()
                val password = findViewById<EditText>(R.id.edt_reg_pass).text.toString()

                val user = User(uuid, name, nickname, Utils.hashPassword(password), cardNo)

                savePersistently(user, serverPubKey, certificate)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
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
    private fun savePersistently(user: User, serverPubKey: String, certificate: String) {
        // Store the UUID and server public key
        val sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString(Constants.USER_KEY, Gson().toJson(user))

        // TODO: check if this should be a string and if it is ok to store in SharedPreferences
        editor.putString(Constants.SERVER_PUB_KEY, serverPubKey)
        editor.apply()

        // Store the server certificate
        CryptoService.storeCertificate(Constants.SERVER_CERTIFICATE, certificate)
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
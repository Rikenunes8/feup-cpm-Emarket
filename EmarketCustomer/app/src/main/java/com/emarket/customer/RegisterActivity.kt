package com.emarket.customer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.os.Bundle
import android.security.KeyPairGeneratorSpec
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import com.emarket.customer.Utils.showToast
import com.emarket.customer.models.User
import com.emarket.customer.services.NetworkService
import com.emarket.customer.services.RequestType
import com.google.gson.Gson
import org.json.JSONObject
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PublicKey
import java.util.*
import javax.security.auth.x500.X500Principal
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
            if (generateAndStoreKeys()) {
                val pubKey = Utils.getPubKey()
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
     * Check if the key pair is already present in the Android Key Store
     */
    private fun keysPresent(): Boolean {
        val entry = KeyStore.getInstance(Constants.ANDROID_KEYSTORE).run {
            load(null)
            getEntry(Constants.STORE_KEY, null)
        }
        return (entry != null)
    }

    /**
     * Generate a new key pair and store it in the Android Key Store
     */
    private fun generateAndStoreKeys(): Boolean {
        try {
            if (!keysPresent()) {
                val spec = KeyPairGeneratorSpec.Builder(this)
                    .setKeySize(Constants.KEY_SIZE)
                    .setAlias(Constants.STORE_KEY)
                    .setSubject(X500Principal("CN=" + Constants.STORE_KEY))
                    .setSerialNumber(BigInteger.valueOf(Constants.serialNr))
                    .setStartDate(GregorianCalendar().time)
                    .setEndDate(GregorianCalendar().apply { add(Calendar.YEAR, 10) }.time)
                    .build()
                KeyPairGenerator.getInstance(Constants.KEY_ALGO, Constants.ANDROID_KEYSTORE).run {
                    initialize(spec)
                    generateKeyPair()
                }
            } else {
                showToast(this, getString(R.string.already_registered_err))
                Log.e("RegisterActivity", "Key pair already present")
                return false
            }
        }
        catch (ex: Exception) {
            showToast(this, ex.message)
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
            Base64.getEncoder().encodeToString(pubKey.encoded),
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

                val uuid = serverResp.uuid
                val serverPubKey = serverResp.serverPubKey

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
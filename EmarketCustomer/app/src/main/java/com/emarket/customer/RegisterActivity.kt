package com.emarket.customer

import android.content.Context
import android.os.Bundle
import android.security.KeyPairGeneratorSpec
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.emarket.customer.Services.NetworkService
import com.emarket.customer.Services.RequestType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PublicKey
import java.util.*
import javax.security.auth.x500.X500Principal

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
                // send registration data to server
                val pubKey = Utils.getPubKey()
                if (pubKey != null) {
                    // show progress bar and hide register button
                    loadingIcon.visibility = View.VISIBLE
                    registerButton.visibility = View.GONE

                    sendRegistrationData(pubKey, card)

                } else {
                    Log.e("RegisterActivity", getString(R.string.error_getting_keys))
                    Toast.makeText(this,
                        getString(R.string.error_getting_keys),
                        Toast.LENGTH_LONG).show()
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
                Toast.makeText(this, getString(R.string.already_registered_err),
                    Toast.LENGTH_LONG).show()
                Log.e("RegisterActivity", "Key pair already present")
                return false
            }
        }
        catch (ex: Exception) {
            // show a red toast message with the exception message
            Toast.makeText(this, ex.message, Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    /**
     * Send the registration data to the server
     * @param pubKey public key of the user
     * @param cardNo card number of the user
     */
    private fun sendRegistrationData(pubKey: PublicKey, cardNo: String) {
        val jsonInputString = "{" +
                "\"pubKey\": \"${Base64.getEncoder().encodeToString(pubKey.encoded)}\", " +
                "\"cardNo\": \"$cardNo\"" +
            "}"

        CoroutineScope(Dispatchers.IO).launch {

            try {
                val response = NetworkService.makeRequest(
                    RequestType.POST,
            Constants.SERVER_URL + Constants.REGISTER_ENDPOINT,
                    jsonInputString
                )

                val jsonResponse = JSONObject(response)
                if (jsonResponse.has("error")) {
                    Log.d("RegisterActivity", "Resp: $jsonResponse")
                    // show a red toast message with the error message
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity,
                            jsonResponse.getString("error"),
                            Toast.LENGTH_LONG).show()
                    }

                    withContext(Dispatchers.Main) {
                        // show register button and hide progress bar
                        registerButton.visibility = View.VISIBLE
                        loadingIcon.visibility = View.GONE
                    }

                    return@launch
                }

                val uuid = jsonResponse.getString("uuid")
                val serverPubKey = jsonResponse.getString("serverPubKey")

                Log.d("RegisterActivity", "UUID: $uuid\nServerPubKey: $serverPubKey" )

                savePersistently(uuid, serverPubKey)

            } catch (ex: Exception) {
                Log.e("RegisterActivity","ERROR: " + ex.message!!)
            }


            withContext(Dispatchers.Main) {
                // show register button and hide progress bar
                registerButton.visibility = View.VISIBLE
                loadingIcon.visibility = View.GONE
            }

        }
    }

    /**
     * Save the public key and user uuid persistently
     */
    private fun savePersistently(uuid: String, serverPubKey: String) {
        // Store the UUID and server public key
        val sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(Constants.UUID_KEY, uuid)

        // TODO: check if this should be a string and if ok to store in SharedPreferences
        editor.putString(Constants.SERVER_PUB_KEY, serverPubKey)
        editor.apply()
    }

}
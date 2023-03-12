package com.emarket.customer

import android.content.Context
import android.os.Bundle
import android.security.KeyPairGeneratorSpec
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val registerButton = findViewById<Button>(R.id.btn_reg_submit)
        registerButton.setOnClickListener {

            // get the registration data
            val nameEditText = findViewById<EditText>(R.id.edt_reg_name)
            val name = nameEditText.text.toString()

            val nickEditText = findViewById<EditText>(R.id.edt_reg_nick)
            val nick = nickEditText.text.toString()

            val passEditText = findViewById<EditText>(R.id.edt_reg_pass)
            val pass = passEditText.text.toString()

            val cardEditText = findViewById<EditText>(R.id.edt_reg_card)
            val card = cardEditText.text.toString()

            // perform validation on the registration data
            if (name.isEmpty()) {
                nameEditText.error = "Name is required"
                nameEditText.requestFocus()
                return@setOnClickListener
            } else if (nick.isEmpty()) {
                nickEditText.error = "Nick is required"
                nickEditText.requestFocus()
                return@setOnClickListener
            } else if (pass.isEmpty()) {
                passEditText.error = "Password is required"
                passEditText.requestFocus()
                return@setOnClickListener
            } else if (card.isEmpty()) {
                cardEditText.error = "Card is required"
                cardEditText.requestFocus()
                return@setOnClickListener
            }

            // generate key pair
            if (generateAndStoreKeys()) {
                // send registration data to server
                val pubKey = Utils.getPubKey()
                if (pubKey != null) {
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
            val connection = withContext(Dispatchers.IO) {
                URL(Constants.serverUrl + getString(R.string.register_endoint))
                    .openConnection()
            } as HttpURLConnection

            try {
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.doInput = true

                connection.outputStream.use { os ->
                    val input = jsonInputString.toByteArray(charset("utf-8"))
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val response = inputStream.bufferedReader().use(BufferedReader::readText)
                    // Parse the JSON response using Gson
                    val responseJson = JSONObject(response)

                    val uuid = responseJson.getString("uuid")
                    val serverPubKey = responseJson.getString("serverPubKey")

                    withContext(Dispatchers.IO) {
                        inputStream.close()
                    }

                    savePersistently(uuid, serverPubKey)
                } else {
                    // Handle error response
                    Log.e("RegisterActivity", "ERROR $responseCode" +
                            " : ${connection.responseMessage}")
                }
            } catch (ex: Exception) {
                Log.e("RegisterActivity","ERROR: " + ex.message!!)
            } finally {
                connection.disconnect()
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
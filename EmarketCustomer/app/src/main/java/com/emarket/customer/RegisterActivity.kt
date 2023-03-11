package com.emarket.customer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.KeyPairGeneratorSpec
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
            // retrieve form data here
            val nameEditText = findViewById<EditText>(R.id.edt_reg_name)
            val name = nameEditText.text.toString()

            val nickEditText = findViewById<EditText>(R.id.edt_reg_nick)
            val nick = nickEditText.text.toString()

            val passEditText = findViewById<EditText>(R.id.edt_reg_pass)
            val pass = passEditText.text.toString()

            val cardEditText = findViewById<EditText>(R.id.edt_reg_card)
            val card = cardEditText.text.toString()

            // perform validation and processing here
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

            // example: show a toast message with the name
            //Toast.makeText(this, "Name: $name : $nick : $pass : $card", Toast.LENGTH_SHORT).show()

            // generate key pair
            if (generateAndStoreKeys()) {
                // send registration data to server
                val pubKey = Utils.getPubKey()
                if (pubKey != null) {
                    sendRegistrationData(pubKey, card)
                } else {
                    Log.e("RegisterActivity", "Key pair generation failed")
                    Toast.makeText(this,
                        "Could not get the public key from the key store",
                        Toast.LENGTH_LONG).show()
                }
            } else {
                Log.e("RegisterActivity", "Key pair generation failed")
            }
        }
    }

    /**
     * Check if the key pair is already present in the Android Key Store
     */
    private fun keysPresent(): Boolean {
        val entry = KeyStore.getInstance(Constants.ANDROID_KEYSTORE).run {
            load(null)
            getEntry(Constants.keyname, null)
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
                    .setAlias(Constants.keyname)
                    .setSubject(X500Principal("CN=" + Constants.keyname))
                    .setSerialNumber(BigInteger.valueOf(Constants.serialNr))
                    .setStartDate(GregorianCalendar().time)
                    .setEndDate(GregorianCalendar().apply { add(Calendar.YEAR, 10) }.time)
                    .build()
                KeyPairGenerator.getInstance(Constants.KEY_ALGO, Constants.ANDROID_KEYSTORE).run {
                    initialize(spec)
                    generateKeyPair()
                }
            } else {
                Toast.makeText(this, "Key pair already present", Toast.LENGTH_LONG).show()
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
        /*
        val publicKeyStr = Base64.getEncoder().encodeToString(pubKey.encoded)
        val jsonInputString = "{\"publicKey\": \"$publicKeyStr\", \"cardNumber\": \"$cardNo\"}"

        val url = URL(Constants.serverUrl + "register")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json; utf-8")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true
        connection.outputStream.use { os ->
            val input = jsonInputString.toByteArray(charset("utf-8"))
            os.write(input, 0, input.size)
            os.close()
        }

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use(BufferedReader::readText)
            inputStream.close()
            // Do something with the response
            Log.d("RegisterActivity", response)
        } else {
            // Handle error response
        }

        connection.disconnect()
        */
    }


}
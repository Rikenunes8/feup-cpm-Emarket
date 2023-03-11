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
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
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
                // go to app page
                Toast.makeText(this, "Key pair generated: " + Utils.getPubKey(), Toast.LENGTH_LONG).show()
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
                Toast.makeText(this, getString(R.string.already_registered_err), Toast.LENGTH_SHORT).show()
                val entry = KeyStore.getInstance(Constants.ANDROID_KEYSTORE).run {
                    load(null)
                    getEntry(Constants.keyname, null)
                }
                Log.e("RegisterActivity", entry.toString())
            }
        }
        catch (ex: Exception) {
            // show a red toast message with the exception message
            Toast.makeText(this, ex.message, Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }


}
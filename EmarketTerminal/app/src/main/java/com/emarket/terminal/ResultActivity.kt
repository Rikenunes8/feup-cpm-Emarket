package com.emarket.terminal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.integration.android.IntentIntegrator

class ResultActivity : AppCompatActivity() {
    private val titleTv by lazy { findViewById<TextView>(R.id.title) }
    private val bodyTv by lazy { findViewById<TextView>(R.id.body) }
    private val errorDetailTv by lazy { findViewById<TextView>(R.id.error) }
    private val priceLabelTv by lazy { findViewById<TextView>(R.id.price_label) }
    private val priceTv by lazy { findViewById<TextView>(R.id.price) }
    private val resultBtn by lazy { findViewById<Button>(R.id.result_btn) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val message = intent.getStringExtra("RESULT")
        val result = Gson().fromJson(message, Message::class.java)
        result.error?.let {
            titleTv.text = getString(R.string.title_error)
            bodyTv.text = getString(R.string.body_error)
            errorDetailTv.text = it
            errorDetailTv.visibility = View.VISIBLE
            resultBtn.text = getString(R.string.try_again)
        }

        result.success?.let {
            titleTv.text = getString(R.string.title_thank_you)
            bodyTv.text = getString(R.string.body_success)
            priceLabelTv.visibility = View.VISIBLE
            priceTv.text = getString(R.string.template_price, result.total)
            priceTv.visibility = View.VISIBLE
            resultBtn.text = getString(R.string.finish)
        }
        resultBtn.setOnClickListener { finish() }
    }
}

data class Message (
    var error : String? = null,
    var success : String? = null,
    var total : Double? = null
)
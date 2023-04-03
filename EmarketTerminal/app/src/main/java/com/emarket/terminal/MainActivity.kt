package com.emarket.terminal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.emarket.terminal.NetworkService.Companion.makeRequest
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "Terminal"
        private const val REQUEST_CAMERA_PERMISSION = 1
    }

    private val scanBtn by lazy { findViewById<Button>(R.id.scan_btn) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanBtn.setOnClickListener {
            if (!requestCameraPermission())
                read.launch(IntentIntegrator(this).createScanIntent())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.mn_scanner -> {
                if (!requestCameraPermission())
                    read.launch(IntentIntegrator(this).createScanIntent())
            }
        }
        return true
    }

    private fun requestCameraPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permission == PackageManager.PERMISSION_GRANTED) return false
        val requests = arrayOf(Manifest.permission.CAMERA)
        ActivityCompat.requestPermissions(this, requests, REQUEST_CAMERA_PERMISSION)
        return true
    }

    private val read = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val intentResult : IntentResult? = IntentIntegrator.parseActivityResult(it.resultCode, it.data)
        if (intentResult != null) {
            if (intentResult.contents != null) {
                Log.d(TAG, "QR Code Content: ${intentResult.contents}")
                Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
                processQRCode(intentResult)
            } else {
                Toast.makeText(this, "Scan failed", Toast.LENGTH_LONG).show()
                Log.d(TAG, "Scan failed")
            }
        }
    }

    private fun processQRCode(result : IntentResult) {
        val data = result.contents.toByteArray(StandardCharsets.ISO_8859_1).decodeToString()
        thread(start = true) {
            val res = makeRequest(
                RequestType.POST,
                Constants.SERVER_URL + Constants.CHECKOUT_ENDPOINT,
                data)
            runOnUiThread {
                intent = Intent(this, ResultActivity::class.java)
                intent.putExtra("RESULT", res)
                startActivity(intent)
            }
        }
    }
}

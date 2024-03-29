package com.emarket.terminal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.emarket.terminal.NetworkService.Companion.makeRequest
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread

const val READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
    }

    private val nfc by lazy { NfcAdapter.getDefaultAdapter(this) }
    private val paymentReader by lazy { PaymentReader(::paymentListener) }
    private val scanBtn by lazy { findViewById<Button>(R.id.scan_btn) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanBtn.setOnClickListener {
            requestCameraPermissionAndStartScan()
        }
    }

    override fun onResume() {
        super.onResume()
        nfc?.enableReaderMode(this, paymentReader, READER_FLAGS, null)
    }

    override fun onPause() {
        super.onPause()
        nfc?.disableReaderMode(this)
    }

    private fun paymentListener(payment: ByteArray) {
        nfc?.disableReaderMode(this)
        processPayment(payment)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.mn_scanner -> requestCameraPermissionAndStartScan()
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) startScan()
            }
        }
    }

    private fun requestCameraPermissionAndStartScan() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startScan()
        } else {
            val requests = arrayOf(Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(this, requests, REQUEST_CAMERA_PERMISSION)
        }
    }

    private fun startScan() {
        val intentIntegrator = IntentIntegrator(this).apply {
            setPrompt(getString(R.string.scanner_prompt))
            setOrientationLocked(false)
        }
        readQRCode.launch(intentIntegrator.createScanIntent())
    }

    private val readQRCode = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val intentResult : IntentResult? = IntentIntegrator.parseActivityResult(it.resultCode, it.data)
        if (intentResult?.contents != null) {
            val data = intentResult.contents.toByteArray(StandardCharsets.ISO_8859_1)
            processPayment(data)
        }
    }

    private fun processPayment(data : ByteArray) {
        thread(start = true) {
            val res = makeRequest(
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

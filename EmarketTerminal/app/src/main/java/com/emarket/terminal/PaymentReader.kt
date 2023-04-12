package com.emarket.terminal

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import java.io.IOException

/* Utility top-level function */
fun byteArrayToHex(ba: ByteArray): String {
    val sb = StringBuilder(ba.size * 2)
    for (b in ba) sb.append(String.format("%02x", b))
    return sb.toString()
}

fun hexStringToByteArray(s: String): ByteArray {
    val data = ByteArray(s.length/2)
    var k = 0
    while (k < s.length) {
        data[k/2] = ((Character.digit(s[k], 16) shl 4) + Character.digit(s[k+1], 16)).toByte()
        k += 2
    }
    return data
}

private const val CARD_AID = "F012233445"
private const val CMD_SEL_AID = "00A40400"
private const val CMD_GET_SECOND = "80010000"
private val RES_OK_SW = hexStringToByteArray("9000")

class PaymentReader(private val paymentListener: (ByteArray)->Unit) : NfcAdapter.ReaderCallback {
    override fun onTagDiscovered(tag: Tag) {
        val isoDep = IsoDep.get(tag)                    // Android smartcard reader emulator
        if (isoDep != null) {
            try {
                isoDep.connect()                            // establish a connection with the card and send 'select aid' command
                val result = isoDep.transceive(hexStringToByteArray(CMD_SEL_AID + String.format("%02X", CARD_AID.length/2) + CARD_AID))
                val rLen = result.size
                val status = byteArrayOf(result[rLen-2], result[rLen-1])
                val more = (result[0] == 1.toByte())
                if (RES_OK_SW.contentEquals(status)) {
                    if (more) {
                        val second = isoDep.transceive(hexStringToByteArray(CMD_GET_SECOND))
                        val len = second.size
                        if (RES_OK_SW.contentEquals(byteArrayOf(second[len-2], second[len-1])))
                            paymentListener(result.sliceArray(1..rLen-3) + second.sliceArray(0..len-3))
                    }
                    else
                        paymentListener(result.sliceArray(1..rLen-3))
                }
            } catch (e: IOException) {
                Log.e("PaymentReader", "Error communicating with customer: $e")
            }
        }
    }
}
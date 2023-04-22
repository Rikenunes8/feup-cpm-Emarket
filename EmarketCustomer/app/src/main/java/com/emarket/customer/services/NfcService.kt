package com.emarket.customer.services

import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.emarket.customer.Constants
import com.emarket.customer.Utils.hexStringToByteArray
import java.nio.charset.StandardCharsets

object Card {
    /* Card internal state */
    var payment = ByteArray(0)
    /* Card constant values */
    private const val CARD_AID = "F012233445"         // AID for this applet service.
    private const val CMD_SEL_AID = "00A40400"        // SmartCard select AID command
    private const val CMD_GET_SECOND = "80010000"     // Get second part of certificate when it exceeds max response size
    const val MAX_RES_SIZE = 250                      // maximum data to send in response
    /* Card APDUs and response status words (SW) */
    val SELECT_APDU = hexStringToByteArray(CMD_SEL_AID + String.format("%02X", CARD_AID.length/2) + CARD_AID)
    val SECOND_APDU = hexStringToByteArray(CMD_GET_SECOND)
    val OK_SW = hexStringToByteArray("9000")             // "OK" status word (0x9000)
    val UNKNOWN_CMD_SW = hexStringToByteArray("0000")    // "UNKNOWN" command status word (0X0000)
}

class NfcService : HostApduService() {
    override fun processCommandApdu(command: ByteArray, extra: Bundle?): ByteArray {
        val content : String = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(Constants.PAYMENT, "") ?: ""
        if (!PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(Constants.PREF_SEND_ENABLED, false)) {
            return Card.UNKNOWN_CMD_SW  // if app not running PaymentNfcActivity don't send anything
        }
        return if (Card.SELECT_APDU.contentEquals(command)) {
            Card.payment = content.toByteArray(StandardCharsets.ISO_8859_1)
            if (Card.payment.size <= Card.MAX_RES_SIZE) { // send complete payment (no second part)
                byteArrayOf(0) + Card.payment + Card.OK_SW
            } else {    // send first part if too big
                byteArrayOf(1) + Card.payment.sliceArray(0 until Card.MAX_RES_SIZE) + Card.OK_SW
            }
        } else if (Card.SECOND_APDU.contentEquals(command)) {   // send second part
            Card.payment.sliceArray(Card.MAX_RES_SIZE until Card.payment.size) + Card.OK_SW
        } else {    // APDU not recognized
            Card.UNKNOWN_CMD_SW
        }
    }

    override fun onDeactivated(cause: Int) {
        val localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        val broadcastIntent = Intent(Constants.ACTION_CARD_DONE)
        localBroadcastManager.sendBroadcast(broadcastIntent)
    }
}

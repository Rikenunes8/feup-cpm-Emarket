package com.emarket.customer.dialogs

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialog
import com.emarket.customer.R
import com.emarket.customer.Utils

typealias EditPaymentListener = (String) -> Unit

class EditPaymentDialog(ctx: Context,
                         private val cardNumber: String,
                         val listener: EditPaymentListener ) : AppCompatDialog(ctx) {

    private val edtCardNo by lazy { findViewById<EditText>(R.id.edt_field)!! }
    private val saveBtn by lazy { findViewById<Button>(R.id.edt_btn_ok)!! }
    private val cancelBtn by lazy { findViewById<Button>(R.id.edt_btn_cancel)!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.edit_personal_title)
        setContentView(R.layout.edit_single_info)

        edtCardNo.setText(cardNumber)
        edtCardNo.setHint(R.string.edit_payment_cardNo)

        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        saveBtn.setOnClickListener {

            val value = edtCardNo.text.toString()
            if (value.isEmpty()) {
                edtCardNo.error = "${context.getString(R.string.edit_payment_cardNo)} is required"
                edtCardNo.requestFocus()
                return@setOnClickListener
            }

            listener(edtCardNo.text.toString())
            dismiss()

            Utils.showToast(context, context.getString(R.string.edit_payment_success))
        }

        cancelBtn.setOnClickListener {
            dismiss()
        }
    }
}
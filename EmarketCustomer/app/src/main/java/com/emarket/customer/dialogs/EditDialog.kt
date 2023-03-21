package com.emarket.customer.dialogs

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialog
import com.emarket.customer.R
import com.emarket.customer.Utils

enum class EditDialogType {
    PERSONAL, PAYMENT
}

typealias EditListener = (String) -> Unit

class EditDialog(ctx: Context,
                 private val field: String,
                 val listener: EditListener,
                 private val editDialogType: EditDialogType) : AppCompatDialog(ctx) {

    private val edtField by lazy { findViewById<EditText>(R.id.edt_field)!! }
    private val saveBtn by lazy { findViewById<Button>(R.id.edt_btn_ok)!! }
    private val cancelBtn by lazy { findViewById<Button>(R.id.edt_btn_cancel)!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var titleId = 0
        var fieldName = ""
        var confirmationMsg = ""

        when (editDialogType) {
            EditDialogType.PAYMENT -> {
                titleId = R.string.edit_payment_title
                fieldName = context.getString(R.string.edit_payment_cardNo)
                confirmationMsg = context.getString(R.string.edit_payment_success)
            }
            EditDialogType.PERSONAL -> {
                titleId = R.string.edit_personal_title
                fieldName = context.getString(R.string.edit_personal_name)
                confirmationMsg = context.getString(R.string.edit_personal_success)
            }
        }

        setTitle(titleId)
        setContentView(R.layout.edit_single_info)

        edtField.setText(field)
        edtField.hint = fieldName

        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        saveBtn.setOnClickListener {

            val value = edtField.text.toString()
            if (value.isEmpty()) {
                edtField.error = "$fieldName is required"
                edtField.requestFocus()
                return@setOnClickListener
            }

            listener(edtField.text.toString())
            dismiss()

            Utils.showToast(context, confirmationMsg)
        }

        cancelBtn.setOnClickListener {
            dismiss()
        }
    }
}
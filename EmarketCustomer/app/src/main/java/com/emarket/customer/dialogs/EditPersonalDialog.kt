package com.emarket.customer.dialogs

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import com.emarket.customer.R

typealias EditPersonalListener = (String) -> Unit

class EditPersonalDialog(ctx: Context,
                         private val name: String,
                         val listener: EditPersonalListener ) : AppCompatDialog(ctx) {

    private val edtName by lazy { findViewById<EditText>(R.id.edt_name)!! }
    private val saveBtn by lazy { findViewById<Button>(R.id.edt_personal_bt_ok)!! }
    private val cancelBtn by lazy { findViewById<Button>(R.id.edt_personal_bt_cancel)!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.edit_personal_title)
        setContentView(R.layout.edit_personal_info)

        with(edtName) {
            setText(name)
        }

        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)


        saveBtn.setOnClickListener {

            val value = edtName.text.toString()
            if (value.isEmpty()) {
                edtName.error = "${context.getString(R.string.edit_personal_name)} is required"
                edtName.requestFocus()
                return@setOnClickListener
            }

            listener(edtName.text.toString())
            dismiss()

            Toast.makeText(context,
                context.getString(R.string.edit_personal_success),
                Toast.LENGTH_LONG).show()
        }

        cancelBtn.setOnClickListener {
            dismiss()
        }
    }
}
package com.emarket.customer.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.emarket.customer.R

class EditPaymentDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return EditPaymentDialog(activity as Context,
            requireArguments().getString("cardNumber", getString(R.string.edit_personal_name)),
            requireArguments().getSerializable("listener") as EditPaymentListener)
    }

    companion object {
        fun newInstance(cardNumber: String, listener: EditPersonalListener): EditPaymentDialogFragment {
            return EditPaymentDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("cardNumber", cardNumber)
                    putSerializable("listener", listener as java.io.Serializable)
                }
            }
        }
    }
}
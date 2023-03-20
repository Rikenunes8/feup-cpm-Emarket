package com.emarket.customer.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.emarket.customer.R
import com.emarket.customer.dialogs.EditPersonalDialog
import com.emarket.customer.dialogs.EditPersonalListener

class EditPersonalDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return EditPersonalDialog(activity as Context,
            requireArguments().getString("name", getString(R.string.edit_personal_name)),
            requireArguments().getSerializable("listener") as EditPersonalListener)
    }

    companion object {
        fun newInstance(name: String, listener: EditPersonalListener): EditPersonalDialogFragment {
            return EditPersonalDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("name", name)
                    putSerializable("listener", listener as java.io.Serializable)
                }
            }
        }
    }
}
package com.emarket.customer.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment


class EditDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return EditDialog(activity as Context,
            requireArguments().getString("field", ""),
            requireArguments().getSerializable("listener") as EditListener,
            EditDialogType.valueOf(requireArguments().getString("editDialogType", "PERSONAL")!!))
    }

    companion object {
        fun newInstance(field: String, listener: EditListener,
                        editDialogType: EditDialogType): EditDialogFragment {
            return EditDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("field", field)
                    putSerializable("listener", listener as java.io.Serializable)
                    putString("editDialogType", editDialogType.name)
                }
            }
        }
    }
}

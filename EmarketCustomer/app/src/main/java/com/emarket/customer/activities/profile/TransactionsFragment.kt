package com.emarket.customer.activities.profile

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.emarket.customer.Constants
import com.emarket.customer.R
import com.emarket.customer.Utils
import com.emarket.customer.Utils.showToast
import com.emarket.customer.controllers.Fetcher.Companion.transactions
import com.emarket.customer.controllers.adapters.TransactionsListAdapter
import com.emarket.customer.databinding.FragmentTransactionsBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.hours

/**
 * Use the [TransactionsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TransactionsFragment : Fragment() {
    private lateinit var binding: FragmentTransactionsBinding
    private val currentTime: Long = System.currentTimeMillis()
    private var selectedBgDate: Calendar = Calendar.getInstance()
    private var selectedEndDate: Calendar = Calendar.getInstance()
    private var registrationTime: Long = currentTime

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val sharedPreferences = requireContext().getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        registrationTime = sharedPreferences.getLong(Constants.REGISTRATION_DATE, currentTime)
        selectedBgDate.timeInMillis = registrationTime
        selectedEndDate.timeInMillis = currentTime

        binding = FragmentTransactionsBinding.inflate(layoutInflater)
        binding.rvTransactions.adapter = TransactionsListAdapter(transactions)
        binding.dateBg.setText(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(selectedBgDate.time))
        binding.dateEnd.setText(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(selectedEndDate.time))

        if (transactions.isEmpty()) binding.tvNoTransactions.visibility = View.VISIBLE

        binding.dateBg.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) openDateDialog(view as EditText, selectedBgDate, registrationTime)
        }
        binding.dateEnd.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) openDateDialog(view as EditText, selectedEndDate, selectedBgDate.timeInMillis)
        }

        binding.bgCalendarBtn.setOnClickListener {
            openDateDialog(binding.dateBg, selectedBgDate, registrationTime)
        }
        binding.endCalendarBtn.setOnClickListener {
            openDateDialog(binding.dateEnd, selectedEndDate, selectedBgDate.timeInMillis)
        }
        
        return binding.root
    }
    private fun openDateDialog(dateView: EditText, selectedDate: Calendar, minDate: Long) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                // Update the selected date when the user selects a date
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, monthOfYear)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                // Update the text of your date input with the selected date
                dateView.setText(
                    SimpleDateFormat("dd/MM/yyyy", Locale.US).format(selectedDate.time)
                )
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        // Show the date picker dialog
        datePickerDialog.datePicker.minDate = minDate
        datePickerDialog.datePicker.maxDate = currentTime
        datePickerDialog.show()
        dateView.clearFocus()
    }

}



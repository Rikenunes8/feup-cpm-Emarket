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
import com.emarket.customer.controllers.Fetcher.Companion.transactions
import com.emarket.customer.controllers.adapters.TransactionsListAdapter
import com.emarket.customer.databinding.FragmentTransactionsBinding
import com.emarket.customer.models.Transaction
import java.text.SimpleDateFormat
import java.util.*

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
    private lateinit var filteredTransactions: MutableList<Transaction>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val sharedPreferences = requireContext().getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        registrationTime = sharedPreferences.getLong(Constants.REGISTRATION_DATE, currentTime)
        selectedBgDate.timeInMillis = registrationTime
        selectedEndDate.timeInMillis = currentTime

        binding = FragmentTransactionsBinding.inflate(layoutInflater)
        binding.dateBg.setText(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(selectedBgDate.time))
        binding.dateEnd.setText(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(selectedEndDate.time))

        filterTransactions()
        binding.rvTransactions.adapter = TransactionsListAdapter(filteredTransactions)
        if (transactions.isEmpty()) binding.tvNoTransactions.visibility = View.VISIBLE


        binding.dateBg.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) openDateDialog(view as EditText, selectedBgDate, registrationTime)
            filterTransactions()
            (binding.rvTransactions.adapter as TransactionsListAdapter).notifyDataSetChanged()
        }
        binding.dateEnd.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) openDateDialog(view as EditText, selectedEndDate, selectedBgDate.timeInMillis)
            filterTransactions()
            (binding.rvTransactions.adapter as TransactionsListAdapter).notifyDataSetChanged()
        }

        binding.bgCalendarBtn.setOnClickListener {
            openDateDialog(binding.dateBg, selectedBgDate, registrationTime)
            filterTransactions()
            (binding.rvTransactions.adapter as TransactionsListAdapter).notifyDataSetChanged()
        }
        binding.endCalendarBtn.setOnClickListener {
            openDateDialog(binding.dateEnd, selectedEndDate, selectedBgDate.timeInMillis)
            filterTransactions()
            (binding.rvTransactions.adapter as TransactionsListAdapter).notifyDataSetChanged()
        }
        
        return binding.root
    }
    private fun openDateDialog(dateView: EditText, selectedDate: Calendar, minDate: Long) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.DialogTheme,
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

    private fun filterTransactions() {
        filteredTransactions = transactions.filter { transaction ->
            val transactionDate = Calendar.getInstance().apply {
                timeInMillis = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault()).parse(transaction.date).time
            }
            transactionDate in selectedBgDate..selectedEndDate
        }.toMutableList()
    }

}



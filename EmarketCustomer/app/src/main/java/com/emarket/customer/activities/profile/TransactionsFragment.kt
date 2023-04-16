package com.emarket.customer.activities.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
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
    private var selectedBgDate: Calendar? = null
    private var selectedEndDate: Calendar? = null
    private lateinit var filteredTransactions: MutableList<Transaction>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTransactionsBinding.inflate(layoutInflater)

        filterTransactions()
        binding.rvTransactions.adapter = TransactionsListAdapter(filteredTransactions)
        if (transactions.isEmpty()) binding.tvNoTransactions.visibility = View.VISIBLE

        //TODO: change this to not let nothing after current time
        binding.dateBgLl.setOnClickListener {
            selectedBgDate = openDateDialog(binding.dateBgTv, selectedBgDate, maxDate = selectedEndDate?.timeInMillis)
            filterTransactions()
            (binding.rvTransactions.adapter as TransactionsListAdapter).notifyDataSetChanged()
        }
        binding.dateEndLl.setOnClickListener {
            selectedEndDate = openDateDialog(binding.dateEndTv, selectedEndDate, minDate = selectedBgDate?.timeInMillis)
            filterTransactions()
            (binding.rvTransactions.adapter as TransactionsListAdapter).notifyDataSetChanged()
        }
        
        return binding.root
    }

    private fun openDateDialog(dateView: TextView, selectedDate: Calendar?, minDate: Long? = null, maxDate: Long? = null) : Calendar {
        val calendar = selectedDate ?: Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.DialogTheme,
            { _, year, monthOfYear, dayOfMonth ->
                // Update the selected date when the user selects a date
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                // Update the text of your date input with the selected date
                dateView.text = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(calendar.time)
            },
            selectedDate?.get(Calendar.YEAR) ?: Calendar.getInstance().apply { timeInMillis = currentTime }.get(Calendar.YEAR),
            selectedDate?.get(Calendar.MONTH) ?: Calendar.getInstance().apply { timeInMillis = currentTime }.get(Calendar.MONTH),
            selectedDate?.get(Calendar.DAY_OF_MONTH) ?: Calendar.getInstance().apply { timeInMillis = currentTime }.get(Calendar.DAY_OF_MONTH)

        )
        // Show the date picker dialog
        minDate?.run { datePickerDialog.datePicker.minDate = minDate }
        datePickerDialog.datePicker.maxDate = maxDate ?: currentTime
        datePickerDialog.show()
        return calendar
    }

    private fun filterTransactions() {
        filteredTransactions = transactions.filter { transaction ->
            val transactionDate = Calendar.getInstance().apply {
                timeInMillis = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault()).parse(transaction.date).time
            }
            if (selectedBgDate == null && selectedEndDate == null) {
                true
            } else if (selectedBgDate == null && selectedEndDate != null) {
                transactionDate <= selectedEndDate
            } else if (selectedBgDate != null && selectedEndDate == null) {
                transactionDate >= selectedBgDate
            } else {
                transactionDate in selectedBgDate!!..selectedEndDate!!
            }
        }.toMutableList()
    }


}



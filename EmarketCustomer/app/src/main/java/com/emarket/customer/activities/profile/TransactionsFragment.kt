package com.emarket.customer.activities.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.Slide
import android.transition.TransitionManager
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
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

    companion object {
        const val IS_FILTER_VISIBLE = "IS_FILTER_VISIBLE"
        const val BG_DATE = "BG_DATE"
        const val END_DATE = "END_DATE"
    }

    private lateinit var binding: FragmentTransactionsBinding
    private val currentTime: Long = System.currentTimeMillis()
    private var selectedBgDate: Calendar? = null
    private var selectedEndDate: Calendar? = null
    private lateinit var filteredTransactions: MutableList<Transaction>
    private lateinit var adapter: TransactionsListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTransactionsBinding.inflate(layoutInflater)

        if (savedInstanceState != null) restoreSavedInstanceState(savedInstanceState)

        applyFilteredTransactions()

        binding.dateBgLl.setOnClickListener {
            openDateDialog(binding.dateBgTv, true, maxDate = selectedEndDate?.timeInMillis)
        }
        binding.dateEndLl.setOnClickListener {
            openDateDialog(binding.dateEndTv, false, minDate = selectedBgDate?.timeInMillis)
        }

        binding.filterBtn.setOnClickListener {
            if (binding.filterBb.visibility == View.INVISIBLE) {
                TransitionManager.beginDelayedTransition(binding.transactionsCl, AutoTransition())
                setBottomMargin(binding.transactionsCl, binding.filterBb.height)

                TransitionManager.beginDelayedTransition(binding.filterBb, Slide(Gravity.BOTTOM))
                binding.filterBb.visibility = View.VISIBLE
                if (selectedBgDate != null || selectedEndDate != null)
                    binding.filterBtn.setImageResource(R.drawable.filter_off)
            } else {
                selectedBgDate = null
                selectedEndDate = null
                binding.dateBgTv.text = ""
                binding.dateEndTv.text = ""
                applyFilteredTransactions()
                binding.filterBtn.setImageResource(R.drawable.filter)
            }
        }

        binding.colapseBtn.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.filterBb, Slide(Gravity.BOTTOM))
            binding.filterBb.visibility = View.INVISIBLE
            TransitionManager.beginDelayedTransition(binding.transactionsCl, AutoTransition())
            setBottomMargin(binding.transactionsCl, 0)
            binding.filterBtn.setImageResource(R.drawable.filter)
        }
        
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(IS_FILTER_VISIBLE, binding.filterBb.visibility == View.VISIBLE)
        selectedBgDate?.let { outState.putLong(BG_DATE, it.timeInMillis) }
        selectedEndDate?.let { outState.putLong(END_DATE, it.timeInMillis) }
        super.onSaveInstanceState(outState)
    }

    private fun restoreSavedInstanceState(savedInstanceState: Bundle) {
        if (savedInstanceState.getBoolean(IS_FILTER_VISIBLE)) {
            binding.filterBb.visibility =  View.VISIBLE
            binding.filterBb.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.filterBb.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val height = binding.filterBb.height
                    setBottomMargin(binding.transactionsCl, height)
                }
            })
        } else binding.filterBb.visibility = View.INVISIBLE

        selectedBgDate = restoreSelectedDate(savedInstanceState, BG_DATE, binding.dateBgTv)
        selectedEndDate = restoreSelectedDate(savedInstanceState, END_DATE, binding.dateEndTv)
    }

    private fun restoreSelectedDate(bundle: Bundle, key: String, textView: TextView) : Calendar? {
        val selectedDate = bundle.getLong(key).takeIf { it != 0L }?.let {
            Calendar.getInstance().apply {
                timeInMillis = it
            }
        }
        textView.text = selectedDate?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.US).format(it.time)
        } ?: ""
        return selectedDate
    }

    private fun openDateDialog(dateView: TextView, isBegin: Boolean, minDate: Long? = null, maxDate: Long? = null) : Calendar? {
        val selectedDate = if (isBegin) selectedBgDate else selectedEndDate
        val calendar = selectedDate ?: Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.DialogTheme,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                dateView.text = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(calendar.time)
                binding.filterBtn.setImageResource(R.drawable.filter_off)

                if (isBegin) selectedBgDate = calendar
                else selectedEndDate = calendar

                applyFilteredTransactions()
            },
            selectedDate?.get(Calendar.YEAR) ?: Calendar.getInstance().apply { timeInMillis = currentTime }.get(Calendar.YEAR),
            selectedDate?.get(Calendar.MONTH) ?: Calendar.getInstance().apply { timeInMillis = currentTime }.get(Calendar.MONTH),
            selectedDate?.get(Calendar.DAY_OF_MONTH) ?: Calendar.getInstance().apply { timeInMillis = currentTime }.get(Calendar.DAY_OF_MONTH)
        )
        minDate?.run { datePickerDialog.datePicker.minDate = minDate }
        datePickerDialog.datePicker.maxDate = maxDate ?: currentTime
        datePickerDialog.show()
        return calendar
    }

    private fun filterTransactions() {
        selectedBgDate?.run {
            this.set(Calendar.HOUR_OF_DAY, 0)
            this.set(Calendar.MINUTE, 0)
            this.set(Calendar.SECOND, 0)
        }
        selectedEndDate?.run {
            this.set(Calendar.HOUR_OF_DAY, 23)
            this.set(Calendar.MINUTE, 59)
            this.set(Calendar.SECOND, 59)
        }
        filteredTransactions = transactions.filter { transaction ->
            val transactionDate = Calendar.getInstance().apply {
                time = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault()).parse(transaction.date!!) as Date
            }
            if (selectedBgDate == null && selectedEndDate == null) {
                true
            } else if (selectedBgDate == null) {
                transactionDate <= selectedEndDate!!
            } else if (selectedEndDate == null) {
                transactionDate >= selectedBgDate!!
            } else {
                transactionDate >= selectedBgDate!! && transactionDate <= selectedEndDate!!
            }
        }.toMutableList()
    }

    private fun applyFilteredTransactions() {
        filterTransactions()
        adapter = TransactionsListAdapter(filteredTransactions)
        binding.rvTransactions.adapter = adapter
        if (filteredTransactions.isEmpty()) binding.tvNoTransactions.visibility = View.VISIBLE
        else binding.tvNoTransactions.visibility = View.GONE
    }

    private fun setBottomMargin(v: View, bottomMargin: Int) {
        if (v.layoutParams is MarginLayoutParams) {
            val p = v.layoutParams as MarginLayoutParams
            p.bottomMargin = bottomMargin
            v.requestLayout()
        }
    }


}



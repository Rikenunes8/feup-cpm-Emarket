package com.emarket.customer.activities.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.activities.transactions
import com.emarket.customer.controllers.TransactionsListAdapter
import com.emarket.customer.databinding.FragmentTransactionsBinding

/**
 * Use the [TransactionsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TransactionsFragment : Fragment() {
    private lateinit var binding: FragmentTransactionsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransactionsBinding.inflate(layoutInflater)

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            adapter = TransactionsListAdapter(transactions)
        }

        return binding.root
    }
}

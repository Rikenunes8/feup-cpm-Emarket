package com.emarket.customer.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.controllers.TransactionsListAdapter
import com.emarket.customer.databinding.FragmentTransactionsBinding
import com.emarket.customer.models.Product
import com.emarket.customer.models.Transaction
import com.emarket.customer.models.Voucher

/**
 * Use the [TransactionsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TransactionsFragment : Fragment() {


    // TODO remove this
    private val productItems : MutableList<Product> = mutableListOf(product1, product2)

    // TODO: Receive transactions from other side
    private var transactions = mutableListOf(
        Transaction(productItems, 0.0, null, 0.0, "2021-01-01"),
        Transaction(productItems, 2.37, Voucher("1", 15), 13.32, "2023-03-30"),
    )
    private lateinit var binding: FragmentTransactionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment TransactionsFragment.
         */
        @JvmStatic
        fun newInstance() =
            TransactionsFragment()
    }
}
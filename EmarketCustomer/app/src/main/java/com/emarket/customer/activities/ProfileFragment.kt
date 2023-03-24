package com.emarket.customer.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.R
import com.emarket.customer.controllers.VoucherListAdapter
import com.emarket.customer.databinding.FragmentProfileBinding
import com.emarket.customer.dialogs.EditDialogFragment
import com.emarket.customer.dialogs.EditDialogType
import com.emarket.customer.models.UserViewModel
import com.emarket.customer.models.Voucher

class ProfileFragment() : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    private val vouchers = mutableListOf(
        Voucher("1", 15),
        Voucher("2", 15),
        Voucher("3", 15),
        Voucher("4", 15),
        Voucher("5", 15)
    ) // TODO get this from other place

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)

        val user = UserViewModel(application = requireActivity().application).user!!

        binding.rvVoucher.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            adapter = VoucherListAdapter(vouchers)
        }

        binding.nameTv.text = user.name
        binding.nicknameTv.text = user.nickname
        binding.cardNoTv.text = user.cardNumber

        binding.editPersonalBtn.setOnClickListener {
            val fragment = EditDialogFragment.newInstance(user.name,
                { name ->
                    user.name = name
                    UserViewModel(requireActivity().application).user = user
                    binding.nameTv.text = user.name
                },
                EditDialogType.PERSONAL)

            fragment.show(requireActivity().supportFragmentManager, "EditPersonalDialogFragment")
        }

        binding.editPaymentBtn.setOnClickListener {
            val fragment = EditDialogFragment.newInstance(user.cardNumber,
                { cardNumber ->
                    user.cardNumber = cardNumber
                    UserViewModel(requireActivity().application).user = user
                    binding.cardNoTv.text = user.cardNumber
                },
                EditDialogType.PAYMENT)

            fragment.show(requireActivity().supportFragmentManager, "EditPaymentDialogFragment")
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}

package com.emarket.customer.activities.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.emarket.customer.Constants
import com.emarket.customer.DataSigned
import com.emarket.customer.R
import com.emarket.customer.Utils
import com.emarket.customer.Utils.showToast
import com.emarket.customer.controllers.Fetcher.Companion.vouchers
import com.emarket.customer.controllers.adapters.VoucherListAdapter
import com.emarket.customer.databinding.FragmentProfileBinding
import com.emarket.customer.dialogs.EditDialogFragment
import com.emarket.customer.dialogs.EditDialogType
import com.emarket.customer.models.User
import com.emarket.customer.models.UserViewModel
import com.emarket.customer.services.NetworkService
import com.emarket.customer.services.RequestType
import com.google.gson.Gson
import kotlin.concurrent.thread

data class UserInfo (
    val uuid: String,
    val name: String? = null,
    val cardNumber: String? = null,
)

data class UserResponse (
    val error: String?,
)

class ProfileFragment() : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        binding.rvVoucher.adapter = VoucherListAdapter(vouchers)

        if (vouchers.isEmpty()) {
            binding.rvVoucher.visibility = View.GONE
            binding.noVouchersView.visibility = View.VISIBLE
        }

        val user = UserViewModel(requireActivity().application).user!!
        binding.nameTv.text = user.name
        binding.nicknameTv.text = user.nickname
        binding.totalSpentTv.text = getString(R.string.template_price, user.totalSpent)
        binding.toDiscountTv.text = getString(R.string.template_price, user.amountToDiscount)
        binding.cardNoTv.text = user.cardNumber

        binding.editPersonalBtn.setOnClickListener {
            val fragment = EditDialogFragment.newInstance(
                user.name,
                { name -> updateUserInfo(user, name =  name) },
                EditDialogType.PERSONAL)

            fragment.show(requireActivity().supportFragmentManager, "EditPersonalDialogFragment")
        }

        binding.editPaymentBtn.setOnClickListener {
            val fragment = EditDialogFragment.newInstance(
                user.cardNumber,
                { cardNumber -> updateUserInfo(user, cardNumber = cardNumber) },
                EditDialogType.PAYMENT
            )

            fragment.show(requireActivity().supportFragmentManager, "EditPaymentDialogFragment")
        }

        return binding.root
    }

    private fun updateUserInfo(user: User, name: String? = null, cardNumber: String? = null) {
        thread(start = true) {
            try {
                val endpoint = Constants.SERVER_URL + Constants.USER_ENDPOINT

                val userInfoJSON = Gson().toJson(UserInfo(user.userId, name=name, cardNumber=cardNumber))
                val signature = Utils.getSignature(userInfoJSON)
                val requestData = Gson().toJson(DataSigned(signature, userInfoJSON))
                val response = NetworkService.makeRequest(RequestType.POST, endpoint, requestData)

                val userResponse = Gson().fromJson(response, UserResponse::class.java)
                if (userResponse.error != null) throw Exception()

                user.name = name ?: user.name
                user.cardNumber = cardNumber ?: user.cardNumber
                UserViewModel(requireActivity().application).user = user

                activity?.runOnUiThread {
                    name?.run {
                        showToast(requireActivity(), getString(R.string.success_edit_personal))
                        binding.nameTv.text = user.name
                    }
                    cardNumber?.run {
                        showToast(requireActivity(), getString(R.string.success_edit_payment))
                        binding.cardNoTv.text = user.cardNumber
                    }
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    name?.run { showToast(requireActivity(),getString(R.string.error_updating_user_name)) }
                    cardNumber?.run { showToast(requireActivity(),getString(R.string.error_updating_user_cardNo)) }
                }
            }
        }
    }
}

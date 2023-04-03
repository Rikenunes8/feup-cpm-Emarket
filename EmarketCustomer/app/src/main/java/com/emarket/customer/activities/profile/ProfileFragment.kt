package com.emarket.customer.activities.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emarket.customer.Constants
import com.emarket.customer.DataSigned
import com.emarket.customer.R
import com.emarket.customer.Utils
import com.emarket.customer.Utils.showToast
import com.emarket.customer.activities.vouchers
import com.emarket.customer.controllers.VoucherListAdapter
import com.emarket.customer.databinding.FragmentProfileBinding
import com.emarket.customer.dialogs.EditDialogFragment
import com.emarket.customer.dialogs.EditDialogType
import com.emarket.customer.models.User
import com.emarket.customer.models.UserViewModel
import com.emarket.customer.services.NetworkService
import com.emarket.customer.services.RequestType
import com.google.gson.Gson
import kotlin.concurrent.thread

data class UserCard (
    val id: String,
    val cardNumber: String,
)

data class UserResponse (
    val error: String?,
)

class ProfileFragment() : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)

        binding.rvVoucher.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            adapter = VoucherListAdapter(vouchers)
        }

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
            val fragment = EditDialogFragment.newInstance(user.name,
                { name ->
                    user.name = name
                    UserViewModel(requireActivity().application).user = user
                    binding.nameTv.text = user.name
                    showToast(requireActivity(), getString(R.string.success_edit_personal))
                },
                EditDialogType.PERSONAL)

            fragment.show(requireActivity().supportFragmentManager, "EditPersonalDialogFragment")
        }

        binding.editPaymentBtn.setOnClickListener {
            val fragment = EditDialogFragment.newInstance(
                user.cardNumber,
                { cardNumber -> updateUserCard(user, cardNumber) },
                EditDialogType.PAYMENT
            )

            fragment.show(requireActivity().supportFragmentManager, "EditPaymentDialogFragment")
        }

        return binding.root
    }

    /**
     * Updates the user's card number by sending a POST request to the server
     * and updating the UI.
     * @param user the user to update
     */
    private fun updateUserCard(user: User, cardNumber: String) {
        thread(start = true) {
            try {
                val endpoint = Constants.SERVER_URL + Constants.USER_ENDPOINT

                val userCardJSON = Gson().toJson(UserCard(user.userId, cardNumber))
                val signature = Utils.getSignature(userCardJSON)
                val requestData = Gson().toJson(DataSigned(signature, userCardJSON))
                val response = NetworkService.makeRequest(RequestType.POST, endpoint, requestData)

                val userResponse = Gson().fromJson(response, UserResponse::class.java)
                if (userResponse.error != null) throw Exception()

                user.cardNumber = cardNumber
                UserViewModel(requireActivity().application).user = user

                activity?.runOnUiThread {
                    showToast(requireActivity(), getString(R.string.success_edit_payment))
                    binding.cardNoTv.text = user.cardNumber
                }
            } catch (e: Exception) {
                activity?.runOnUiThread { showToast(requireActivity(),getString(R.string.error_updating_user_cardNo)) }
            }

        }
    }

}

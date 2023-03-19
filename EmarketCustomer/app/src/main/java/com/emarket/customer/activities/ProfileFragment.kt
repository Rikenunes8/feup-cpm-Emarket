package com.emarket.customer.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.emarket.customer.databinding.FragmentProfileBinding
import com.emarket.customer.dialogs.EditPersonalDialogFragment
import com.emarket.customer.models.UserViewModel

class ProfileFragment() : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)

        val user = UserViewModel(application = requireActivity().application).user

        binding.nameTv.text = user.name
        binding.nicknameTv.text = user.nickname

        binding.editPersonalBtn.setOnClickListener {
            val fragment = EditPersonalDialogFragment.newInstance(user.name) { name ->
                user.name = name
                UserViewModel(requireActivity().application).user = user
                binding.nameTv.text = user.name
            }
            fragment.show(requireActivity().supportFragmentManager, "EditPersonalDialogFragment")
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}

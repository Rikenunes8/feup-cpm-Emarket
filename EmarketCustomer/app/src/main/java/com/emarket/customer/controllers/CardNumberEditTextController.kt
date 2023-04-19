package com.emarket.customer.controllers

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher

class CardNumberEditTextController : TextWatcher {
    companion object {
        private const val SEPARATOR = "-"
        private const val DIGIT_NUMBER = 16
        private const val GROUP_SIZE = 4
    }

    private var current = ""
    private val nonDigits = Regex("\\D")

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable) {
        if (s.toString() == current) return
        val userInput = s.toString().replace(nonDigits, "")
        if (userInput.length <= DIGIT_NUMBER) {
            current = userInput.chunked(GROUP_SIZE).joinToString(SEPARATOR)
            s.filters = arrayOfNulls<InputFilter>(0)
        }
        s.replace(0, s.length, current, 0, current.length)
    }
}

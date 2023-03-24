package com.emarket.customer.models

import java.time.LocalDate

data class Transaction (
    var products : MutableList<Product> = mutableListOf(),
    var discounted : Double = 0.0,
    var voucher : Voucher? = null,
    var total : Double = 0.0, // after discount
    var date : LocalDate = LocalDate.now()
)
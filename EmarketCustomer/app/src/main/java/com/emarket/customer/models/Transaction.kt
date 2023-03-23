package com.emarket.customer.models

import java.time.LocalDate

data class Transaction (
    var products : MutableList<Product> = mutableListOf(),
    var discounted : Double = 0.0,
    var voucher : Voucher? = null,
    // var date : LocalDate // TODO nice to have
)
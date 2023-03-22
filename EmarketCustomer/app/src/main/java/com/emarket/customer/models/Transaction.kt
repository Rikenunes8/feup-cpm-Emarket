package com.emarket.customer.models

import java.time.LocalDate

data class Transaction (
    var products : MutableList<Product>,
    var discounted : Double,
    var voucher : Voucher?,
    // var date : LocalDate // TODO nice to have
)
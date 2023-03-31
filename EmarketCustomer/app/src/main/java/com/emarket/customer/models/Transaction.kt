package com.emarket.customer.models

data class Transaction (
    var products : MutableList<Product> = mutableListOf(),
    var discounted : Double? = null,
    var voucher : Voucher? = null,
    var total : Double = 0.0,
    var date : String? = null
)

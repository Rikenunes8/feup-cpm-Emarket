package com.emarket.customer.models

data class ProductDTO (
    var uuid: String,
    var name: String,
    var price: Double,
)

data class Product (
    var imgRes: Int?,
    var uuid: String,
    var name: String,
    var price: Double,
    var qnt: Int = 1
)


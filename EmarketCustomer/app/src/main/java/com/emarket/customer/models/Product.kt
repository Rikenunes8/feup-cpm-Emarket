package com.emarket.customer.models

/**
 * Product with the same structure saved in server's database
 */
data class ProductDTO (
    var uuid: String,
    var name: String,
    var price: Double,
)

/**
 * Complete product of a transaction
 */
data class Product (
    var imgRes: Int?,
    var uuid: String,
    var name: String,
    var price: Double,
    var quantity: Int = 1
)

/**
 * Product sent inside a checkout
 */
data class ProductToCheckout(
    val uuid: String,
    val price: Double,
    val quantity: Int
)


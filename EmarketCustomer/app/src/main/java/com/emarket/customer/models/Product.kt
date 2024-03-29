package com.emarket.customer.models

/**
 * Product with the same structure saved in server's database
 */
data class ProductDTO (
    var uuid: String,
    var name: String,
    var price: Double,
    var url: String? = null,
)

/**
 * Complete product of a transaction
 */
data class Product (
    var uuid: String,
    var name: String,
    var price: Double,
    var url: String? = null,
    var quantity: Int = 1
)

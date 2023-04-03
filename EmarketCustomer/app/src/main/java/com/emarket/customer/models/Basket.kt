package com.emarket.customer.models

/**
 * Basket sent to the server on checkout
 */
class Basket (
    val products : List<ProductToCheckout>,
    val toDiscount : Boolean,
    val voucher : String?, // voucher id
)
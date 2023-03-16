package com.emarket.customer.models

data class User(
    val userId: String,
    val name: String,
    val nickname: String,
    val password: String,
    val cardNumber: String
)
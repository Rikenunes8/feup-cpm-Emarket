package com.emarket.customer.Models

data class User(
    val userId: String,
    val name: String,
    val nickname: String,
    val password: String,
    val cardNumber: String
)
package com.example.myapplication

data class User(
    val email: String,
    val password: String,
    var autoLogin: Boolean = false
)
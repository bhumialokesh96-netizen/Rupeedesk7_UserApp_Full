package com.rupeedesk7.userapp.data

data class UserModel(
    val phone: String = "",
    val name: String? = null,
    val balance: Double = 0.0,
    val dailySent: Long = 0,
    val dailyLimit: Long = 50,
    val spins: Long = 0,
    val referredBy: String? = null,
    val simId: Int = -1
)

package ru.netology.chat.model

data class User(
    val id: Long,
    val photo: String,
    val name: String,
    val company: String
)
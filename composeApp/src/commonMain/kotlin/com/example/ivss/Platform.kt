package com.example.ivss

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
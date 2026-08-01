package com.melashkov.mcparking

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
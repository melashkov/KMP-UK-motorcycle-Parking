package com.melashkov.mcparking.di

import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [AppModule::class]
)
class KoinApp
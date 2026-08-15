package com.melashkov.mcparking.domain.usecases

import com.melashkov.mcparking.domain.entity.MapViewport
import org.koin.core.annotation.Singleton

@Singleton
class ShouldShowSearchThisAreaUseCase {

    operator fun invoke(
        viewport: MapViewport,
        lastSearchedViewport: MapViewport?,
    ): Boolean {
        println("ShouldShowSearchThisAreaUseCase, $viewport")
        if (viewport.zoom < 12.0) {
            return false
        }

        if (lastSearchedViewport == null) {
            return true
        }

        return viewport != lastSearchedViewport
    }
}
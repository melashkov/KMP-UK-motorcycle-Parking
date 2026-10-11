package com.melashkov.mcparking.domain.usecases

import com.melashkov.mcparking.domain.entity.MapViewport
import org.koin.core.annotation.Singleton

@Singleton
class ShouldShowSearchThisAreaUseCase {

    operator fun invoke(
        viewport: MapViewport,
        lastSearchedViewport: MapViewport?,
    ): Boolean {
        if (viewport.zoom < SearchParkingBaysUseCase.MIN_SEARCH_ZOOM) {
            return false
        }

        if (lastSearchedViewport == null) {
            return true
        }

        return viewport != lastSearchedViewport
    }
}

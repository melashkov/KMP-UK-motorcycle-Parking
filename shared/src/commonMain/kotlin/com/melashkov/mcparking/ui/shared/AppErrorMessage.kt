package com.melashkov.mcparking.ui.shared

import androidx.compose.runtime.Composable
import com.melashkov.mcparking.domain.interfaces.AppError
import org.jetbrains.compose.resources.stringResource
import ukmotorcycleparking.shared.generated.resources.Res
import ukmotorcycleparking.shared.generated.resources.error_connection_failed
import ukmotorcycleparking.shared.generated.resources.error_forbidden
import ukmotorcycleparking.shared.generated.resources.error_rate_limited
import ukmotorcycleparking.shared.generated.resources.error_server_unavailable
import ukmotorcycleparking.shared.generated.resources.error_unauthorized
import ukmotorcycleparking.shared.generated.resources.error_unknown

@Composable
internal fun AppError.localizedMessage(): String =
    when (this) {
        AppError.ConnectionFailed -> stringResource(Res.string.error_connection_failed)
        AppError.Unauthorized -> stringResource(Res.string.error_unauthorized)
        AppError.Forbidden -> stringResource(Res.string.error_forbidden)
        AppError.RateLimited -> stringResource(Res.string.error_rate_limited)
        AppError.ServerUnavailable -> stringResource(Res.string.error_server_unavailable)
        is AppError.ServerError -> message
        AppError.Unknown -> stringResource(Res.string.error_unknown)
    }

package com.melashkov.mcparking.ui.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.Singleton

internal sealed interface AppCommand {
    data class Navigate(val route: AppRoute) : AppCommand
    data object Back : AppCommand
    data class Launch(val action: ExternalAction) : AppCommand
}

@Singleton
class AppNavigationSink {
    private val commandsChannel = Channel<AppCommand>(Channel.BUFFERED)

    internal val commands = commandsChannel.receiveAsFlow()

    fun navigate(route: AppRoute) {
        commandsChannel.trySend(AppCommand.Navigate(route))
    }

    fun back() {
        commandsChannel.trySend(AppCommand.Back)
    }

    fun launch(action: ExternalAction) {
        commandsChannel.trySend(AppCommand.Launch(action))
    }
}

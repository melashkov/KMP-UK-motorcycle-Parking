package com.melashkov.mcparking.di

import com.melashkov.mcparking.data.preferences.PreferenceNamespace
import com.melashkov.mcparking.data.preferences.platformPreferenceStore
import com.melashkov.mcparking.ui.support.DeveloperMessagePreferences
import com.melashkov.mcparking.ui.theme.ThemePreferences
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import org.koin.core.scope.Scope

@Module
class PreferenceModule {
    @Singleton
    internal fun provideThemePreferences(scope: Scope): ThemePreferences =
        ThemePreferences(platformPreferenceStore(scope, PreferenceNamespace.Appearance))

    @Singleton
    internal fun provideDeveloperMessagePreferences(scope: Scope): DeveloperMessagePreferences =
        DeveloperMessagePreferences(platformPreferenceStore(scope, PreferenceNamespace.DeveloperMessage))

    @Singleton
    internal fun provideDebugApiPreferences(scope: Scope): DebugApiPreferences =
        DebugApiPreferences(
            platformPreferenceStore(scope, PreferenceNamespace.DebugApi),
            canSelectLocal = platformIsDebugBuild(scope) && platformSupportsLocalBackend(),
        )
}

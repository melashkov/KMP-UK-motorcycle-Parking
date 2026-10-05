package com.melashkov.mcparking.di

import android.content.Context
import android.os.Build
import com.melashkov.mcparking.shared.R
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.scope.Scope
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal actual fun platformHttpClient(scope: Scope, apiUrlProvider: ApiUrlProvider): HttpClient {
    // Android 6 and early Android 7 lack ISRG Root X1 in their system trust store.
    // Add the official root only for the production API client on these versions.
    val legacyTrustManager = if (
        Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1 &&
        apiUrlProvider.baseUrl == apiUrlProvider(ApiEnvironment.Production).baseUrl
    ) {
        legacyApiTrustManager(scope.get())
    } else {
        null
    }
    return HttpClient(OkHttp) {
        if (legacyTrustManager != null) {
            val sslContext = SSLContext.getInstance("TLS").apply {
                init(null, arrayOf(legacyTrustManager), null)
            }
            engine {
                config {
                    sslSocketFactory(sslContext.socketFactory, legacyTrustManager)
                }
            }
        }
    }
}

internal fun legacyApiTrustManager(context: Context): X509TrustManager {
    val systemFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    systemFactory.init(null as KeyStore?)
    val systemTrustManager = systemFactory.trustManagers.filterIsInstance<X509TrustManager>().single()
    val trustedRoots = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
        load(null, null)
        systemTrustManager.acceptedIssuers.forEachIndexed { index, certificate ->
            setCertificateEntry("system-$index", certificate)
        }
        context.resources.openRawResource(R.raw.isrg_root_x1).use { input ->
            setCertificateEntry("isrg-root-x1", CertificateFactory.getInstance("X.509").generateCertificate(input))
        }
    }
    val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    factory.init(trustedRoots)
    return factory.trustManagers.filterIsInstance<X509TrustManager>().single()
}

package me.rafaelldi.dotnet.warehouse.receivingHub

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

@Service
internal class ReceivingHub {
    companion object {
        fun getInstance(): ReceivingHub = service()

        private const val DOTNET_FEED_URL =
            "https://builds.dotnet.microsoft.com/dotnet/release-metadata/releases-index.json"

        private val LOG = logger<ReceivingHub>()
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun receiveDotnetReleaseIndex(): DotnetReleaseIndex? {
        val response = client.get(DOTNET_FEED_URL)
        if (response.status.value !in 200..299) {
            LOG.warn("Failed to receive dotnet release index. Status: ${response.status}")
            return null
        }

        val releaseIndex: DotnetReleaseIndex = response.body()
        LOG.trace { "Received dotnet release index: $releaseIndex" }

        return releaseIndex
    }
}
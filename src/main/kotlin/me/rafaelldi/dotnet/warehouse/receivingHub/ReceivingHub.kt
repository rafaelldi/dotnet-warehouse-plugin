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

    suspend fun receiveInboundCargo(model: InboundCargoModel) {
        val index = receiveDotnetReleaseIndex()
        if (index == null) {
            LOG.warn("Failed to receive dotnet release index")
            return
        }

        val releaseVersion = index.versions.firstOrNull { it.channelVersion == model.version.channelVersion }
        if (releaseVersion == null) {
            LOG.warn("Failed to find release index for version: ${model.version}")
            return
        }

        val versionIndex = receiveDotnetReleaseVersionIndex(releaseVersion.releasesJson)
        if (versionIndex == null) {
            LOG.warn("Failed to receive dotnet release version index for version: ${model.version}")
            return
        }

        val latestRelease = versionIndex.releases.firstOrNull { it.releaseVersion == releaseVersion.latestRelease }
        if (latestRelease == null) {
            LOG.warn("Failed to find latest release for version: ${model.version}")
            return
        }

        val filesToDownload = when (model.type) {
            InboundCargoType.Sdk -> latestRelease.sdk.files
            InboundCargoType.Runtime -> latestRelease.runtime.files
            InboundCargoType.AspNetRuntime -> latestRelease.aspNetCoreRuntime.files
        }
        val fileNameToDownload = buildString {
            append(model.type.fileNamePrefix)
            append("-")
            append(model.rid.fileNameSuffix)
            append(model.rid.fileExtension)
        }
        LOG.trace { "File to download: $fileNameToDownload" }

        val fileToDownload = filesToDownload.firstOrNull { it.name == fileNameToDownload }
        if (fileToDownload == null) {
            LOG.warn("Failed to find file to download for version: ${model.version}")
            return
        }

        downloadFile(fileToDownload)
    }

    private suspend fun receiveDotnetReleaseIndex(): DotnetReleaseIndex? {
        val response = client.get(DOTNET_FEED_URL)
        if (response.status.value !in 200..299) {
            LOG.warn("Failed to receive dotnet release index. Status: ${response.status}")
            return null
        }

        val releaseIndex: DotnetReleaseIndex = response.body()
        LOG.trace { "Received dotnet release index: $releaseIndex" }

        return releaseIndex
    }

    private suspend fun receiveDotnetReleaseVersionIndex(url: String): DotnetReleaseVersionIndex? {
        val response = client.get(url)
        if (response.status.value !in 200..299) {
            LOG.warn("Failed to receive dotnet release version index. Status: ${response.status}")
            return null
        }

        val releaseVersionIndex: DotnetReleaseVersionIndex = response.body()
        LOG.trace { "Received dotnet release index: $releaseVersionIndex" }

        return releaseVersionIndex
    }

    private suspend fun downloadFile(dotnetReleaseFile: DotnetReleaseFile) {

    }
}
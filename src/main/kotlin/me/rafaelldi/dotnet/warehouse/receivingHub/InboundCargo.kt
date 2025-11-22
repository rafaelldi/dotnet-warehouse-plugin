package me.rafaelldi.dotnet.warehouse.receivingHub

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class InboundCargo(
    @SerialName("channel-version")
    val channelVersion: String,
    @SerialName("latest-release")
    val latestRelease: String,
    @SerialName("latest-release-date")
    val latestReleaseDate: String,
    @SerialName("latest-runtime")
    val latestRuntime: String,
    @SerialName("latest-sdk")
    val latestSdk: String,
    @SerialName("release-type")
    val releaseType: String,
    @SerialName("releases.json")
    val releaseJson: String
)

@Serializable
internal data class InboundCargoIndex(
    @SerialName("releases-index")
    val items: List<InboundCargo>
)
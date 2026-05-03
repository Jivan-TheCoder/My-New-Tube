package com.playtube.protube.video.music.local.subscription.workers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.playtube.protube.video.music.BuildConfig

@Serializable
class SubscriptionData(
    val subscriptions: List<SubscriptionItem>
) {
    @SerialName("app_version")
    private val appVersion = BuildConfig.VERSION_NAME

    @SerialName("app_version_int")
    private val appVersionInt = BuildConfig.VERSION_CODE
}

@Serializable
data class SubscriptionItem(
    @SerialName("service_id")
    val serviceId: Int,
    val url: String,
    val name: String
)

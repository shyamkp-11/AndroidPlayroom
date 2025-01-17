package com.shyampatel.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkFcmEnabled(
    @SerialName("fcmEnabled")val fcmEnabled: Boolean,
)
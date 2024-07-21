package com.shyampatel.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkAccessTokenModel(
    @SerialName("access_token") val accessToken: String,
)
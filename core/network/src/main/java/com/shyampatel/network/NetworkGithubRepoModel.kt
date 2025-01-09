package com.shyampatel.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkGithubRepoModel(
    @SerialName("id")
    val serverId: Long,
    val name: String = "",
    @SerialName("full_name") val fullName: String = "",
    @SerialName("stargazers_count")val stars: Int = 0,
    @SerialName("html_url")val htmlUrl: String = "",
    val private : Boolean,
    val owner: NetworkRepoOwner,
    val description: String?,
    val language: String?
)

@Serializable
data class NetworkRepoOwner(
    val login: String,
    @SerialName("id")
    val serverId: Long,
    @SerialName("avatar_url") val avatarUrl: String,
    @SerialName("html_url") val htmlUrl: String,
    val type: String,
    val company: String? = null,
    val name: String? = null, )

@Serializable
data class NetworkGithubRepoWrapper(
    val items:List<NetworkGithubRepoModel>
)
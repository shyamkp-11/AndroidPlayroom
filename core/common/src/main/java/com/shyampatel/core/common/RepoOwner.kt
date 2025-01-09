package com.shyampatel.core.common

data class RepoOwner(
    val serverId: String,
    val login: String,
    val name: String?,
    val company: String?,
    val avatarUrl: String,
    val htmlUrl: String,
    val type: RepoOwnerType
)
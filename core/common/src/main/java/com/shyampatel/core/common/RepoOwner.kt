package com.shyampatel.core.common

data class RepoOwner(
    val id: Long,
    val login: String,
    val name: String?,
    val company: String?,
    val avatarUrl: String,
    val htmlUrl: String,
    val type: RepoOwnerType
)
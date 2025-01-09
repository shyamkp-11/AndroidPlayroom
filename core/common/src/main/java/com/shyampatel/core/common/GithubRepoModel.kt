package com.shyampatel.core.common

data class GithubRepoModel(
    val serverId: String,
    val name: String,
    val fullName: String,
    val description: String?,
    val stars: Int,
    val htmlUrl: String,
    val ownerId: String,
    val ownerLogin: String,
    val private: Boolean,
    val ownerAvatarUrl: String,
    val ownerType: RepoOwnerType,
    val language: String?,
)
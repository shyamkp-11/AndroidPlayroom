package com.shyampatel.network.graphql

import com.shyampatel.core.common.GithubRepoModel
import com.shyampatel.core.common.RepoOwnerType
import com.shyampatel.network.graphql.fragment.Repository
import java.util.Locale

fun Repository.asGithubRepoEntity() = GithubRepoModel(
    serverId = id,
    name = name,
    fullName = nameWithOwner,
    stars = stargazers.totalCount,
    htmlUrl = url.toString(),
    private = isPrivate,
    ownerId = owner.id,
    ownerLogin = owner.login,
    ownerAvatarUrl = owner.avatarUrl.toString(),
    ownerType = enumValueOf<RepoOwnerType>(owner.__typename.uppercase(Locale.getDefault())),
    description = description,
    language = primaryLanguage?.name ?: "",
)


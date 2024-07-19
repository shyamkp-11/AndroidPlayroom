package com.shyampatel.core.data

import com.shyampatel.core.common.GithubRepoModel
import com.shyampatel.core.common.RepoOwner
import com.shyampatel.core.common.RepoOwnerType
import com.shyampatel.database.GithubRepoEntity
import com.shyampatel.database.RepoOwnerEntity
import com.shyampatel.network.NetworkGithubRepoModel
import com.shyampatel.network.NetworkRepoOwner
import java.util.Locale

fun NetworkGithubRepoModel.asGithubRepoEntity() = GithubRepoEntity(
    id = id,
    name = name,
    fullName = fullName,
    stars = stars,
    htmlUrl = htmlUrl,
    private = private,
    ownerId = owner.id,
    ownerLogin = owner.login,
    ownerAvtarUrl = owner.avatarUrl,
    ownerType = enumValueOf<RepoOwnerType>(owner.type.uppercase(Locale.getDefault())),
    description = description,
    language = language,
)

fun NetworkGithubRepoModel.asRepoOwnerEntity() = RepoOwnerEntity(
    id = owner.id,
    login = owner.login,
    htmlUrl = owner.htmlUrl,
    type =  enumValueOf<RepoOwnerType>(owner.type.uppercase(Locale.getDefault())),
    avatarUrl = owner.avatarUrl,
    name = owner.name,
    company = owner.company,
)

fun NetworkRepoOwner.asRepoOwnerEntity() = RepoOwnerEntity(
    id = id,
    login = login,
    htmlUrl = htmlUrl,
    type =  enumValueOf<RepoOwnerType>(type.uppercase(Locale.getDefault())),
    avatarUrl = avatarUrl,
    name = name,
    company = company,
)

fun GithubRepoEntity.asGithubRepoModel() = GithubRepoModel(
    id = id,
    name = name,
    fullName = fullName,
    stars = stars,
    htmlUrl = htmlUrl,
    private = private,
    ownerId = ownerId,
    ownerLogin = ownerLogin,
    ownerAvatarUrl = ownerAvtarUrl,
    ownerType = ownerType,
    description = description,
    language = language,
)

fun RepoOwnerEntity.asRepoOwner() = RepoOwner(
    id = id,
    login = login,
    htmlUrl = htmlUrl,
    type =  type,
    avatarUrl = avatarUrl,
    name = name,
    company = company
)
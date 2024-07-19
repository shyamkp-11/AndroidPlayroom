package com.shyampatel.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shyampatel.core.common.RepoOwnerType

@Entity(
    tableName = "github_repo",
)
data class GithubRepoEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val fullName: String,
    val stars: Int,
    val htmlUrl: String,
    val private: Boolean,
    val ownerId: Long,
    val ownerLogin: String,
    @ColumnInfo("owner_avatar_url") val ownerAvtarUrl: String?,
    val ownerType: RepoOwnerType,
    val description: String?,
    val language: String?
)
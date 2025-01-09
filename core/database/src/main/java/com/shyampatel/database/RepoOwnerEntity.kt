package com.shyampatel.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shyampatel.core.common.RepoOwnerType

@Entity(
    tableName = "repo_owner"
)
data class RepoOwnerEntity(
    @PrimaryKey
    val serverId: String,
    val login: String,
    @ColumnInfo("avatar_url") val avatarUrl: String,
    @ColumnInfo("html_url") val htmlUrl: String,
    val type: RepoOwnerType,
    val name: String?,
    val company: String?
)

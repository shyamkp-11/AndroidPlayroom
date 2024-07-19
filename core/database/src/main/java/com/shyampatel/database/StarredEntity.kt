package com.shyampatel.database

import androidx.room.Entity

@Entity(
    tableName = "repo_starred",
    primaryKeys = ["userId", "repoId"]
)
data class StarredEntity(
    val userId: Long,
    val repoId: Long
)
package com.shyampatel.database.util

import androidx.room.TypeConverter
import com.shyampatel.core.common.RepoOwnerType

class RepoOwnerTypeConverter {
    @TypeConverter
    fun toRepoOwnerType(value: String) = enumValueOf<RepoOwnerType>(value)

    @TypeConverter
    fun fromRepoOwnerType(value: RepoOwnerType) = value.name
}
package com.shyampatel.core.common.geofence

import java.util.Date

data class Geofence(val id: Long,
                    val name: String,
                    val latLong: LatLong,
                    val radius: Double,
                    val createdAt: Date,
                    val activatedAt: Date?,
                    val bitmapFilePath: String?,
                    val modifiedAt: Date)

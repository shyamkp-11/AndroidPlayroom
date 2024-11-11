package com.shyampatel.datastore.geofence

import android.graphics.Bitmap
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class GeofenceFileDaoImpl(private val filesDirAbsPath: String): GeofenceFileDao {
    private val geofenceImagesDir by lazy { File("$filesDirAbsPath/geofence_images") }
    override suspend fun <T> saveGeofenceImage(image: T, id: Long): Result<String> =
        if (image is Bitmap) {
            try {
                val dest = File("${geofenceImagesDir.absolutePath}/$id.png")
                if (!geofenceImagesDir.exists()) {
                    geofenceImagesDir.mkdirs()
                }
                FileOutputStream(dest).use { out ->
                    image.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        out
                    )
                    Result.success(dest.absolutePath)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("Invalid image type"))
        }


    override suspend fun <T> getGeofenceImage(id: Long): Result<File>
    {
        val file = File("${geofenceImagesDir.absolutePath}/$id.png")
        if (file.exists()) {
            return Result.success(file)
        }
        return Result.failure(FileNotFoundException())
    }

    override suspend fun deleteGeofenceImage(id: Long): Result<Unit> {
        val file = File("${geofenceImagesDir.absolutePath}/$id.png")
        if (file.exists()) {
            file.delete()
            return Result.success(Unit)
        }
        return Result.failure(FileNotFoundException())
    }
}
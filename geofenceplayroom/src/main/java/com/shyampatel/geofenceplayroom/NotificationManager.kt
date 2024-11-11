package com.shyampatel.geofenceplayroom

import android.Manifest
import android.app.NotificationChannel
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.shyampatel.core.common.geofence.GeofenceEvent
import com.shyampatel.core.common.geofence.GeofenceLog
import com.shyampatel.core.data.geofence.GeofenceRepository
import com.shyampatel.geofenceplayroom.navigation.uri
import kotlinx.coroutines.flow.first

class NotificationManager(val geofencesRepository: GeofenceRepository, val context: Context) {

    private val geofenceNotificationChannelId = "GEOFENCE_CHANNEL_ID"

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(geofenceNotificationChannelId, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    suspend fun showGeofenceNotification(geofencelog: GeofenceLog, event: GeofenceEvent, rowid: Long) {

        val geofence = geofencesRepository.getGeofence(geofencelog.id).first().getOrNull() ?: return
        var builder = NotificationCompat.Builder(context, geofenceNotificationChannelId)
            .setSmallIcon(R.drawable.thumbtack)
            .setContentTitle(geofence.name)
            .setContentText(event.name)
            .setContentIntent(
                Intent(
                    Intent.ACTION_VIEW,
                    "$uri/geofences/${geofence.id}".toUri(),
                    context,
                    MainActivity::class.java
                ).let {
                    TaskStackBuilder.create(context).run {
                        addNextIntentWithParentStack(it)
                        getPendingIntent(0, PendingIntent.FLAG_MUTABLE)
                    }
                }
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(rowid.toInt(), builder.build())
    }
}
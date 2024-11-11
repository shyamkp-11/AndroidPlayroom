package com.shyampatel.geofenceplayroom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.shyampatel.core.common.geofence.GeofenceEvent
import com.shyampatel.core.common.geofence.GeofenceLog
import com.shyampatel.core.data.geofence.GeofenceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val myhelper: MyHelper by lazy { MyHelper() }
    private val TAG = "GeofenceReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        myhelper.onReceive(context, intent)
    }

    private inner class MyHelper : KoinComponent {

        val repository: GeofenceRepository by inject()
        val notificationManager: NotificationManager by inject()
        val coroutineScope: CoroutineScope by inject()
        val ioDispatcher: CoroutineDispatcher by inject(named("IO"))

        fun onReceive(context: Context?, intent: Intent?) {
            val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) } ?: return

            if (geofencingEvent.hasError()) {
                val errorMessage =
                    GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
                Log.e(TAG, "onReceive: $errorMessage")
                return
            }
            val event = geofencingTransitionToGeofenceEvent(geofencingEvent) ?: return
            coroutineScope.launch {
                withContext(ioDispatcher) {
                    geofencingEvent.triggeringGeofences?.map { systemGeofence ->
                        GeofenceLog(
                            id = systemGeofence.requestId.toLong(),
                            geofenceEvent = event,
                            timeStamp = System.currentTimeMillis()
                        )
                    }?.let { geofenceLogs ->
                        repository.addGeofenceLog(
                            geofenceLogs
                        ).getOrNull()?.zip(geofenceLogs)?.filter { it.first > 0 }?.forEach {
                            geofencingTransitionToGeofenceEvent(geofencingEvent)?.let { event ->
                                notificationManager.showGeofenceNotification(
                                    geofencelog = it.second,
                                    event = event,
                                    rowid = it.first
                                )
                            }
                        }
                    }
                }
            }
            val alertString = "Geofence Alert :" +
                    " Trigger ${geofencingEvent.triggeringGeofences}" +
                    " Transition ${geofencingEvent.geofenceTransition}"
            Log.d(
                TAG,
                alertString
            )
        }
    }
}

fun geofencingTransitionToGeofenceEvent(geofencingEvent: GeofencingEvent): GeofenceEvent? {
    return when (geofencingEvent.geofenceTransition) {
        Geofence.GEOFENCE_TRANSITION_ENTER -> GeofenceEvent.TRANSITION_ENTER
        Geofence.GEOFENCE_TRANSITION_EXIT -> GeofenceEvent.TRANSITION_EXIT
        Geofence.GEOFENCE_TRANSITION_DWELL -> GeofenceEvent.TRANSITION_DWELL
        else -> null
    }
}
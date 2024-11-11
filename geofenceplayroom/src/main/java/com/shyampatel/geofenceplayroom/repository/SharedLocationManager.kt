package com.shyampatel.geofenceplayroom.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.TimeUnit

class SharedLocationManager constructor(
    private val context: Context,
    externalScope: CoroutineScope
) {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            context
        )
    }
    private val locationRequest by lazy {
        LocationRequest.Builder(Long.MAX_VALUE)
            .setIntervalMillis(TimeUnit.SECONDS.toMillis(60))
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(30))
            .setMinUpdateDistanceMeters(50.0f)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
    }
    private val TAG: String = "LocationManager"

    @SuppressLint("MissingPermission")
    private val _locationUpdates = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                Log.d(TAG, "New location: ${result.lastLocation.toString()}")
                // Send the new location to the Flow observers
                result.lastLocation?.let { trySend(Result.success(it)) }
            }
        }

        Log.d(TAG, "Starting location updates")

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        ).addOnFailureListener { e ->
            close(e)
        }

        awaitClose {
            Log.d(TAG, "Stopping location updates")
            val removeTask =
                fusedLocationClient.removeLocationUpdates(callback) // clean up when Flow collection ends
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Location Callback removed.")
                } else {
                    Log.d(TAG, "Failed to remove Location Callback.")
                }
            }
        }

    }.
    retryWhen { cause, attempt ->
        emit(Result.failure(cause))
        cause is SecurityException
    }.shareIn(
        externalScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )

    fun locationFlow(): Flow<Result<Location>> {
        return _locationUpdates
    }
}

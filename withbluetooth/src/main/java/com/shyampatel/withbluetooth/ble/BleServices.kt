package com.shyampatel.withbluetooth.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.companion.CompanionDeviceManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.shyampatel.withbluetooth.MainActivity
import com.shyampatel.withbluetooth.ble.BleForegroundService.Companion.LIVE_TEMPERATURE_CHANNEL_ID
import com.shyampatel.withbluetooth.navigation.homeScreenIntentExtraKey
import com.shyampatel.withbluetooth.navigation.uri
import java.util.UUID

object ServiceState{
    var isForegroundServiceRunning = false
}

@SuppressLint("MissingPermission")
open class BleService : Service() {

    companion object {

        val SERVICE_UUID: UUID = UUID.fromString("00000001-710e-4a5b-8d75-3e5b444bc3cf")
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("00000002-710e-4a5b-8d75-3e5b444bc3cf")
        val C_OR_F_UUID: UUID = UUID.fromString("00000003-710e-4a5b-8d75-3e5b444bc3cf")
        private const val TAG = "BleService"
    }

    private var device: BluetoothDevice? = null
    var state by mutableStateOf(DeviceConnectionState.None)
    val notifyingCharacteristics: MutableList<UUID> = mutableListOf()
    private val deviceManager by lazy { applicationContext.getSystemService<CompanionDeviceManager>() }
    private val binder by lazy { LocalBinder() }
    protected open val connectionManager by lazy { ConnectionManager() }.apply { state = state.copy(connectionManager = this.value, isForegroundServiceRunning = false) }
    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onConnectionSetupComplete = { gatt ->
                state = state.copy(services = gatt.services)
            }

            onDisconnect = {
                Log.d(TAG, "Disconnected")
                state = DeviceConnectionState.None
            }

            onCharacteristicRead = { _, characteristic, value ->
                Log.d(TAG, "Read from ${characteristic.uuid}: ${value.decodeToString()}")
                if (characteristic.uuid == CHARACTERISTIC_UUID) {
                    state = state.copy(temperature = value.decodeToString())
                }
                if (characteristic.uuid == C_OR_F_UUID) {
                    state = state.copy(celsiusOrFahrenheit = value.decodeToString())
                }
            }

            onCharacteristicWrite = { _, characteristic ->
                Log.d(TAG, "Wrote to ${characteristic.uuid}")
            }

            onMtuChanged = { _, mtu ->
                Log.d(TAG, "MTU updated to $mtu")
            }

            onCharacteristicChanged = { _, characteristic, value ->
                Log.d(TAG, "Value changed on ${characteristic.uuid}: ${value.decodeToString()}")
                state = state.copy(temperature = value.decodeToString())
                if (state.isForegroundServiceRunning == true) {
                    val builder = NotificationCompat.Builder(this@BleService, LIVE_TEMPERATURE_CHANNEL_ID)
                        .setSmallIcon(applicationInfo.icon)
                        .setContentTitle("Current Temperature")
                        .setContentText(value.decodeToString())
                        .setContentIntent(
                            Intent(
                                Intent.ACTION_VIEW,
                                "$uri/homeScreen".toUri(),
                                applicationContext,
                                MainActivity::class.java
                            ).let {
                                it.putExtras(
                                    Bundle().apply {
                                        putString(homeScreenIntentExtraKey, value.decodeToString())
                                    }
                                )
                                TaskStackBuilder.create(applicationContext).run {
                                    addNextIntentWithParentStack(it)
                                    getPendingIntent(0, PendingIntent.FLAG_MUTABLE)
                                }
                            }
                        )
                        .setAutoCancel(true)
                    if (ActivityCompat.checkSelfPermission(
                            this@BleService,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        NotificationManagerCompat.from(this@BleService).notify(1, builder.build())
                    }
                }
            }

            onNotificationsEnabled = { _, characteristic ->
                Log.d(TAG, "Enabled notifications on ${characteristic.uuid}")
                notifyingCharacteristics.add(characteristic.uuid)
                state = state.copy(isNotifying = true)
            }

            onNotificationsDisabled = { _, characteristic ->
                Log.d(TAG, "Disabled notifications on ${characteristic.uuid}")
                notifyingCharacteristics.remove(characteristic.uuid)
                state = state.copy(isNotifying = false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        // If we are missing permission stop the service
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            PackageManager.PERMISSION_GRANTED
        }
        if (permission != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        val device = deviceManager?.getAssociatedDevices()?.first()
        this.device = device?.device
        this.device?.let {
            connectionManager.connect(it, applicationContext)
        }
        connectionManager.registerListener(connectionEventListener)
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        return binder
    }

    @SuppressLint("MissingPermission")
    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        this.device?.let {
            connectionManager.unregisterListener(connectionEventListener)
            connectionManager.teardownConnection(it)
        }
        return super.onUnbind(intent)
    }

    //TODO()
    @SuppressLint("MissingPermission")
    open fun reconnectIfDisconnected() {
//        if (state.gatt != null && !onCreateRunning) {
            // If we previously had a GATT connection let's reestablish it
//            state.gatt?.connect()
//        }
    }

//    @SuppressLint("MissingPermission")
//    open fun disconnect() {
////        state.gatt?.disconnect()
//        this.device?.let {
//            connectionManager.teardownConnection(it)
//        }
//    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        this.device?.let {
            connectionManager.unregisterListener(connectionEventListener)
            connectionManager.teardownConnection(it)
        }
        super.onDestroy()
    }

    open inner class LocalBinder : Binder() {
        open fun getService(): BleService = this@BleService
    }
}

class BleForegroundService : BleService() {

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
        const val LIVE_TEMPERATURE_CHANNEL_ID = "LiveTemperatureNotificationChannel"
        private const val TAG = "BleForegroundService"
    }

    private var isStartedInForeground: Boolean = false
    private val binder by lazy { LocalBinder() }
    override val connectionManager by lazy { ConnectionManager() }.apply { state = state.copy(connectionManager = this.value, isForegroundServiceRunning = true) }

    override fun onCreate() {
        super.onCreate()
        ServiceState.isForegroundServiceRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        if (!isStartedInForeground) {
            createNotificationChannel()
            val notification = createNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    100,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE,
                )
            } else {
                startForeground(100, notification)
            }
        }

        isStartedInForeground = true
        return START_REDELIVER_INTENT
    }

    override fun onBind(p0: Intent?): IBinder {
        Log.d(TAG,"onBind")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnBind")
        return true
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        ServiceState.isForegroundServiceRunning = false
        super.onDestroy()
    }

    override fun reconnectIfDisconnected() {

    }

    inner class LocalBinder : BleService.LocalBinder() {
        override fun getService(): BleForegroundService = this@BleForegroundService
    }

    private fun createNotificationChannel() {
        val channel1 = NotificationChannelCompat.Builder(
            CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_HIGH,
        )
            .setName("PI BLE channel")
            .setDescription("Channel for PI BLE notifications")
            .build()
        NotificationManagerCompat.from(this).createNotificationChannel(channel1)
        val channel2 = NotificationChannelCompat.Builder(
            LIVE_TEMPERATURE_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_HIGH,
        )
            .setSound(null, null)
            .setName("PI Temperature channel")
            .build()
        NotificationManagerCompat.from(this).createNotificationChannel(channel2)
    }

    private fun createNotification(): Notification {
        // Create a notification for the foreground service
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(applicationInfo.icon)
            .setContentTitle("PI BLE service")
            .setContentText("Running...")
        return builder.build()
    }

}

data class DeviceConnectionState(
    val connectionManager: ConnectionManager?,
    val mtu: Int,
    val services: List<BluetoothGattService> = emptyList(),
    val isNotifying: Boolean = false,
    val temperature: String = "",
    val celsiusOrFahrenheit: String = "",
    val isForegroundServiceRunning: Boolean? = null,
) {
    companion object {
        val None = DeviceConnectionState(null, -1, )
    }
}
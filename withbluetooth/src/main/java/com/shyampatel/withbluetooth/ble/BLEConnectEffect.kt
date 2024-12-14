package com.shyampatel.withbluetooth.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner


@SuppressLint("InlinedApi")
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
internal fun BLEConnectEffect(
    device: BluetoothDevice,
    startForegroundService: Boolean?,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onStateChange: (DeviceConnectionState) -> Unit,
) {

    val context = LocalContext.current
    val currentOnStateChange by rememberUpdatedState(onStateChange)

    var service: BleService? by remember { mutableStateOf<BleService?>(null) }
    val isServiceExistInForeground = ServiceState.isForegroundServiceRunning

    var state by remember(service?.state) {
        val myState = service?.state ?: DeviceConnectionState.None
        mutableStateOf(myState).also {
            currentOnStateChange(myState)
        }
    }

    val mConnection =
        object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
                service = if (startForegroundService == true) {
                    (iBinder as BleForegroundService.LocalBinder).getService()
                } else {
                    (iBinder as BleService.LocalBinder).getService()
                }
                state = service?.state ?: DeviceConnectionState.None
                currentOnStateChange(state)
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                service = null
            }
        }

    DisposableEffect(key1 = lifecycleOwner, device, startForegroundService) {
        if (startForegroundService == true || (isServiceExistInForeground && startForegroundService == true) || (isServiceExistInForeground && startForegroundService == null)) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, BleForegroundService::class.java).apply {}
            )
            context.bindService(
                Intent(context, BleForegroundService::class.java),
                mConnection, 0
            )
        } else {
            if (isServiceExistInForeground) {
                context.stopService(Intent(context, BleForegroundService::class.java))
            }
            context.bindService(
                Intent(context, BleService::class.java),
                mConnection, Context.BIND_AUTO_CREATE
            )
        }

        val observer = LifecycleEventObserver { _, event ->

            if(startForegroundService == false) {
                if (event == Lifecycle.Event.ON_START) {
//                    service?.reconnectIfDisconnected()
                } else if (event == Lifecycle.Event.ON_STOP) {
//                    service?.disconnect()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer and close the connection
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            context.unbindService(mConnection)
            state = DeviceConnectionState.None
        }
    }
}
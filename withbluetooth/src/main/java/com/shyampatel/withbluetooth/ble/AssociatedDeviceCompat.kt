package com.shyampatel.withbluetooth.ble

import android.bluetooth.BluetoothDevice
import android.companion.AssociationInfo
import android.companion.CompanionDeviceManager
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Wrapper for the different type of classes the CDM returns
 */
data class AssociatedDeviceCompat(
    val id: Int,
    val address: String,
    val name: String,
    val device: BluetoothDevice?,
)

@RequiresApi(Build.VERSION_CODES.O)
internal fun CompanionDeviceManager.getAssociatedDevices(): List<AssociatedDeviceCompat> {
    val associatedDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        myAssociations.map { it.toAssociatedDevice() }
    } else {
        // Before Android 34 we can only get the MAC. We could use the BT adapter to find the
        // device, but to use CDM we only need the MAC.
        @Suppress("DEPRECATION")
        associations.map {
            AssociatedDeviceCompat(
                id = -1,
                address = it,
                name = "",
                device = null,
            )
        }
    }
    return associatedDevice
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal fun AssociationInfo.toAssociatedDevice() = AssociatedDeviceCompat(
    id = id,
    address = deviceMacAddress?.toString() ?: "N/A",
    name = displayName?.ifBlank { "N/A" }?.toString() ?: "N/A",
    device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        associatedDevice?.bleDevice?.device
    } else {
        null
    },
)

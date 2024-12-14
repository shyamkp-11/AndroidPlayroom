package com.shyampatel.withbluetooth.screens

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothLeDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.ParcelUuid
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.shyampatel.ui.AndroidPlayroomLoadingIndicator
import com.shyampatel.ui.permissions.PermissionsScreen
import com.shyampatel.ui.theme.AndroidPlayroomTheme
import com.shyampatel.withbluetooth.ble.AssociatedDeviceCompat
import com.shyampatel.withbluetooth.ble.BLEConnectEffect
import com.shyampatel.withbluetooth.ble.BleService.Companion.C_OR_F_UUID
import com.shyampatel.withbluetooth.ble.BleService.Companion.CHARACTERISTIC_UUID
import com.shyampatel.withbluetooth.ble.BleService.Companion.SERVICE_UUID
import com.shyampatel.withbluetooth.ble.DeviceConnectionState
import com.shyampatel.withbluetooth.ble.ConnectionManager
import com.shyampatel.withbluetooth.ble.getAssociatedDevices
import com.shyampatel.withbluetooth.ble.toAssociatedDevice
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.Executor

private const val TAG = "BLEConnectEffect"
@Composable
fun HomeScreenRoute(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    autoConnect: Boolean = false,
    homeViewModel: HomeViewModel = koinViewModel(),
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        HomeScreen(autoConnect, onClose)
    }
}

@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    autoConnect: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold { innerPadding ->
        Box(modifier = modifier.padding(top = innerPadding.calculateTopPadding())) {

            val context = LocalContext.current
            val deviceManager = context.getSystemService<CompanionDeviceManager>()
            val adapter = context.getSystemService<BluetoothManager>()?.adapter
            var selectedDevice by remember {
                mutableStateOf<BluetoothDevice?>(null)
            }
            if (deviceManager == null || adapter == null) {
                Text(text = "No Companion device manager found. The device does not support it.")
            } else {
                PermissionsScreen(
                    modifier = Modifier.padding(16.dp),
                    title = "Need Bluetooth Permissions",
                    description = "Allow WithBluetooth App to access Bluetooth",
                    permissions = setOf(Manifest.permission.BLUETOOTH_CONNECT)
                ) {
                    if ((autoConnect && deviceManager.getAssociatedDevices()
                            .isEmpty()) || (!autoConnect && selectedDevice == null)
                    ) {
                        DevicesScreen(deviceManager) { device ->
                            selectedDevice =
                                (device.device ?: adapter.getRemoteDevice(device.address))
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (selectedDevice == null) {
                                selectedDevice = deviceManager.getAssociatedDevices().last().device
                                    ?: adapter.getRemoteDevice(
                                        deviceManager.getAssociatedDevices().last().address
                                    )
                            }
                            ConnectDeviceScreen(device = selectedDevice!!) {
                                selectedDevice = null
                                onClose()
                            }

                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DevicesScreen(
    deviceManager: CompanionDeviceManager,
    onConnect: (AssociatedDeviceCompat) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var associatedDevices by remember {
        // If we already associated the device no need to do it again.
        mutableStateOf(deviceManager.getAssociatedDevices())
    }


    Column(modifier = Modifier.fillMaxSize()) {

        // the or clause is used to reassociate after forget device from settings.
        if (associatedDevices.isEmpty() || associatedDevices.last().device?.name == null) {
            ScanForDevicesMenu(deviceManager) {
                associatedDevices = associatedDevices + it
            }
        } else {

            AssociatedDevicesList(
                associatedDevices = associatedDevices,
                onConnect = onConnect,
                onDisassociate = {
                    scope.launch {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            deviceManager.disassociate(it.id)
                        } else {
                            @Suppress("DEPRECATION")
                            deviceManager.disassociate(it.address)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            deviceManager.stopObservingDevicePresence(it.address)
                        }
                        associatedDevices = deviceManager.getAssociatedDevices()
                    }
                },
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ScanForDevicesMenu(
    deviceManager: CompanionDeviceManager,
    onDeviceAssociated: (AssociatedDeviceCompat) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var errorMessage by remember {
        mutableStateOf("")
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) {
        when (it.resultCode) {
            CompanionDeviceManager.RESULT_OK -> {
                it.data?.getAssociationResult()?.run {
                    onDeviceAssociated(this)
                }
            }

            CompanionDeviceManager.RESULT_CANCELED -> {
                errorMessage = "The request was canceled"
            }

            CompanionDeviceManager.RESULT_INTERNAL_ERROR -> {
                errorMessage = "Internal error happened"
            }

            CompanionDeviceManager.RESULT_DISCOVERY_TIMEOUT -> {
                errorMessage = "No device matching the given filter were found"
            }

            CompanionDeviceManager.RESULT_USER_REJECTED -> {
                errorMessage = "The user explicitly declined the request"
            }

            else -> {
                errorMessage = "Unknown error"
            }
        }
    }
    ScanForDevicesMenu(
        onScanClicked = {
            scope.launch {
                val intentSender = requestDeviceAssociation(deviceManager)
                launcher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
        },
        errorMessage = errorMessage
    )
}

@Composable
fun ScanForDevicesMenu(
    errorMessage: String? = null,
    onScanClicked: () -> Unit,
) {
//    .background(MaterialTheme.colorScheme.primaryContainer)
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 250.dp),
            text = "Associate Bluetooth device",
        )
        Button(
            modifier = Modifier
                .size(200.dp)
                .background(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
                .align(Alignment.Center),
            onClick = onScanClicked
        ) {
            Text(text = "Start", style = MaterialTheme.typography.titleLarge)
        }
        if (!errorMessage.isNullOrEmpty()) {
            Text(modifier = Modifier
                .padding(top = 250.dp)
                .align(Alignment.Center),
                text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AssociatedDevicesList(
    associatedDevices: List<AssociatedDeviceCompat>,
    onConnect: (AssociatedDeviceCompat) -> Unit,
    onDisassociate: (AssociatedDeviceCompat) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 250.dp),
            text = "${associatedDevices.last().name}",
        )
        Button(
            modifier = Modifier
                .size(200.dp)
                .background(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
                .align(Alignment.Center),
            onClick = { onConnect(associatedDevices.last()) }
        ) {
            Text(text = "Connect", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@SuppressLint("InlinedApi")
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun ConnectDeviceScreen(device: BluetoothDevice, onClose: () -> Unit) {

    // Keeps track of the last connection state with the device
    var state by remember(device) {
        mutableStateOf<DeviceConnectionState?>(null)
    }
    // Once the device services are discovered find the PI Temperature service
    val service by remember(state?.services) {
        mutableStateOf(state?.services?.find { it.uuid == SERVICE_UUID })
    }
    // If the Pi temperature characteristic service is found, get the characteristic
    val characteristic by remember(service) {
        mutableStateOf(service?.getCharacteristic(CHARACTERISTIC_UUID))
    }

    val corFCharacteristic by remember(service) {
        mutableStateOf(service?.getCharacteristic(C_OR_F_UUID))
    }

    var startForegroundService by remember {
        mutableStateOf<Boolean?>(null)
    }

    // This effect will handle the connection and notify when the state changes
    BLEConnectEffect(device = device, startForegroundService) {
        // Check is needed when device if paired.
        if (state != DeviceConnectionState.None && state != null && it == DeviceConnectionState.None) {
            state = it
            onClose()
        } else {
            // update our state to recompose the UI
            state = it
        }
    }
    ConnectDeviceScreen(
        canRunOperations = characteristic != null && corFCharacteristic != null,
        deviceName = device.name ?: "Unknown",
        deviceAddress = device.address,
        state = state,
        onClose = onClose,
        enableNotifications = {
            state?.connectionManager?.enableNotifications(device, characteristic!!) },
        disableNotifications = {
            state?.connectionManager?.disableNotifications(device, characteristic!!) },
        writeCharacteristic = {
            state?.connectionManager?.writeCharacteristic(
                device,
                corFCharacteristic!!,
                it.toByteArray()
            )
            state?.connectionManager?.readCharacteristic(device, corFCharacteristic!!) },
        readCharacteristic = {
            state?.connectionManager?.readCharacteristic(device, characteristic!!)
            state?.connectionManager?.readCharacteristic(device, corFCharacteristic!!) },
        startForegroundService = {
            startForegroundService = it
        }
    )
}

@Composable
fun ConnectDeviceScreen(
    modifier: Modifier = Modifier,
    canRunOperations: Boolean,
    deviceName: String,
    deviceAddress: String,
    state: DeviceConnectionState?,
    onClose: () -> Unit,
    enableNotifications: () -> Unit,
    disableNotifications: () -> Unit,
    writeCharacteristic: (temperatureUnit: String)-> Unit,
    readCharacteristic: () -> Unit,
    startForegroundService: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    var showNotificationPermission by remember {
        mutableStateOf<Boolean?>(null)
    }
    if (showNotificationPermission == true) {
        PermissionsScreen(
            modifier = modifier.padding(16.dp),
            title = "Allow notification permissions",
            description = "Allow WithBluetooth App to post notifications to show real time PI CPI temperature",
            permissions = setOf(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            showNotificationPermission = false
        }
    } else {
        Box {
            Column(
                modifier = modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = "$deviceName", style = MaterialTheme.typography.headlineSmall)
                Row {
                    if (canRunOperations) {
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(5.dp)
                                .background(color = Color.Green, shape = CircleShape)
                                .align(Alignment.CenterVertically)

                        )
                        Text(
                            text = "Connected ($deviceAddress)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Row {
                    Text(
                        text = "Celsius or Fahrenheit:",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "°C", modifier = Modifier.align(Alignment.CenterVertically))
                    RadioButton(
                        enabled = state?.services != null,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        selected = state?.celsiusOrFahrenheit == "C",
                        onClick = { scope.launch { writeCharacteristic("C") } }
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(text = "°F", modifier = Modifier.align(Alignment.CenterVertically))
                    RadioButton(
                        enabled = state?.services != null,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        selected = state?.celsiusOrFahrenheit == "F",
                        onClick = { scope.launch { writeCharacteristic("F") } }
                    )
                }
                Row {
                    ElevatedButton(
                        modifier = Modifier.padding(start = 0.dp),
                        border = null,
                        enabled = state?.services != null,
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                readCharacteristic()
                            }
                        },
                    ) {
                        Text(text = "Read Temperature",)
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    if (state?.isNotifying == true) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = state.temperature,
                            color = Color.Green
                        )
                    } else {
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = "${state?.temperature}",
                        )
                    }
                }
                Row {
                    Text(
                        text = "Notify:",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        enabled = state?.services != null,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        checked = state?.isNotifying == true,
                        onCheckedChange = {
                            scope.launch {
                                if (it) {
                                    enableNotifications()
                                } else {
                                    disableNotifications()
                                }
                            }
                            if (showNotificationPermission == null) {
                                showNotificationPermission = true
                            }
                        })
                }
                Row {
                    Text(
                        text = "Start temperature service:",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        checked = state?.isForegroundServiceRunning == true,
                        enabled = state?.isForegroundServiceRunning != null,
                        onCheckedChange = {
                            scope.launch {
                                if (it) {
                                    startForegroundService(true)
                                } else {
                                    startForegroundService(false)
                                }
                            }
                            if (showNotificationPermission == null) {
                                showNotificationPermission = true
                            }
                        })
                }

                Button(onClick = onClose) {
                    Text(text = "Close")
                }
            }
        }
        if (!canRunOperations) {
            AndroidPlayroomLoadingIndicator(modifier = Modifier)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun Intent.getAssociationResult(): AssociatedDeviceCompat? {
    var result: AssociatedDeviceCompat? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        result = getParcelableExtra(
            CompanionDeviceManager.EXTRA_ASSOCIATION,
            AssociationInfo::class.java,
        )?.toAssociatedDevice()
    } else {
        // Below Android 33 the result returns either a BLE ScanResult, a
        // Classic BluetoothDevice or a Wifi ScanResult
        // In our case we are looking for our BLE GATT server so we can cast directly
        // to the BLE ScanResult
        @Suppress("DEPRECATION")
        val scanResult = getParcelableExtra<ScanResult>(CompanionDeviceManager.EXTRA_DEVICE)
        if (scanResult != null) {
            result = AssociatedDeviceCompat(
                id = scanResult.advertisingSid,
                address = scanResult.device.address ?: "N/A",
                name = scanResult.scanRecord?.deviceName ?: "N/A",
                device = scanResult.device,
            )
        }
    }
    return result
}

@RequiresApi(Build.VERSION_CODES.O)
private suspend fun requestDeviceAssociation(deviceManager: CompanionDeviceManager): IntentSender {
    // Match only Bluetooth devices whose service UUID matches this pattern.
    val scanFilter = ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()
    val deviceFilter = BluetoothLeDeviceFilter.Builder()
        .setScanFilter(scanFilter)
        .build()

    val pairingRequest: AssociationRequest = AssociationRequest.Builder()
        .addDeviceFilter(deviceFilter)
        .setSingleDevice(true)
        .build()

    val result = CompletableDeferred<IntentSender>()

    val callback = object : CompanionDeviceManager.Callback() {
        override fun onAssociationPending(intentSender: IntentSender) {
            result.complete(intentSender)
        }

        @Suppress("OVERRIDE_DEPRECATION")
        override fun onDeviceFound(intentSender: IntentSender) {
            result.complete(intentSender)
        }

        override fun onAssociationCreated(associationInfo: AssociationInfo) {
            // This callback was added in API 33 but the result is also send in the activity result.
            // For handling backwards compatibility we can just have all the logic there instead
        }

        override fun onFailure(errorMessage: CharSequence?) {
            result.completeExceptionally(IllegalStateException(errorMessage?.toString().orEmpty()))
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val executor = Executor { it.run() }
        deviceManager.associate(pairingRequest, executor, callback)
    } else {
        deviceManager.associate(pairingRequest, callback, null)
    }
    return result.await()
}


@Preview
@Composable
private fun ConnectDeviceScreenPrev() {
    AndroidPlayroomTheme {
        ConnectDeviceScreen(
            canRunOperations = false,
            deviceName = "Device Name",
            deviceAddress = "Device Address",
            state = DeviceConnectionState(
                connectionManager = ConnectionManager(),
                services = emptyList(),
                temperature = "20",
                celsiusOrFahrenheit = "C",
                isNotifying = true,
                mtu = 517,
            ),
            onClose = {},
            enableNotifications = {},
            disableNotifications = {},
            writeCharacteristic = {},
            readCharacteristic = {},
            startForegroundService = {}
        )
    }
}

@Preview
@Composable
private fun ScanForDevicesMenuPreview() {
    ScanForDevicesMenu(
        onScanClicked = {},
        errorMessage = "No devices found"
    )
}
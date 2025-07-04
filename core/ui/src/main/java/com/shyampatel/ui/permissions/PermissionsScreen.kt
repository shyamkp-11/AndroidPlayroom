package com.shyampatel.ui.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.shyampatel.ui.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PermissionsScreen(
    modifier: Modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp),
    title: String,
    description: String?,
    requiredTitle: String? = null,
    requiredDescription: String? = null,
    onGranted: () -> Unit,
    permissions: Set<String>,
    openSettingsIntent: Intent? = null,
    requiredPermissions: Set<String> = permissions,
    nextText: String = stringResource(R.string.core_ui_next),
    settingsText: String = stringResource(R.string.core_ui_open_settings),
    cancelText: String? = null,
    onCancelButtonClicked: (() -> Unit)? = null,
    permissionsViewModel: PermissionsViewModel = koinViewModel()
) {
    val permissionsMap by permissionsViewModel.permissionsMap.collectAsStateWithLifecycle()
    Scaffold(
    ) { innerPadding ->
        Box(modifier = modifier.padding(top = innerPadding.calculateTopPadding()).semantics { testTagsAsResourceId = true }) {
            PermissionsScreen(
                modifier = Modifier,
                title = title,
                description = description,
                requiredTitle = requiredTitle,
                requiredSubtitle = requiredDescription,
                settingsText = settingsText,
                openSettingsIntent = openSettingsIntent,
                permissionsMap = permissionsMap,
                savePermissionMap = permissionsViewModel::savePermissionsMap,
                permissions = permissions,
                requiredPermissions = requiredPermissions,
                onNextClicked = { onGranted() },
                cancelText = cancelText,
                onCancelButtonClicked = onCancelButtonClicked,
                nextText = nextText,
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PermissionsScreen(
    modifier: Modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp).semantics { testTagsAsResourceId = true },
    title: String,
    description: String?,
    requiredTitle: String? = null,
    requiredDescription: String? = null,
    permissions: Set<String>,
    openSettingsIntent: Intent? = null,
    requiredPermissions: Set<String> = permissions,
    nextText: String = stringResource(R.string.core_ui_next),
    cancelText: String? = null,
    onCancelButtonClicked: (() -> Unit)? = null,
    permissionsViewModel: PermissionsViewModel = koinViewModel(),
    settingsText: String = stringResource(R.string.core_ui_open_settings),
    contentAlignment: Alignment = Alignment.TopStart,
    onGrantedComposable: @Composable BoxScope.(grantedPermissions: List<String>) -> Unit,
) {
    val permissionsMap by permissionsViewModel.permissionsMap.collectAsStateWithLifecycle()

    PermissionsScreen(
        modifier = modifier,
        title = title,
        description = description,
        requiredTitle = requiredTitle,
        requiredSubtitle = requiredDescription,
        openSettingsIntent = openSettingsIntent,
        permissionsMap = permissionsMap,
        settingsText = settingsText,
        savePermissionMap = permissionsViewModel::savePermissionsMap,
        permissions = permissions,
        requiredPermissions = requiredPermissions,
        contentAlignment = contentAlignment,
        onGranted = onGrantedComposable,
        onNextClicked = {},
        cancelText = cancelText,
        onCancelButtonClicked = onCancelButtonClicked,
        nextText = nextText,
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionsScreen(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    requiredTitle: String?,
    requiredSubtitle: String?,
    cancelText: String?,
    nextText: String = stringResource(R.string.core_ui_next),
    settingsText: String = stringResource(R.string.core_ui_open_settings),
    onCancelButtonClicked: (() -> Unit)?,
    openSettingsIntent: Intent?,
    permissionsMap: Map<String, Boolean>?,
    savePermissionMap: (Map<String, Boolean>) -> Unit,
    permissions: Set<String>,
    requiredPermissions: Set<String> = permissions,
    onNextClicked: (grantedPermissions: List<String>) -> Unit,
    contentAlignment: Alignment = Alignment.TopStart,
    onGranted: @Composable (BoxScope.(grantedPermissions: List<String>) -> Unit)? = null,
) {

    val context = LocalContext.current
    var showPermissionRequiredScreen by remember { mutableStateOf(false) }

    var rejectedPermissions by remember {
        mutableStateOf(setOf<String>())
    }

    val permissionsState =
        rememberMultiplePermissionsState(permissions = permissions.toList()) { map ->
            rejectedPermissions = map.filterValues { !it }.keys
        }

    var readyToNavigateGranted by remember { mutableStateOf(false) }
    if (readyToNavigateGranted) {
        if (onGranted == null) {
            LaunchedEffect(key1 = Unit) {
                onNextClicked(permissionsState.permissions.filter { it.status.isGranted }
                    .map { it.permission })
            }
        } else {
            /*Box(modifier = Modifier
                .fillMaxSize()
                .then(contentModifier),
                contentAlignment = contentAlignment
            ) {*/
            Box(modifier = Modifier) {
                onGranted(
                    permissionsState.permissions
                        .filter { it.status.isGranted }
                        .map { it.permission },
                )
            }
           /* }*/
        }
    }

    val allRequiredPermissionsGranted =
        permissionsState.revokedPermissions.none { it.permission in requiredPermissions }

    if (allRequiredPermissionsGranted) {
        LaunchedEffect(Unit) {
            val updatedMap = permissionsMap?.toMutableMap()
            permissionsState.permissions.forEach {
                if (it.status.isGranted) {
                    showPermissionRequiredScreen = false
                    updatedMap.also { map -> map?.remove(it.permission) }
                }
            }
            updatedMap?.let(savePermissionMap)
            readyToNavigateGranted = true
        }

    } else {

        LaunchedEffect(permissionsState.shouldShowRationale, permissionsMap) {
            val updatedMap = permissionsMap?.toMutableMap() ?: return@LaunchedEffect
            permissionsState.permissions.forEach {
                if (permissionsMap[it.permission] == null || permissionsMap[it.permission] == false) {
                    updatedMap.also { map ->
                        map[it.permission] =
                            permissionsState.shouldShowRationale
                    }
                }
            }
            savePermissionMap(updatedMap)
        }

        // if shouldShowRationale changed from true to false and the permission is not granted,
        // then the user denied the permission
        val userDeniedPermission =
            (permissionsMap != null && permissionsMap.any { requiredPermissions.contains(it.key) && it.value } && !permissionsState.shouldShowRationale) &&
                    permissionsState.revokedPermissions.any {
                        requiredPermissions.contains(
                            it.permission
                        )
                    }
        if (userDeniedPermission) {
            showPermissionRequiredScreen = true
        }

        if (!showPermissionRequiredScreen || requiredTitle == null) {
            PermissionsScreen(
                modifier = modifier,
                title = title,
                description = description,
                onButtonClicked = {
                    if (showPermissionRequiredScreen) {
                        val intent = openSettingsIntent
                            ?: Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts(
                                    "package",
                                    context.applicationContext.packageName,
                                    null
                                )
                            }
                        context.startActivity(intent)
                    } else {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                },
                buttonText = if (showPermissionRequiredScreen) settingsText else nextText,
                cancelText = cancelText,
                onCancelButtonClicked = onCancelButtonClicked,
            )
        } else {
            PermissionsRequiredScreen(
                modifier = modifier,
                requiredTitle = requiredTitle,
                requiredDescription = requiredSubtitle,
                openSettingsIntent = openSettingsIntent,
                cancelText = cancelText,
                settingsText = settingsText,
                onCancelButtonClicked = onCancelButtonClicked,
            )
        }
    }
}

@Composable
private fun PermissionsRequiredScreen(
    requiredTitle: String?,
    requiredDescription: String?,
    cancelText: String?,
    settingsText: String,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    openSettingsIntent: Intent? = null,
    onCancelButtonClicked: (() -> Unit)? = null,
) {
    PermissionsScreen(
        modifier = modifier,
        title = requiredTitle ?: "",
        description = requiredDescription,
        onButtonClicked = {
            val intent = openSettingsIntent
                ?: Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.applicationContext.packageName, null)
                }
            context.startActivity(intent)
        },
        buttonText = settingsText,
        cancelText = cancelText,
        onCancelButtonClicked = onCancelButtonClicked,
    )
}

@Composable
private fun PermissionsScreen(
    title: String,
    description: String?,
    onButtonClicked: () -> Unit,
    cancelText: String?,
    buttonText: String,
    onCancelButtonClicked: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        contentAlignment = Alignment.TopStart,
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxSize(),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Start,
                modifier = Modifier
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
        if (!cancelText.isNullOrEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f)
                        .testTag("permission:cancel"),
                    onClick = { onCancelButtonClicked?.invoke() },
                ) {
                    Text(text = cancelText)
                }
                Spacer(modifier = Modifier.width(20.dp))
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("permission:next"),
                    onClick = { onButtonClicked() },
                ) {
                    Text(text = buttonText)
                }
            }
        } else {
            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                onClick = { onButtonClicked() },
            ) {
                Text(text = buttonText)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionsScreenPreview() {
    PermissionsScreen(
        modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp),
        title = "Title",
        description = "Description",
        onCancelButtonClicked = {},
        onButtonClicked = {},
        buttonText = "Next",
        cancelText = "Cancel",
    )
}
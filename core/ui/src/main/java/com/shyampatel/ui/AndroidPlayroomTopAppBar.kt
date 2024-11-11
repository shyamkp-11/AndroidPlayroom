package com.shyampatel.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.shyampatel.ui.theme.AndroidPlayroomTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidPlayroomTopAppBar(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    drawerState: DrawerState? = null,
    backIconClick: (()-> Unit)? = null,
    topAppBarState: TopAppBarState = rememberTopAppBarState(),
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? =
        TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
) {
    val coroutineScope = rememberCoroutineScope()
    CenterAlignedTopAppBar(
        navigationIcon = {
            if (drawerState != null) {
                IconButton(onClick = {
                    coroutineScope.launch {
                        drawerState.open()
                    }
                }) {
                    Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.core_ui_navigation_drawer_icon))
                }
            } else if (backIconClick!= null) {
                IconButton(onClick = {
                    backIconClick()
                }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.core_ui_navigation_back_icon))
                }
            }
        },

        title = {
            Box(modifier = Modifier.fillMaxHeight()) {
                Text(text = stringResource(id = titleRes),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center))
            }
        },
        colors = colors,
        modifier = modifier,
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview("Top App Bar")
@Composable
private fun GithubPlayroomTopAppBarPreview() {
    AndroidPlayroomTheme {
        AndroidPlayroomTopAppBar(
            titleRes = R.string.core_ui_untitled,
        )
    }
}
package com.shyampatel.githubplayroom

import android.R
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.shyampatel.ui.theme.AndroidPlayroomTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GithubPlayroomTopAppBar(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    topAppBarState: TopAppBarState = rememberTopAppBarState(),
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? =
        TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
) {
    CenterAlignedTopAppBar(
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
        GithubPlayroomTopAppBar(
            titleRes = R.string.untitled,
        )
    }
}
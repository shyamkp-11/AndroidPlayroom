package com.shyampatel.githubplayroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.shyampatel.githubplayroom.navigation.GithubPlayroomNavHost
import com.shyampatel.ui.theme.AndroidPlayroomTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val appState = rememberGithubPlayroomAppState()
            AndroidPlayroomTheme {
                GithubPlayroomApp(appState, modifier = Modifier)
            }
        }
    }


    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun GithubPlayroomApp(appState: GithubPlayroomAppState, modifier: Modifier = Modifier) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceDim,
            modifier = modifier.fillMaxSize()
                        .semantics {
                testTagsAsResourceId = true
            },
        ) {
            val snackbarHostState = remember { SnackbarHostState() }
            GithubPlayroomApp(appState = appState, snackbarHostState = snackbarHostState)
        }
    }

    @Composable
    fun GithubPlayroomApp(appState: GithubPlayroomAppState, snackbarHostState: SnackbarHostState) {
        GithubPlayroomNavHost(
            navController = appState.navController,
            modifier = Modifier,
        )
    }
}

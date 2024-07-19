package com.shyampatel.githubplayroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration.Short
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.ActionPerformed
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.shyampatel.githubplayroom.navigation.GithubPlayroomNavHost
import com.shyampatel.githubplayroom.theme.GithubPlayroomTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setContent {
            val appState = rememberGithubPlayroomAppState()
            GithubPlayroomTheme {
                GithubPlayroomApp(appState, modifier = Modifier)
            }
        }
    }


    @Composable
    fun GithubPlayroomApp(appState: GithubPlayroomAppState, modifier: Modifier = Modifier) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceDim,
            modifier = modifier.fillMaxSize(),
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

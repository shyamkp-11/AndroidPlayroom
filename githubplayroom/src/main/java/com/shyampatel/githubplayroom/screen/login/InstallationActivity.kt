package com.shyampatel.githubplayroom.screen.login

import android.app.ComponentCaller
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.browser.customtabs.EngagementSignalsCallback
import androidx.browser.customtabs.ExperimentalMinimizationCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.shyampatel.githubplayroom.BuildConfig
import com.shyampatel.githubplayroom.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel


class InstallationActivity : AppCompatActivity() {

    private val viewModel: GithubLoginViewModel by viewModel()
    private var mClient: CustomTabsClient? = null
    var customTabsSession: CustomTabsSession? = null
    private var onNewIntentProcessing = false
    private var sessionEnded = false
    private var startDismissTimer = false

    private val mConnection: CustomTabsServiceConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected( name: ComponentName, client: CustomTabsClient
        ) {
            Log.d("AuthenticationActivity", "onCustomTabsServiceConnected: ")
            mClient = client
            // Warm up the browser process
            mClient!!.warmup(0 /* placeholder for future use */)
            // Create a new browser session
            customTabsSession = mClient!!.newSession(object : CustomTabsCallback() {
                @ExperimentalMinimizationCallback
                override fun onMinimized(extras: Bundle) {
                    super.onMinimized(extras)
                    Log.d("InstallationActivity", "minimized")
                }

                override fun onActivityResized(height: Int, width: Int, extras: Bundle) {
                    super.onActivityResized(height, width, extras)
                    Log.d("InstallationActivity", "resized")
                }
            })?.apply {
                if (isEngagementSignalsApiAvailable(Bundle())) {
                    this.setEngagementSignalsCallback(object : EngagementSignalsCallback {
                        override fun onSessionEnded(didUserInteract: Boolean, extras: Bundle) {
                            super.onSessionEnded(didUserInteract, extras)
                            Log.d("InstallationActivity", "session ended")
                            sessionEnded = true
                        }
                    }, Bundle())
                    // Pre-render pages the user is likely to visit
                    // you can do this any time while the service is connected
//            customTabsSession!!.mayLaunchUrl(Uri.parse("https://developers.android.com"), null, null)
                }
        }}

        override fun onServiceDisconnected(name: ComponentName) {
            mClient = null
            customTabsSession = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("InstallationActivity", "onCreate: ")
        enableEdgeToEdge()
        setContentView(R.layout.activity_authentication)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindCustomTabService(this)

        val authorizationUrl = "https://github.com/apps/${BuildConfig.APP_NAME_GITHUBAPP}/installations/new"

        val intentBuilder = CustomTabsIntent.Builder(customTabsSession)
        intentBuilder.setExitAnimations(
            this@InstallationActivity,
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )
        intentBuilder.setShowTitle(true)
        intentBuilder.setUrlBarHidingEnabled(true)
        intentBuilder.setSendToExternalDefaultHandlerEnabled(true)
        intentBuilder.setShareState(CustomTabsIntent.SHARE_STATE_OFF);
        intentBuilder.setBookmarksButtonEnabled(false)
        intentBuilder.setDownloadButtonEnabled(false)
        val intent = intentBuilder.build()
        intent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.launchUrl(this, Uri.parse(authorizationUrl));

    }

    private fun bindCustomTabService(context: Context) {
        // Check for an existing connection
        if (mClient != null) {
            // Do nothing if there is an existing service connection
            return
        }

        // Get the default browser package name, this will be null if
        // the default browser does not provide a CustomTabsService
        val packageName = CustomTabsClient.getPackageName(context, null)
            ?: // Do nothing as service connection is not supported
            return
        CustomTabsClient.bindCustomTabsService(context, packageName, mConnection)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("InstallationActivity", "onNewIntent:")
        onNewIntentProcessing = true
        setResult(RESULT_OK)
        finish()
    }

    override fun onResume() {
        super.onResume()
        Log.d("InstallationActivity", "onResume: ")
        lifecycleScope.launch {
            startDismissTimer = true
            delay(1000)
            if (startDismissTimer) {
                withContext(Dispatchers.Main) {
                    if (!onNewIntentProcessing) {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("InstallationActivity", "onPause: ")
        startDismissTimer = false
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)
        Log.d("InstallationActivity", "onActivityResult: $requestCode; $resultCode; $data")
    }

    companion object {
        const val KEY_TOKEN: String = "token"
    }
}

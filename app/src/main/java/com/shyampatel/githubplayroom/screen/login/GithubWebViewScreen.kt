package com.shyampatel.githubplayroom.screen.login

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.shyampatel.githubplayroom.BuildConfig
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun GithubWebViewRoute(
    modifier: Modifier = Modifier,
    onFinish: (success: Boolean) -> Unit,
    loginViewModel: GithubLoginViewModel = koinViewModel(), ){
    GithubWebViewScreen(modifier, loginViewModel::generateToken, onFinish )
}
@Composable
fun GithubWebViewScreen(modifier: Modifier, saveCode: (code: String, onSaveComplete:()->Unit)-> Unit, onFinish: (success: Boolean) -> Unit) {

    val mUrl = "https://github.com/login/oauth/authorize?client_id=${BuildConfig.CLIENT_ID}&scope=repo"
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = CustomWebViewClient(saveCode, onFinish)

                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.setSupportZoom(true)
            }
        },
        update = { webView ->
            webView.loadUrl(mUrl)
        }
    )
}

class CustomWebViewClient(val saveCode: (code: String, onSaveComplete:()->Unit)-> Unit, val onFinish: (success: Boolean) -> Unit): WebViewClient(){
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if(request?.url != null &&
            (request.url.toString().startsWith("https://www.github.com") ||
                    request.url.toString().startsWith("https://github.com"))){
            view?.loadUrl(request.url.toString())
            if (request.url.toString().contains("github.com/?code=")) {
                saveCode(request.url.getQueryParameter("code").toString()
                ) {
                    onFinish(true)
                }
            }
            return true
        }
        return false
    }
}
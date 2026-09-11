package io.github.norbertweb.bluebird.browser.ui.webview

import android.graphics.Bitmap
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.norbertweb.bluebird.browser.model.BrowserTab

/** Hardened, deliberately small WebView used by Bluebird Launcher.
 * Only normal HTTP(S) browsing is handled here. */
@Composable
fun BrowserWebView(
    url: String,
    tab: BrowserTab,
    modifier: Modifier = Modifier,
    onWebViewReady: (WebView) -> Unit,
    onPageStarted: (String) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onPageFinished: (String, String?) -> Unit,
    onPageError: (String, String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onUrlChanged: (String) -> Unit,
    onFaviconChanged: (Bitmap?) -> Unit,
    onDownloadStart: (String, String, String, String, Long) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val webView = remember(context) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = false
                allowFileAccess = false
                allowContentAccess = false
                setSupportMultipleWindows(false)
                javaScriptCanOpenWindowsAutomatically = false
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                mediaPlaybackRequiresUserGesture = true
                safeBrowsingEnabled = true
                builtInZoomControls = false
                displayZoomControls = false
                setSupportZoom(false)
            }
            CookieManager.getInstance().setAcceptCookie(true)
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val scheme = request.url.scheme?.lowercase()
                    return scheme != "http" && scheme != "https"
                }
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) { onPageStarted(url) }
                override fun onPageFinished(view: WebView, url: String) { onPageFinished(url, view.title); onUrlChanged(url) }
                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: android.webkit.WebResourceError) {
                    if (request.isForMainFrame) onPageError(request.url.toString(), error.description?.toString() ?: "Unable to load page")
                }
                override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                    // Never bypass certificate errors in the launcher browser.
                    handler.cancel()
                    onPageError(view.url.orEmpty(), "Secure connection could not be verified")
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) { onProgressChanged(newProgress) }
                override fun onReceivedTitle(view: WebView, title: String?) { onTitleChanged(title.orEmpty()) }
                override fun onReceivedIcon(view: WebView, icon: Bitmap?) { onFaviconChanged(icon) }
            }
            setDownloadListener { downloadUrl, userAgent, contentDisposition, mimeType, contentLength ->
                onDownloadStart(downloadUrl, userAgent, contentDisposition, mimeType, contentLength)
            }
        }
    }

    LaunchedEffect(url) {
        if (webView.url != url) webView.loadUrl(url)
    }

    DisposableEffect(Unit) {
        onWebViewReady(webView)
        onDispose {
            webView.stopLoading()
            webView.webChromeClient = null
            webView.destroy()
        }
    }

    AndroidView(factory = { webView }, modifier = modifier, update = {})
}

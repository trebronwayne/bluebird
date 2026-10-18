package io.github.norbertweb.bluebird.ui.components

import android.graphics.Bitmap
import android.provider.Settings
import android.net.Uri
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream
import java.io.File
import java.util.Locale
import org.json.JSONObject

/**
 * Runtime for an installed Bluebird web application. The JS bridge is attached only
 * to packaged local content. Navigation away from that app's directory is blocked,
 * so a remote page cannot inherit the native bridge.
 */
@Composable
fun WebAppViewerScreen(
    isDark: Boolean,
    app: InstalledBpkApp
) {
    val background = if (isDark) Color(0xFF111111) else Color(0xFFF7F7F7)
    val text = if (isDark) Color(0xFFEDEDED) else Color(0xFF1A1A1A)
    var isLoading by remember(app.id) { mutableStateOf(true) }
    var webViewRef by remember(app.id) { mutableStateOf<WebView?>(null) }

    fun dispatchPickedFile(uri: android.net.Uri?, eventName: String) {
        if (uri == null) return
        val safeUri = JSONObject.quote(uri.toString())
        webViewRef?.post {
            webViewRef?.evaluateJavascript(
                "window.dispatchEvent(new CustomEvent('$eventName',{detail:{uri:$safeUri}}));",
                null
            )
        }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        dispatchPickedFile(uri, "bluebird-file-picked")
    }
    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        dispatchPickedFile(uri, "bluebird-media-picked")
    }

    val entryFile = remember(app.installDir, app.entry) { File(app.installDir, app.entry).canonicalFile }
    val root = remember(app.installDir) { File(app.installDir).canonicalFile }
    val validEntry = remember(entryFile, root) {
        entryFile.isFile && (entryFile.path == root.path || entryFile.path.startsWith(root.path + File.separator))
    }

    DisposableEffect(app.id) {
        onDispose {
            webViewRef?.apply {
                stopLoading()
                removeJavascriptInterface("Bluebird")
                loadUrl("about:blank")
                clearHistory()
                removeAllViews()
                destroy()
            }
            webViewRef = null
        }
    }

    Box(Modifier.fillMaxSize().background(background)) {
        if (validEntry) {
            AndroidView(
                factory = { ctx ->
                    val logger = BpkAppLogger(ctx)
                    val permissions = BpkPermissionManager(ctx)
                    WebView(ctx).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = true
                            allowContentAccess = false
                            // Do not let a file:// page read arbitrary file:// or network origins.
                            allowFileAccessFromFileURLs = false
                            allowUniversalAccessFromFileURLs = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                            cacheMode = WebSettings.LOAD_DEFAULT
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = false
                            displayZoomControls = false
                            setSupportZoom(false)
                            javaScriptCanOpenWindowsAutomatically = false
                            setSupportMultipleWindows(false)
                        }
                        addJavascriptInterface(
                            BpkRuntimeApi(
                                context = ctx,
                                appId = app.id,
                                onPickFile = { post { filePicker.launch(arrayOf("*/*")) } },
                                onPickMedia = { post { mediaPicker.launch(arrayOf("image/*", "video/*", "audio/*")) } },
                                onBrightnessRequest = { value ->
                                    runCatching {
                                        if (!Settings.System.canWrite(ctx)) false
                                        else {
                                            val modeWritten = Settings.System.putInt(ctx.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                                            val valueWritten = Settings.System.putInt(ctx.contentResolver, Settings.System.SCREEN_BRIGHTNESS, value)
                                            modeWritten && valueWritten
                                        }
                                    }.getOrDefault(false)
                                }
                            ),
                            "Bluebird"
                        )
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                                val level = when (message.messageLevel()) {
                                    ConsoleMessage.MessageLevel.ERROR -> "ERROR"
                                    ConsoleMessage.MessageLevel.WARNING -> "WARN"
                                    ConsoleMessage.MessageLevel.TIP -> "DEBUG"
                                    else -> "INFO"
                                }
                                logger.log(app.id, level, "JS ${message.sourceId()}:${message.lineNumber()} ${message.message()}")
                                return true
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }

                            override fun onReceivedError(
                                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    logger.log(app.id, "ERROR", "WebView ${error?.errorCode}: ${error?.description ?: "page load failed"} (${request.url})")
                                    isLoading = false
                                }
                            }

                            override fun onReceivedHttpError(
                                view: WebView?, request: WebResourceRequest?, response: WebResourceResponse?
                            ) {
                                if (response != null && response.statusCode >= 400) {
                                    logger.log(app.id, "WARN", "HTTP ${response.statusCode} ${request?.url}")
                                }
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val uri = request?.url ?: return true
                                if (isInsidePackage(uri, root)) return false
                                logger.log(app.id, "WARN", "Blocked navigation outside package: $uri")
                                return true
                            }

                            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                                val uri = request?.url ?: return null
                                val scheme = uri.scheme?.lowercase(Locale.ROOT)
                                if (scheme == "http" || scheme == "https") {
                                    if (!permissions.isGranted(app.id, BpkCapability.INTERNET)) {
                                        logger.log(app.id, "WARN", "Blocked network request without Internet permission: $uri")
                                        return WebResourceResponse(
                                            "text/plain", "UTF-8", 403, "Permission denied",
                                            mapOf("Access-Control-Allow-Origin" to "*"),
                                            ByteArrayInputStream("BPK Internet permission is disabled".toByteArray())
                                        )
                                    }
                                }
                                if (scheme == "file" && !isInsidePackage(uri, root)) {
                                    logger.log(app.id, "WARN", "Blocked file access outside package: $uri")
                                    return WebResourceResponse("text/plain", "UTF-8", 403, "Forbidden", emptyMap(), ByteArrayInputStream(ByteArray(0)))
                                }
                                return null
                            }
                        }
                        webViewRef = this
                        logger.log(app.id, "INFO", "Application runtime started: ${app.name} (${app.version})")
                        loadUrl(entryFile.toURI().toString())
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopStart),
                    color = Color.Unspecified
                )
            }
        } else {
            Text(
                text = "Application entry point is invalid. Check the installation and app logs.",
                color = text,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

private fun isInsidePackage(uri: Uri, root: File): Boolean {
    if (!uri.scheme.equals("file", ignoreCase = true)) return false
    val file = runCatching { File(uri.path ?: return false).canonicalFile }.getOrNull() ?: return false
    return file.path == root.path || file.path.startsWith(root.path + File.separator)
}

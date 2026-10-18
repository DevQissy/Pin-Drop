package com.devqissy.pindrop.network

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

private const val WEBVIEW_TIMEOUT_MS = 15_000L
private const val PAGE_SETTLE_DELAY_MS = 1500L

class SafeWebAppInterface(private val onHtmlReceived: (String) -> Unit) {
    @JavascriptInterface
    fun processHTML(html: String) {
        onHtmlReceived(html)
    }

    @JavascriptInterface
    override fun toString(): String = "."
}

suspend fun getHtmlFromWebView(context: Context, url: String): String {
    return withTimeoutOrNull(WEBVIEW_TIMEOUT_MS) {
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                var webView: WebView? = null
                var isResumed = false

                fun safeResume(html: String) {
                    if (!isResumed && continuation.isActive) {
                        isResumed = true
                        continuation.resume(html) {}
                    }
                    webView?.post {
                        webView?.apply {
                            stopLoading()
                            clearCache(true)
                            clearHistory()
                            removeAllViews()
                            destroy()
                        }
                        webView = null
                    }
                }

                try {
                    webView = WebView(context.applicationContext).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (Mobile)"
                            setSupportZoom(false)
                            builtInZoomControls = false
                            displayZoomControls = false
                            loadWithOverviewMode = true
                            useWideViewPort = true
                        }

                        addJavascriptInterface(SafeWebAppInterface { html ->
                            safeResume(html)
                        }, "Android")

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                if (!continuation.isActive) return

                                view?.postDelayed({
                                    if (continuation.isActive) {
                                        view.loadUrl(
                                            "javascript:window.Android.processHTML(document.documentElement.outerHTML);"
                                        )
                                    }
                                }, PAGE_SETTLE_DELAY_MS)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true && continuation.isActive) {
                                    safeResume("")
                                }
                            }
                        }

                        loadUrl(url)
                    }
                } catch (e: Exception) {
                    safeResume("")
                }

                continuation.invokeOnCancellation {
                    webView?.post {
                        webView?.apply {
                            stopLoading()
                            clearCache(true)
                            clearHistory()
                            removeAllViews()
                            destroy()
                        }
                        webView = null
                    }
                }
            }
        }
    } ?: ""
}
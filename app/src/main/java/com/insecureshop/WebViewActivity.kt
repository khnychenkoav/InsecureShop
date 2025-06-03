package com.insecureshop

import android.net.Uri
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.insecureshop.util.Prefs
import androidx.core.net.toUri

class WebViewActivity : AppCompatActivity() {

    private val TRUSTED_HOST = Config.TRUSTED_HOST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        setSupportActionBar(toolbar)
        title = getString(R.string.webview)

        val webview = findViewById<WebView>(R.id.webview)

        webview.settings.javaScriptEnabled = false
        webview.settings.allowFileAccess = false
        webview.settings.allowContentAccess = false
        webview.settings.allowUniversalAccessFromFileURLs = false
        webview.settings.allowFileAccessFromFileURLs = false
        webview.settings.loadWithOverviewMode = true
        webview.settings.useWideViewPort = true

        webview.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView, handler: SslErrorHandler, error: android.net.http.SslError
            ) {
                handler.cancel()
                finish()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean {
                val uri: Uri = request.url
                return if (uri.host == TRUSTED_HOST) {
                    false
                } else {
                    finish()
                    true
                }
            }
        }

        val uri: Uri? = intent.data
        if (uri != null) {
            var data: String? = null
            when (uri.path) {
                "/web" -> {
                    data = uri.getQueryParameter("url")
                }
                "/webview" -> {
                    val candidate = uri.getQueryParameter("url")
                    if (candidate != null && candidate.toUri().host == TRUSTED_HOST) {
                        data = candidate
                    }
                }
            }

            if (data.isNullOrBlank()) {
                finish()
                return
            }

            val finalUri = data.toUri()
            if (finalUri.host == TRUSTED_HOST) {
                webview.loadUrl(data)
                Prefs.getInstance(this).data = data
            } else {
                finish()
            }
        } else {
            finish()
        }
    }
}

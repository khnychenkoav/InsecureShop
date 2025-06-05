package com.insecureshop

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_product_list.*
import androidx.core.net.toUri


class WebView2Activity : AppCompatActivity() {

    private val TRUSTED_HOST = Config.TRUSTED_HOST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        setSupportActionBar(toolbar)
        title = getString(R.string.webview)

        val extraIntent = intent.getParcelableExtra<Intent>("extra_intent")
        if (extraIntent != null) {
            startActivity(extraIntent)
            finish()
            return
        }

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
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val uri: Uri = request.url
                val currentHost = uri.host
                val expectedHost1 = Config.SECOND_HOST
                val expectedHost2 = Config.TRUSTED_HOST
                if (currentHost != null &&
                    (currentHost.equals(expectedHost1, ignoreCase = true) ||
                            currentHost.equals(expectedHost2, ignoreCase = true))) {
                    return false
                }

                finish()
                return true
            }
        }

        val incomingUri: String? = when {
            !intent.dataString.isNullOrBlank() -> intent.dataString
            !intent.data?.getQueryParameter("url").isNullOrBlank() ->
                intent.data?.getQueryParameter("url")
            !intent.extras?.getString("url").isNullOrEmpty() ->
                intent.extras?.getString("url")
            else -> null
        }

        if (!incomingUri.isNullOrBlank()) {
            val parsed = incomingUri.toUri()
            if (parsed.host == TRUSTED_HOST) {
                webview.loadUrl(incomingUri)
            } else {
                finish()
            }
        } else {
            webview.loadUrl(Config.WEBSITE_DOMAIN)
        }
    }
}

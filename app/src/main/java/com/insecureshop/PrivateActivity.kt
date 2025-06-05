package com.insecureshop

import android.net.Uri
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.insecureshop.Config.TRUSTED_HOST
import com.insecureshop.Config.USER_AGENT
import com.insecureshop.util.Prefs
import kotlinx.android.synthetic.main.activity_product_list.*


class PrivateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private)
        setSupportActionBar(toolbar)
        title = getString(R.string.webview)

        val webview = findViewById<WebView>(R.id.webview)

        webview.settings.allowFileAccess = false
        webview.settings.allowContentAccess = false
        webview.settings.allowUniversalAccessFromFileURLs = false
        webview.settings.allowFileAccessFromFileURLs = false

        webview.settings.loadWithOverviewMode = true
        webview.settings.useWideViewPort = true

        webview.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: android.net.http.SslError
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
                val expectedHost1 = "insecureshopapp.com"
                val expectedHost2 = "www.insecureshopapp.com"
                if (currentHost != null &&
                    (currentHost.equals(expectedHost1, ignoreCase = true) ||
                            currentHost.equals(expectedHost2, ignoreCase = true))) {
                    return false
                }

                finish()
                return true
            }
        }

        var data: String? = intent.getStringExtra("url")
        if (data.isNullOrBlank()) {
            data = Config.WEBSITE_DOMAIN
        }
        webview.loadUrl(data)
        Prefs.getInstance(this).data = data
    }
}

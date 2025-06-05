package com.insecureshop

import android.content.ComponentName
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
import java.util.Locale


class WebView2Activity : AppCompatActivity() {

    private val TRUSTED_HOST = Config.TRUSTED_HOST

    private val ALLOWED_INTERNAL_ACTIVITIES = setOf<String>(
        "com.insecureshop.SomeSafeInternalActivity"
    )

    private val ALLOWED_ACTIONS = setOf<String>(
        Intent.ACTION_VIEW
    )

    private val ALLOWED_HOSTS_FOR_VIEW_ACTION = setOf(
        Config.TRUSTED_HOST.toLowerCase(Locale.ROOT),
        Config.SECOND_HOST.toLowerCase(Locale.ROOT)
    )

    private val ALLOWED_URI_SCHEMES_FOR_VIEW_ACTION = setOf("http", "https")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        setSupportActionBar(toolbar)
        title = getString(R.string.webview)
        val currentIntent = intent
        val extraIntent = intent.getParcelableExtra<Intent>("extra_intent")
        if (extraIntent != null) {
            if (isIntentSafeToStart(extraIntent)) {
                try {
                    val intentToStart = Intent(extraIntent)
                    intentToStart.removeFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                    )
                    intentToStart.clipData = null
                    startActivity(intentToStart)
                } catch (e: Exception) {
                }
            }
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
    private fun isIntentSafeToStart(intentToValidate: Intent): Boolean {
        val component: ComponentName? = intentToValidate.component
        if (component != null) {
            if (component.packageName != this.packageName) {
                return false
            }
            if (ALLOWED_INTERNAL_ACTIVITIES.isNotEmpty() && !ALLOWED_INTERNAL_ACTIVITIES.contains(component.className)) {
                return false
            }
            return true
        } else {
            val action = intentToValidate.action
            if (action != null) {
                if (ALLOWED_ACTIONS.isNotEmpty() && !ALLOWED_ACTIONS.contains(action)) {
                    return false
                }
                if (action == Intent.ACTION_VIEW) {
                    val dataUri = intentToValidate.data
                    if (dataUri != null) {
                        val scheme = dataUri.scheme?.toLowerCase(Locale.ROOT)
                        if (scheme == null || !ALLOWED_URI_SCHEMES_FOR_VIEW_ACTION.contains(scheme)) {
                            return false
                        }
                        if (dataUri.host != null) {
                            val targetHost = dataUri.host?.toLowerCase(Locale.ROOT)
                            if (targetHost == null || !ALLOWED_HOSTS_FOR_VIEW_ACTION.contains(targetHost)) {
                                return false
                            }
                        }
                        return true
                    } else {
                        return false
                    }
                }
                return true
            } else {
                return false
            }
        }
    }
}

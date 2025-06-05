package com.insecureshop

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.insecureshop.util.Prefs
import kotlinx.android.synthetic.main.activity_product_list.toolbar
import androidx.core.net.toUri

class WebViewActivity : AppCompatActivity() {

    private val TAG = "WebViewActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity started.")
        setContentView(R.layout.activity_webview)
        setSupportActionBar(toolbar)
        title = getString(R.string.webview)

        val webview = findViewById<WebView>(R.id.webview)

        Log.d(TAG, "onCreate: Configuring WebView settings.")
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
                Log.e(TAG, "onReceivedSslError: SSL Error: ${error.toString()} for URL: ${error.url}")
                handler.cancel()
                Log.d(TAG, "onReceivedSslError: Calling finish() due to SSL error.")
                finish()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean {
                val uri: Uri = request.url
                val currentHost = uri.host
                val expectedHost1 = "insecureshopapp.com"
                val expectedHost2 = "www.insecureshopapp.com"

                Log.d(TAG, "shouldOverrideUrlLoading: Requested URL: ${request.url}")
                Log.d(TAG, "shouldOverrideUrlLoading: Current host: $currentHost")

                if (currentHost != null &&
                    (currentHost.equals(expectedHost1, ignoreCase = true) ||
                            currentHost.equals(expectedHost2, ignoreCase = true))) {
                    Log.i(TAG, "shouldOverrideUrlLoading: Allowing URL (host matched): ${request.url}")
                    return false
                }

                Log.w(TAG, "shouldOverrideUrlLoading: Denying URL (host mismatch). Calling finish(). URL: ${request.url}")
                finish()
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.i(TAG, "onPageFinished: Page loading finished for URL: $url")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                val failingUrl = request?.url?.toString()
            }
        }

        val extraUrl = intent.getStringExtra("url")
        Log.d(TAG, "onCreate: extraUrl from intent: '$extraUrl'")

        if (!extraUrl.isNullOrBlank()) {
            Log.i(TAG, "onCreate: Processing extraUrl: '$extraUrl'")
            val parsedExtraUrl = extraUrl.toUri()
            val extraUrlHost = parsedExtraUrl.host

            if (extraUrlHost != null &&
                (extraUrlHost.equals("insecureshopapp.com", ignoreCase = true) ||
                        extraUrlHost.equals("www.insecureshopapp.com", ignoreCase = true))) {
                Log.i(TAG, "onCreate: Host for extraUrl ('$extraUrlHost') is trusted. Loading URL: $extraUrl")
                webview.loadUrl(extraUrl)
                Prefs.getInstance(this).data = extraUrl
            } else {
                Log.w(TAG, "onCreate: Host for extraUrl ('$extraUrlHost') is NOT trusted. Calling finish().")
                finish()
            }
            return
        }

        Log.d(TAG, "onCreate: extraUrl was null or blank. Checking intent.data.")
        val dataUri: Uri? = intent.data
        Log.d(TAG, "onCreate: dataUri from intent: $dataUri")

        if (dataUri != null) {
            Log.i(TAG, "onCreate: Processing dataUri: $dataUri")
            val loadedUrlFromData: String? = when (dataUri.path) {
                "/web" -> {
                    val urlParam = dataUri.getQueryParameter("url")
                    Log.d(TAG, "onCreate: dataUri path '/web', urlParam: '$urlParam'")
                    urlParam
                }
                "/webview" -> {
                    val urlParam = dataUri.getQueryParameter("url")
                    Log.d(TAG, "onCreate: dataUri path '/webview', urlParam: '$urlParam'")
                    if (urlParam != null) {
                        val candidateUri = urlParam.toUri()
                        val candidateHost = candidateUri.host
                        if (candidateHost != null &&
                            (candidateHost.equals("insecureshopapp.com", ignoreCase = true) ||
                                    candidateHost.equals("www.insecureshopapp.com", ignoreCase = true))) {
                            Log.i(TAG, "onCreate: Host for dataUri candidate ('$candidateHost') is trusted.")
                            urlParam
                        } else {
                            Log.w(TAG, "onCreate: Host for dataUri candidate ('$candidateHost') is NOT trusted.")
                            null
                        }
                    } else {
                        null
                    }
                }
                else -> {
                    Log.w(TAG, "onCreate: dataUri path ('${dataUri.path}') is unknown.")
                    null
                }
            }

            if (loadedUrlFromData.isNullOrBlank()) {
                Log.w(TAG, "onCreate: loadedUrlFromData is null or blank. Calling finish().")
                finish()
                return
            }

            val finalUri = loadedUrlFromData.toUri()
            val finalHost = finalUri.host
            if (finalHost != null &&
                (finalHost.equals("insecureshopapp.com", ignoreCase = true) ||
                        finalHost.equals("www.insecureshopapp.com", ignoreCase = true))) {
                Log.i(TAG, "onCreate: Host for loadedUrlFromData ('$finalHost') is trusted. Loading URL: $loadedUrlFromData")
                webview.loadUrl(loadedUrlFromData)
                Prefs.getInstance(this).data = loadedUrlFromData
            } else {
                Log.w(TAG, "onCreate: Host for loadedUrlFromData ('$finalHost') is NOT trusted. Calling finish().")
                finish()
            }
        } else {

            Log.e(TAG, "onCreate: Both extraUrl and dataUri are null or blank. No URL to load. Calling finish().")
            finish()
        }
        Log.d(TAG, "onCreate: Activity setup finished.")
    }
}
package com.insecureshop.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.insecureshop.WebViewActivity

class ProductDetailBroadCast : BroadcastReceiver() {

    private val TAG = "ProductDetail_BroadCast"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: Broadcast received! Action: ${intent?.action}")

        val url = intent?.getStringExtra("url")
        Log.d(TAG, "onReceive: URL from intent extras: '$url'")

        if (url.isNullOrBlank()) {
            Log.e(TAG, "onReceive: URL is null or blank. Cannot start WebViewActivity.")
            return
        }

        if (context == null) {
            Log.e(TAG, "onReceive: Context is null. Cannot start WebViewActivity.")
            return
        }

        Log.i(TAG, "onReceive: Preparing to start WebViewActivity with URL: $url")
        try {
            val webViewIntent = Intent(context, WebViewActivity::class.java).apply {
                putExtra("url", url)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webViewIntent)
            Log.i(TAG, "onReceive: startActivity(webViewIntent) called successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "onReceive: EXCEPTION while trying to start WebViewActivity!", e)
        }
    }
}
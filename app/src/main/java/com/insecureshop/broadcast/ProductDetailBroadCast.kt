package com.insecureshop.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.insecureshop.Config
import com.insecureshop.WebViewActivity
import androidx.core.net.toUri

class ProductDetailBroadCast : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val url = intent?.getStringExtra("url") ?: return
        val webViewIntent = Intent(context, WebViewActivity::class.java).apply {
            putExtra("url", url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context?.startActivity(webViewIntent)
    }
}
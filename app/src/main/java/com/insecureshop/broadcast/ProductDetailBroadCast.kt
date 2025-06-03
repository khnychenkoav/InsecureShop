package com.insecureshop.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.insecureshop.Config

class ProductDetailBroadCast : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val webViewIntent = Intent("com.insecureshop.action.WEBVIEW")
        webViewIntent.putExtra("url",Config.WEBSITE_DOMAIN)
        context?.startActivity(webViewIntent)
    }
}
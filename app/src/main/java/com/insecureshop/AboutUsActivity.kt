package com.insecureshop

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.insecureshop.util.Prefs
import kotlinx.android.synthetic.main.activity_about_us.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class AboutUsActivity : AppCompatActivity() {

    private lateinit var receiver: CustomReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        receiver = CustomReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            receiver,
            IntentFilter("com.insecureshop.CUSTOM_INTENT")
        )
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onDestroy()
    }

    fun onSendData(view: View) {
        val userName = Prefs.username!!
        //val password = Prefs.password!!

        val intent = Intent("com.insecureshop.action.BROADCAST")
        intent.putExtra("username", userName)
        //intent.putExtra("password", password)
        intent.setPackage(this.packageName)
        sendBroadcast(intent, "com.insecureshop.permission.SEND_PROTECTED_BROADCAST")

        textView.text = "InsecureShop is an intentionally designed vulnerable android app built in Kotlin."

    }


}
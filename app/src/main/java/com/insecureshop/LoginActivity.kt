package com.insecureshop

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.insecureshop.databinding.ActivityLoginBinding
import com.insecureshop.util.Prefs
import com.insecureshop.util.Util
import androidx.core.net.toUri

class LoginActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityLoginBinding
    private val TARGET_APP_PACKAGE_NAME = "com.insecureshopapp.trusted"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        requestPermissions(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            100
        )
    }


    fun onLogin(view: View) {
        val username = mBinding.edtUserName.text.toString().trim()
        val password = mBinding.edtPassword.text.toString().trim()
        //Log.d("LoginActivity", "userName = $username")
        //Log.d("LoginActivity", "password = $password")
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and password cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }
        val auth = Util.verifyUserNamePassword(this, username, password)
        if (auth) {
            Prefs.getInstance(applicationContext).username = username
            //Prefs.getInstance(applicationContext).password = password
            Util.saveProductList(this)
            val intent = Intent(this, ProductListActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Log.w("LoginActivity", "Login attempt failed for username: $username.")
            Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_LONG).show()
            tryToLaunchTargetApp()
        }
    }

    private fun tryToLaunchTargetApp() {
        val launchIntent = packageManager.getLaunchIntentForPackage(TARGET_APP_PACKAGE_NAME)
        if (launchIntent != null) {
            Log.i("LoginActivity", "Target app ($TARGET_APP_PACKAGE_NAME) is installed. Attempting to launch.")
            try {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
            } catch (e: Exception) {
                Log.e("LoginActivity", "Could not launch target app $TARGET_APP_PACKAGE_NAME", e)
            }
        } else {
            Log.i("LoginActivity", "Target app ($TARGET_APP_PACKAGE_NAME) is not installed.")
            redirectToPlayStoreForTargetApp()
        }
    }

    private fun redirectToPlayStoreForTargetApp() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW,
                "market://details?id=$TARGET_APP_PACKAGE_NAME".toUri()))
        } catch (e: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$TARGET_APP_PACKAGE_NAME".toUri()))
        }
    }

    fun onRegister(view: View) {
        val username = mBinding.edtUserName.text.toString().trim()
        val password = mBinding.edtPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите логин и пароль для регистрации", Toast.LENGTH_SHORT)
                .show()
            return
        }

        Util.registerUser(this, username, password)
        Toast.makeText(
            this,
            "Пользователь '$username' успешно зарегистрирован",
            Toast.LENGTH_SHORT
        ).show()

        startActivity(Intent(this, ProductListActivity::class.java))
        finish()
    }
}

package com.insecureshop

import android.Manifest
import android.content.Context
import android.content.Intent
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

class LoginActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityLoginBinding

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

        Log.d("LoginActivity", "userName = $username")
        Log.d("LoginActivity", "password = $password")

        val auth = Util.verifyUserNamePassword(this, username, password)

        if (auth) {
            Prefs.getInstance(applicationContext).username = username
            Prefs.getInstance(applicationContext).password = password

            Util.saveProductList(this)

            val intent = Intent(this, ProductListActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            for (info in packageManager.getInstalledPackages(0)) {
                val packageName = info.packageName
                if (packageName.startsWith("com.insecureshopapp")) {
                    try {
                        val packageContext = createPackageContext(
                            packageName,
                            Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
                        )
                        val value: Any = packageContext.classLoader
                            .loadClass("com.insecureshopapp.MainInterface")
                            .getMethod("getInstance", Context::class.java)
                            .invoke(null, this)!!
                        Log.d("LoginActivity", "Loaded value from external app: $value")

                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                }
            }

            Toast.makeText(
                applicationContext,
                "Invalid username and password",
                Toast.LENGTH_LONG
            ).show()
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

package com.insecureshop.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object Prefs {

    private lateinit var sharedpreferences: SharedPreferences
    private var prefs : Prefs? = null

    fun getInstance(context: Context): Prefs {
        if (prefs == null) {
            sharedpreferences =
                context.getSharedPreferences("Prefs", Context.MODE_PRIVATE)
            prefs = this
        }
        return prefs!!
    }

    var data: String?
        get() = sharedpreferences.getString("data","")
        set(value) {
            sharedpreferences.edit() { putString("data", value) }
        }

    var username: String?
        get() = sharedpreferences.getString("username","")
        set(value) {
            sharedpreferences.edit() { putString("username", value) }
        }

    var password: String?
        get() = sharedpreferences.getString("password","")
        set(value) {
            sharedpreferences.edit() { putString("password", value) }
        }

    var productList: String?
        get() = sharedpreferences.getString("productList","")
        set(value) {
            sharedpreferences.edit() { putString("productList", value) }
        }


    fun clearAll(){
        sharedpreferences.edit().clear().apply()
    }
}
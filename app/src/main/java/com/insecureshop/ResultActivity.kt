package com.insecureshop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ResultActivity"
        const val EXTRA_INPUT_DATA = Config.EXTRA_INPUT_DATA_RESULT_ACTIVITY
        const val EXTRA_PROCESSED_DATA = Config.EXTRA_PROCESSED_DATA_RESULT_ACTIVITY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inputData = intent.getStringExtra(EXTRA_INPUT_DATA)
        val processedData = if (inputData != null) {
            "Processed: $inputData - OK"
        } else {
            "No input data received - Processed with default"
        }
        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_PROCESSED_DATA, processedData)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun bundleToString(bundle: Bundle?): String {
        if (bundle == null) return "null"
        val stringBuilder = StringBuilder("Bundle[")
        for (key in bundle.keySet()) {
            stringBuilder.append(" $key=${bundle.get(key)};")
        }
        stringBuilder.append(" ]")
        return stringBuilder.toString()
    }
}
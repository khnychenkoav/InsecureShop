package com.insecureshop

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.OpenableColumns
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.InputStream
import java.io.OutputStream


class ChooserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooser)
        if (intent.extras != null) {
            val uriInput = intent.getParcelableExtra<Parcelable>("android.intent.extra.STREAM") as? Uri
            if (uriInput != null) {
                val originalName = getFilename(uriInput) ?: "unknown_file"
                makeTempCopy(uriInput, originalName)
            }
        }
    }
    private fun makeTempCopy(fileUri: Uri, originalFilename: String): Uri? {
        return try {
            val baseDir: File? = getExternalFilesDir("insecureshop")
            if (baseDir == null) return null
            if (!baseDir.exists()) {
                baseDir.mkdirs()
            }
            val fileTemp = File(baseDir, originalFilename)
            if (!fileTemp.exists()) {
                fileTemp.createNewFile()
            }
            val outUri: Uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "com.insecureshop.file_provider",
                fileTemp
            )
            val inputStream: InputStream? = contentResolver.openInputStream(fileUri)
            val outputStream: OutputStream? = contentResolver.openOutputStream(outUri)
            val buffer = ByteArray(8 * 1024)
            var bytesRead: Int
            while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
                if (bytesRead > 0) {
                    outputStream?.write(buffer, 0, bytesRead)
                }
            }
            inputStream?.close()
            outputStream?.close()
            outUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFilename(uri: Uri): String? {
        var fileName: String? = null
        val scheme = uri.scheme
        if (scheme == "file") {
            fileName = uri.lastPathSegment
        } else if (scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                    fileName = it.getString(index)
                }
            }
        }
        return fileName
    }
}
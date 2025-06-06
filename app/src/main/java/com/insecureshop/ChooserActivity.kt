package com.insecureshop

import android.net.Uri
import android.os.Bundle
import android.content.Intent
import android.os.Parcelable
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID

class ChooserActivity : AppCompatActivity() {

    private val TAG = "ChooserActivity"
    private val MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024
    private val ALLOWED_MIME_TYPES = setOf(
        "image/jpeg", "image/png", "image/gif",
        "text/plain",
        "application/pdf"
    )
    private val TARGET_SUBDIRECTORY = "imported_files"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate called with intent: $intent")

        if (intent.action == Intent.ACTION_SEND && intent.type != null) {
            val uriInput = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
            if (uriInput != null) {
                Log.i(TAG, "Received URI from intent: $uriInput")
                handleIncomingUri(uriInput)
            } else {
                Log.w(TAG, "No URI found in EXTRA_STREAM.")
                Toast.makeText(this, "No file provided to share.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "Incorrect action or type. Action: ${intent.action}, Type: ${intent.type}")
        }
        finish()
    }

    private fun handleIncomingUri(uri: Uri) {
        val fileName = getOriginalFileName(uri) ?: UUID.randomUUID().toString()
        val fileSize = getFileSize(uri)
        val mimeType = contentResolver.getType(uri)?.toLowerCase(Locale.ROOT)
        Log.i(TAG, "File details: Name='$fileName', Size=$fileSize bytes, MIME='$mimeType'")
        if (mimeType == null || !isMimeTypeAllowed(mimeType)) {
            Log.w(TAG, "MIME type not allowed: $mimeType")
            Toast.makeText(this, "File type '$mimeType' is not supported.", Toast.LENGTH_LONG).show()
            return
        }
        if (fileSize == -1L) {
            Log.w(TAG, "Could not determine file size.")
            Toast.makeText(this, "Could not determine file size.", Toast.LENGTH_LONG).show()
            return
        }
        if (fileSize > MAX_FILE_SIZE_BYTES) {
            Log.w(TAG, "File size ($fileSize bytes) exceeds maximum allowed size ($MAX_FILE_SIZE_BYTES bytes).")
            Toast.makeText(this, "File is too large (max ${MAX_FILE_SIZE_BYTES / (1024*1024)}MB).", Toast.LENGTH_LONG).show()
            return
        }
        if (fileSize == 0L) {
            Log.w(TAG, "File is empty (0 bytes).")
            Toast.makeText(this, "File is empty.", Toast.LENGTH_LONG).show()
            return
        }

        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            ?: fileName.substringAfterLast('.', "")
        val safeOutputFileName = "${UUID.randomUUID()}${if (extension.isNotEmpty()) ".$extension" else ""}"

        Log.i(TAG, "Generated safe output file name: $safeOutputFileName")

        val copiedFileUri = copyFileToAppStorage(uri, safeOutputFileName)

        if (copiedFileUri != null) {
            Log.i(TAG, "File successfully copied to: $copiedFileUri")
            Toast.makeText(this, "'$fileName' imported successfully as '$safeOutputFileName'.", Toast.LENGTH_LONG).show()
        } else {
            Log.e(TAG, "Failed to copy file.")
            Toast.makeText(this, "Failed to import file.", Toast.LENGTH_LONG).show()
        }
    }

    private fun isMimeTypeAllowed(mimeType: String): Boolean {
        if (ALLOWED_MIME_TYPES.isEmpty()) {
            return true
        }
        return ALLOWED_MIME_TYPES.any { allowedType ->
            if (allowedType.endsWith("/*")) {
                mimeType.startsWith(allowedType.removeSuffix("/*"))
            } else {
                mimeType == allowedType
            }
        }
    }

    private fun getOriginalFileName(uri: Uri): String? {
        var fileName: String? = null
        if (uri.scheme == "content") {
            try {
                contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (displayNameIndex != -1) {
                            fileName = cursor.getString(displayNameIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting file name from content URI", e)
            }
        }
        if (fileName == null) {
            fileName = uri.lastPathSegment
        }
        return fileName?.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }

    private fun getFileSize(uri: Uri): Long {
        try {
            contentResolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
                return parcelFileDescriptor.statSize
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file size", e)
        }
        return -1L
    }

    private fun copyFileToAppStorage(sourceUri: Uri, outputFileName: String): Uri? {
        val targetDir = File(getExternalFilesDir(null), TARGET_SUBDIRECTORY)
        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                Log.e(TAG, "Failed to create target directory: $targetDir")
                return null
            }
        }
        val outputFile = File(targetDir, outputFileName)

        try {
            contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(4 * 1024) // 4KB buffer
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                }
            }
            Log.i(TAG, "File copied to: ${outputFile.absolutePath}")
            return androidx.core.content.FileProvider.getUriForFile(
                this,
                "com.insecureshop.file_provider",
                outputFile
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error copying file", e)
            outputFile.delete()
            return null
        }
    }
}
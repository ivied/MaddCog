package com.udev.maddcog

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream

fun Context.getFileFromUri(uri: Uri): File {
    val fileName: String = contentResolver.getFileName(uri)
    val file = File(externalCacheDir, fileName)
    file.createNewFile()
    FileOutputStream(file).use { outputStream ->
        uri.let {
            contentResolver.openInputStream(uri).use { inputStream ->
                inputStream?.copyTo(outputStream) // Simply reads input to output stream
                outputStream.flush()
            }
        }
    }
    return file
}

fun ContentResolver.getFileName(uri: Uri): String {
    var fileName: String? = getFileNameFromCursor(uri)
    if (fileName == null) {
        val fileExtension: String? = getFileExtension(uri)
        fileName = "temp_file" + if (fileExtension != null) ".$fileExtension" else ""
    } else if (!fileName.contains(".")) {
        val fileExtension: String? = getFileExtension(uri)
        fileName = "$fileName.$fileExtension"
    }
    return fileName
}

fun ContentResolver.getFileNameFromCursor(uri: Uri,): String? {
    val fileCursor: Cursor? = query(
        uri,
        arrayOf(
            OpenableColumns.DISPLAY_NAME
        ),
        null, null, null
    )
    var fileName: String? = null
    if (fileCursor != null && fileCursor.moveToFirst()) {
        val cIndex: Int = fileCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cIndex != -1) {
            fileName = fileCursor.getString(cIndex)
        }
    }
    return fileName
}

fun ContentResolver.getFileExtension(uri: Uri): String? {
    val fileType: String? = getType(uri)
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
}

package com.example.mapnote.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object Util {
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Code from chat GPT for resize the image quality and convert to base64 string
    fun fileToBase64(context: Context, uri: Uri): String? {
        return try {
            // Load the image
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Get the rotation from EXIF data
            val rotation = getRotationFromExif(context, uri)

            // Resize the image
            val resizedBitmap = originalBitmap?.let {
                val aspectRatio = it.width.toFloat() / it.height.toFloat()
                val (newWidth, newHeight) = if (it.width > it.height) {
                    Pair(1024, (1024 / aspectRatio).toInt())
                } else {
                    Pair((1024 * aspectRatio).toInt(), 1024)
                }
                Bitmap.createScaledBitmap(it, newWidth, newHeight, true)
            }

            // Rotate the bitmap if necessary
            val finalBitmap = resizedBitmap?.let { rotateBitmap(it, rotation) }

            // Convert to byte array and encode to Base64
            finalBitmap?.let {
                val byteArrayOutputStream = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun getRotationFromExif(context: Context, uri: Uri): Int {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val exif = inputStream?.let { ExifInterface(it) }
        inputStream?.close()
        return when (exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
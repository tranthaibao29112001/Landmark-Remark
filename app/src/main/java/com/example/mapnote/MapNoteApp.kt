package com.example.mapnote
import android.app.Application
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MapNoteApp : Application(){
    init {
        instance = this
    }
    companion object {
        private var instance: MapNoteApp? = null
        fun getUriPermission(uri: Uri){
            instance!!.applicationContext.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

    }


}
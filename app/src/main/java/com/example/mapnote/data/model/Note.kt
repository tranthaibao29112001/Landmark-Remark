package com.example.mapnote.data.model

import android.os.Parcelable
import androidx.room.Entity
import java.util.Date
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat


@Entity
@Parcelize
data class Note(
    var id: String = "",
    val title: String ="",
    val note: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val dateUpdated: Date = Date(),
    val imageBase64: String? = "",
    val userName: String = "",
    val userId: String = "",
): Parcelable

fun Note.getDay(): String{
    val outputFormat = SimpleDateFormat("HH:mm:ss dd/MM/yyyy")
    return outputFormat.format(dateUpdated)
}
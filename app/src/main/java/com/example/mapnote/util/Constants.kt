package com.example.mapnote.util

import com.example.mapnote.data.model.Note
import java.util.Date


object Constants {
const val NAVIGATION_MAP_POSITION = "map/{lat}/{lng}"
const val NAVIGATION_NOTES_CREATE = "notesCreated"
const val NAVIGATION_NOTES_CREATE_POSITION = "notesCreated/{lat}/{lng}/{noteId}"
const val NAVIGATION_Lat_Argument = "lat"
const val NAVIGATION_Lng_Argument = "lng"
const val NAVIGATION_NoteId_Argument = "noteId"
const val ROUTE_SIGNUP = "route_signup"
const val ROUTE_LOGIN = "route_login"
const val NOTE = "note"

    fun List<Note>?.orPlaceHolderList(): List<Note> {
        fun placeHolderList(): List<Note> {
            return listOf(Note(id = "", title = "No Notes Found", note = "Please create a note.", dateUpdated = Date()))
        }
        return if (this != null && this.isNotEmpty()){
            this
        } else placeHolderList()
    }
}
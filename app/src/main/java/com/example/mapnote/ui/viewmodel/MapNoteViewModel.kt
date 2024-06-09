package com.example.mapnote.ui.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.*
import com.example.mapnote.data.model.Note
import com.example.mapnote.data.repository.NoteRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MapNoteViewModel @Inject constructor(
    val noteRepository: NoteRepository

) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>>
        get() = _notes

    private val _addNote = MutableStateFlow<String>("")
    val addNote: StateFlow<String>
        get() = _addNote

    private val _updateNote = MutableStateFlow<String>("")
    val updateNote: StateFlow<String>
        get() = _updateNote

    private val _deleteNote = MutableStateFlow<String>("")
    val deleteNote: StateFlow<String>
        get() = _deleteNote

    private val _getNoteById = MutableStateFlow<Note?>(null)
    val getNoteById: StateFlow<Note?>
        get() = _getNoteById

    private val _currentPosition = MutableStateFlow<LatLng?>(null)
    val currentPosition: StateFlow<LatLng?>
        get() = _currentPosition


    fun getNotes() {
        noteRepository.getNotes {
            _notes.value = it
        }
    }
    fun addNote(note: Note){
        noteRepository.addNote(note) { _addNote.value = it }
    }
    fun updateNote(note: Note){
        noteRepository.updateNote(note) { _updateNote.value = it }
    }
    fun deleteNote(note: Note){
        noteRepository.addNote(note) { _deleteNote.value = it }
    }
    fun getNote(noteId: String){
        return noteRepository.getNoteById(noteId) { _getNoteById.value = it }
    }

    // get the current position of the device
    @SuppressLint("MissingPermission")
    fun getDeviceLocation(
        fusedLocationProviderClient: FusedLocationProviderClient
    ) {
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.let { location ->
                        _currentPosition.value = LatLng(location.latitude, location.longitude)
                    } ?: run {
                        _currentPosition.value = null
                    }
                } else {
                    _currentPosition.value = null
                }
            }
        } catch (e: SecurityException) {
            _currentPosition.value = null
        }
    }

}

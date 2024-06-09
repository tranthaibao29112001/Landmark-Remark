package com.example.mapnote.data.repository

import com.example.mapnote.data.model.Note

interface NoteRepository {
    fun getNotes(result: (List<Note>) -> Unit)
    fun addNote(note: Note, result: (String) -> Unit)
    fun updateNote(note: Note, result: (String) -> Unit)
    fun deleteNote(note: Note, result: (String) -> Unit)
    fun getNoteById(noteId: String, result: (Note?) -> Unit)
}
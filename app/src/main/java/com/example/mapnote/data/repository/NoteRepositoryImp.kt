package com.example.mapnote.data.repository

import com.example.mapnote.data.model.Note
import com.example.mapnote.util.Constants
import com.google.firebase.firestore.FirebaseFirestore

class NoteRepositoryImp(
    val database: FirebaseFirestore
) : NoteRepository {

    override fun getNotes(result: (List<Note>) -> Unit) {
        database.collection(Constants.NOTE)
            .get()
            .addOnSuccessListener {
                val notes = arrayListOf<Note>()
                for (document in it) {
                    val note = document.toObject(Note::class.java)
                    notes.add(note)
                }
                result.invoke(
                    notes
                )
            }
            .addOnFailureListener {
                result.invoke(
                    emptyList()
                )
            }
    }

    override fun addNote(note: Note, result: (String) -> Unit) {
        val document = database.collection(Constants.NOTE).document()
        note.id = document.id
        document
            .set(note)
            .addOnSuccessListener {
                result.invoke(
                    "Note has been created successfully"
                )
            }
            .addOnFailureListener {
                result.invoke(
                    "Note has not been created successfully"
                )
            }
    }

    override fun updateNote(note: Note, result: (String) -> Unit) {
        val document = database.collection(Constants.NOTE).document(note.id)
        document
            .set(note)
            .addOnSuccessListener {
                result.invoke(
                    "Note has been update successfully"
                )
            }
            .addOnFailureListener {
                result.invoke(
                    "Note has not been updated successfully"
                )
            }
    }

    override fun deleteNote(note: Note, result: (String) -> Unit) {
        val document = database.collection(Constants.NOTE).document(note.id)
        document
            .delete()
            .addOnSuccessListener {
                result.invoke(
                    "Note has been deleted successfully"
                )
            }
            .addOnFailureListener {
                result.invoke(
                    "Note has not been deleted successfully"
                )
            }
    }

    override fun getNoteById(noteId: String, result: (Note?) -> Unit) {
        val document = database.collection(Constants.NOTE).document(noteId)
        document
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val note = documentSnapshot.toObject(Note::class.java)
                if (note != null) {
                    result.invoke(
                        note
                    )
                }
                else{
                    result.invoke(null)
                }
            }
            .addOnFailureListener {
                result.invoke(null
                )
            }
    }
}
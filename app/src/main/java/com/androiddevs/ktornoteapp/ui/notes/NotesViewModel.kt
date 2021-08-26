package com.androiddevs.ktornoteapp.ui.notes

import androidx.lifecycle.*
import com.androiddevs.ktornoteapp.data.local.entities.LocallyDeletedNoteID
import com.androiddevs.ktornoteapp.data.local.entities.Note
import com.androiddevs.ktornoteapp.data.other.Event
import com.androiddevs.ktornoteapp.data.other.Resource
import com.androiddevs.ktornoteapp.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(private val noteRepository: NoteRepository) : ViewModel() {

    private val _forceUpdate = MutableLiveData(false)
    private val _allNotes = _forceUpdate.switchMap {
        noteRepository.getAllNotes().asLiveData(viewModelScope.coroutineContext)
    }.switchMap {
        // one time event to not repeat on device rotation
        MutableLiveData(Event(it))
    }

    val allNotes: LiveData<Event<Resource<List<Note>>>> = _allNotes

    fun syncAllNotes() = _forceUpdate.postValue(true)

    fun insertNote(note: Note) = viewModelScope.launch {
        noteRepository.insertNote(note)
    }

    fun deleteNote(noteID: String) = viewModelScope.launch {
        noteRepository.deleteNote(noteID)
    }

    fun deleteLocallyDeletedNoteID(deletedNoteID: String) = viewModelScope.launch {
        noteRepository.deleteLocallyDeletedNoteID(deletedNoteID)
    }
}
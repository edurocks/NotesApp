package com.androiddevs.ktornoteapp.ui.notedetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.ktornoteapp.data.other.Event
import com.androiddevs.ktornoteapp.data.other.Resource
import com.androiddevs.ktornoteapp.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(private val notesRepository: NoteRepository) : ViewModel() {

    private val _addOwnerStatus = MutableLiveData<Event<Resource<String>>>()
    val addOwnerStatus: LiveData<Event<Resource<String>>> = _addOwnerStatus

    fun addOwnerToNote(owner: String, noteID: String) {
        _addOwnerStatus.postValue(Event(Resource.loading(null)))
        if (owner.isEmpty() || noteID.isEmpty()) {
            _addOwnerStatus.postValue(Event(Resource.error("The owner can´t be empty", null)))
            return
        }

        viewModelScope.launch {
            val result = notesRepository.addOwnerToNote(owner, noteID)
            _addOwnerStatus.postValue(Event(result))
        }
    }

    fun observeNoteByID(noteID: String) = notesRepository.observeNoteByID(noteID)
}
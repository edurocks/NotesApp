package com.androiddevs.ktornoteapp.ui.addeditnote

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.androiddevs.ktornoteapp.R
import com.androiddevs.ktornoteapp.data.local.entities.Note
import com.androiddevs.ktornoteapp.data.other.Constants
import com.androiddevs.ktornoteapp.data.other.Status
import com.androiddevs.ktornoteapp.ui.BaseFragment
import com.androiddevs.ktornoteapp.ui.dialogs.ColorPickerDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_edit_note.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddEditNoteFragment : BaseFragment(R.layout.fragment_add_edit_note) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    private val viewModel: AddEditNoteViewModel by viewModels()
    private val args: AddEditNoteFragmentArgs by navArgs()
    private var curNote: Note? = null
    private var curNoteColor = Constants.DEFAULT_NOTE_COLOR
    private val FRAGMENT_TAG = "AddEditNoteFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.id.isNotEmpty()) {
            viewModel.getNoteById(args.id)
            subscribeToObservers()
        }

        //Survive device rotation to prevent destruct with this dialog
        if (savedInstanceState != null) {
            val colorPickerDialog = parentFragmentManager.findFragmentByTag(FRAGMENT_TAG) as ColorPickerDialogFragment?
            colorPickerDialog?.setPositiveListener {
                changeViewNoteColor(it)
            }
        }

        viewNoteColor.setOnClickListener {
            ColorPickerDialogFragment().apply {
                setPositiveListener {
                    changeViewNoteColor(it)
                }
            }.show(parentFragmentManager, FRAGMENT_TAG)
        }
    }

    private fun subscribeToObservers() {
        viewModel.note.observe(viewLifecycleOwner, {
            it?.getContentIfNotHandled()?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        val note = result.data!!
                        curNote = note
                        etNoteTitle.setText(note.title)
                        etNoteContent.setText(note.content)
                        changeViewNoteColor(note.color)
                    }
                    Status.ERROR -> {
                        showSnackbar(result.message ?: "Note not found")
                    }
                    Status.LOADING -> {}

                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        saveNote()
    }

    private fun changeViewNoteColor(colorString: String) {
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.circle_shape, null)
        drawable?.let {
            val wrappedDrawable = DrawableCompat.wrap(it)
            val color = Color.parseColor("#$colorString")
            DrawableCompat.setTint(wrappedDrawable, color)
            viewNoteColor.background = wrappedDrawable
            curNoteColor = colorString
        }
    }

    private fun saveNote() {
        val authEmail = sharedPref.getString(Constants.KEY_LOGGED_IN_EMAIL, Constants.NO_EMAIL) ?: Constants.NO_EMAIL

        val title = etNoteTitle.text.toString()
        val content = etNoteContent.text.toString()
        if (title.isEmpty() || content.isEmpty()) {
            return
        }

        val date = System.currentTimeMillis()
        val color = curNoteColor
        val id = curNote?.id ?: UUID.randomUUID().toString()
        val owners = curNote?.owners ?: listOf(authEmail)
        val note = Note(id = id, title = title, content = content, date = date, owners = owners, color = color)
        viewModel.insertNote(note)
    }
}
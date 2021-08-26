package com.androiddevs.ktornoteapp.ui.notedetail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.androiddevs.ktornoteapp.R
import com.androiddevs.ktornoteapp.data.local.entities.Note
import com.androiddevs.ktornoteapp.data.other.Constants
import com.androiddevs.ktornoteapp.data.other.Status
import com.androiddevs.ktornoteapp.ui.BaseFragment
import com.androiddevs.ktornoteapp.ui.dialogs.AddOwnerDialog
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.edit_text_email.*
import kotlinx.android.synthetic.main.fragment_note_detail.*

@AndroidEntryPoint
class NoteDetailFragment : BaseFragment(R.layout.fragment_note_detail) {

    private val viewModel: NoteDetailViewModel by viewModels()

    private val args: NoteDetailFragmentArgs by navArgs()
    private var curNote: Note? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        fabEditNote.setOnClickListener {
            findNavController().navigate(NoteDetailFragmentDirections.actionNoteDetailFragmentToAddEditNoteFragment(args.id))
        }

        if (savedInstanceState == null) {
            val addOwnerDialog = parentFragmentManager.findFragmentByTag(Constants.ADD_OWNER_DIALOG_TAG)
            as AddOwnerDialog?
            addOwnerDialog?.setPositiveListener {
                addOwnerToNote(it)
            }
        }
    }

    private fun setMarkdownText(text: String) {
        val markown = Markwon.create(requireContext())
        val markdown = markown.toMarkdown(text)
        markown.setParsedMarkdown(tvNoteContent, markdown)
    }

    private fun showAddOwnerDialog() {
        AddOwnerDialog().apply {
            setPositiveListener {
                addOwnerToNote(it)
            }
        }.show(parentFragmentManager, Constants.ADD_OWNER_DIALOG_TAG)
    }

    private fun addOwnerToNote(email: String) {
        curNote?.let { note ->
            viewModel.addOwnerToNote(email, note.id)
        }
    }

    private fun subscribeToObservers() {
        viewModel.observeNoteByID(args.id).observe(viewLifecycleOwner, {
            it?.let { note ->
                tvNoteTitle.text = note.title
                setMarkdownText(note.content)
                curNote = note
            } ?: showSnackbar("Note not found")
        })

        viewModel.addOwnerStatus.observe(viewLifecycleOwner, { event ->
            event?.getContentIfNotHandled()?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        addOwnerProgressBar.visibility = View.GONE
                        showSnackbar(result.data ?: "Sucessfully added owner to note")
                    }
                    Status.ERROR -> {
                        addOwnerProgressBar.visibility = View.GONE
                        showSnackbar(result.message ?: "An unknown error occured")
                    }
                    Status.LOADING -> {
                        addOwnerProgressBar.visibility = View.VISIBLE
                    }
                }
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.note_detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miAddOwner -> showAddOwnerDialog()
        }
        return super.onOptionsItemSelected(item)
    }
}
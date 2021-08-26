package com.androiddevs.ktornoteapp.ui.notes

import android.content.SharedPreferences
import android.graphics.Canvas
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.ktornoteapp.R
import com.androiddevs.ktornoteapp.data.other.Constants
import com.androiddevs.ktornoteapp.data.other.Status
import com.androiddevs.ktornoteapp.ui.BaseFragment
import com.androiddevs.ktornoteapp.ui.adapters.NoteAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_notes.*
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment : BaseFragment(R.layout.fragment_notes) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    private lateinit var noteAdapter: NoteAdapter

    private val viewModel: NotesViewModel by viewModels()

    private val swippingItem = MutableLiveData(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefreshLayout()
        subscribeToObservers()

        noteAdapter.setOnItemClickListene {
            findNavController().navigate(NotesFragmentDirections.actionNotesFragmentToNoteDetailFragment(it.id))
        }

        fabAddNote.setOnClickListener {
            findNavController().navigate(NotesFragmentDirections.actionNotesFragmentToAddEditNoteFragment(""))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_notes, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.miLogout -> logout()
        }
        return super.onOptionsItemSelected(item)
    }

    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                swippingItem.postValue(isCurrentlyActive)
            }
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.layoutPosition
            val note = noteAdapter.notes[position]
            viewModel.deleteNote(note.id)
            Snackbar.make(requireView(), "Note was successfully deleted", Snackbar.LENGTH_LONG).apply {
                setAction("Undo") {
                    viewModel.insertNote(note)
                    viewModel.deleteLocallyDeletedNoteID(note.id)
                }
                show()
            }
        }
    }

    private fun setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.syncAllNotes()
        }
    }

    private fun setupRecyclerView() = rvNotes.apply {
        noteAdapter = NoteAdapter()
        adapter = noteAdapter
        layoutManager = LinearLayoutManager(requireContext())
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this)
    }

    private fun subscribeToObservers() {
        viewModel.allNotes.observe(viewLifecycleOwner, {
            it?.let { event ->
                val result = event.peekContent()
                when(result.status) {
                    Status.SUCCESS -> {
                        noteAdapter.notes = result.data!!
                        swipeRefreshLayout.isRefreshing = false
                    }
                    Status.ERROR -> {
                        event.getContentIfNotHandled()?.let { errorResource ->
                            errorResource.message?.let { message ->
                                showSnackbar(message)
                            }
                        }
                        result.data?.let { notes ->
                            noteAdapter.notes = notes
                        }
                        swipeRefreshLayout.isRefreshing = false
                    }
                    Status.LOADING -> {
                        result.data?.let { notes ->
                            noteAdapter.notes = notes
                        }
                        swipeRefreshLayout.isRefreshing = true
                    }
                }
            }
        })

        swippingItem.observe(viewLifecycleOwner, {
            swipeRefreshLayout.isEnabled = !it
        })
    }

    private fun logout() {
        sharedPref.edit().putString(Constants.KEY_LOGGED_IN_EMAIL, Constants.NO_EMAIL).apply()
        sharedPref.edit().putString(Constants.KEY_PASSWORD, Constants.NO_PASSWORD).apply()

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.notesFragment, true)
            .build()

        findNavController().navigate(NotesFragmentDirections.actionNotesFragmentToAuthFragment(), navOptions)
    }

}
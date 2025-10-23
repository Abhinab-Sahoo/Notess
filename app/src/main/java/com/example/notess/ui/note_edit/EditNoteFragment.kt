package com.example.notess.ui.note_edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.notess.R
import com.example.notess.data.model.Note
import com.example.notess.databinding.FragmentEditNoteBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditNoteFragment : Fragment() {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!
    private var currentNote: Note? = null
    private val editNoteViewModel: EditNoteViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editNoteViewModel.note.observe(viewLifecycleOwner) { selectedNote ->
            if (selectedNote != null) {
                this.currentNote = selectedNote
                bindNote(selectedNote)

                editNoteViewModel.onNoteLoaded(selectedNote)
            }
        }
        setupToolbarAndMenu()
        collectUiEvents()
        collectMenuScreenState()

    }

    private fun bindNote(note: Note) {
        binding.apply {
            titleEditText.setText(note.noteHead)
            bodyEditText.setText(note.noteBody)
        }
    }

    private fun collectUiEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                editNoteViewModel.uiEvent.collect { event ->
                    when (event) {
                        is EditNoteUiEvent.ShowConfirmationAndNavigateUp -> {
                            Toast.makeText(
                                requireContext(),
                                event.message,
                                Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }

    private fun collectMenuScreenState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                editNoteViewModel.menuScreenState.collect {
                    requireActivity().invalidateOptionsMenu()
                }
            }
        }
    }

    private fun setupToolbarAndMenu() {
        val toolbar = binding.editNoteToolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requireActivity().addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(
                menu: Menu,
                menuInflater: MenuInflater
            ) {
                menuInflater.inflate(R.menu.edit_note_master_menu, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                val menuState = editNoteViewModel.menuScreenState.value

                menu.findItem(R.id.action_save)?.isVisible = menuState.showSaveButton
                menu.findItem(R.id.action_archive)?.isVisible = menuState.showArchiveMenuButton
                menu.findItem(R.id.action_unarchive)?.isVisible = menuState.showUnarchiveMenuButton
                menu.findItem(R.id.action_trash)?.isVisible = menuState.showTrashButton
                menu.findItem(R.id.action_restore)?.isVisible = menuState.showRestoreButton
                menu.findItem(R.id.action_delete_forever)?.isVisible = menuState.showDeleteForeverButton
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                currentNote?.let { note ->
                    return when (menuItem.itemId) {
                        R.id.action_save -> {
                            val newTitle = binding.titleEditText.text.toString()
                            val newBody = binding.bodyEditText.text.toString()
                            editNoteViewModel.onSaveClicked(note, newTitle, newBody)
                            true
                        }
                        R.id.action_archive -> {
                            editNoteViewModel.onArchiveClicked(note)
                            true
                        }
                        R.id.action_unarchive -> {
                            editNoteViewModel.onUnarchiveClicked(note)
                            true
                        }
                        R.id.action_trash -> {
                            editNoteViewModel.onTrashClicked(note)
                            true
                        }
                        R.id.action_restore -> {
                            editNoteViewModel.onRestoreClicked(note)
                            true
                        }
                        R.id.action_delete_forever -> {
                            editNoteViewModel.onDeleteForeverClicked(note)
                            true
                        }
                        android.R.id.home -> {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                            true
                        }

                        else -> false
                    }
                }
                return false
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
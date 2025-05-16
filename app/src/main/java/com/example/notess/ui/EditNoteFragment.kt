package com.example.notess.ui

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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.notess.NoteApplication
import com.example.notess.R
import com.example.notess.databinding.FragmentEditNoteBinding
import com.example.notess.model.Note
import com.example.notess.ui.viewmodel.NoteViewModel
import com.example.notess.ui.viewmodel.NoteViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class EditNoteFragment : Fragment() {

    private val viewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(
            (activity?.application as NoteApplication).database.noteDao()
        )
    }
    private val navigationArgs: EditNoteFragmentArgs by navArgs()
    private lateinit var note: Note
    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!
    private var isFromArchive = false
    private var editMenu: Menu? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteId = navigationArgs.id

        viewModel.retrieveNote(noteId).observe(viewLifecycleOwner) { selectedNote ->
            note = selectedNote
            bindNote(note)
            isFromArchive = note.isArchived
            updateArchiveMenuItem()
        }

        val toolbar = binding.editNoteToolbar
        (toolbar as? androidx.appcompat.widget.Toolbar)?.let {
            (requireActivity() as AppCompatActivity).setSupportActionBar(it)
            (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

            requireActivity().addMenuProvider(object : MenuProvider {

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.edit_note_menu, menu)
                    editMenu = menu
                    updateArchiveMenuItem()
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_archive -> {
                            archiveNote()
                            true
                        }
                        R.id.action_delete -> {
                            showDeleteConfirmationDialog()
                            true
                        }
                        R.id.action_save -> {
                            updateNote()
                            true
                        }
                        android.R.id.home -> {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                            true
                        }
                        else -> false
                    }
                }
            }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        }

    }

    private fun bindNote(note: Note) {
        binding.apply {
            titleEditText.setText(note.noteHead)
            bodyEditText.setText(note.noteBody)
        }
    }

    private fun updateNote() {
        if (isEntryValid()) {
            val updatedNote = note.copy(
                noteHead = binding.titleEditText.text.toString(),
                noteBody = binding.bodyEditText.text.toString()
            )
            viewModel.updateNote(updatedNote)
            Toast.makeText(requireContext(), "Note Updated", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun archiveNote() {
        val wasArchived = note.isArchived
        val updatedNote = note.copy(isArchived = !wasArchived)
        note = updatedNote
        viewModel.updateNote(updatedNote)

        if (wasArchived) {
            Toast.makeText(requireContext(), "Note Unarchived!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_editNoteFragment_to_archiveFragment)
        } else {
            Toast.makeText(requireContext(), "Note Archived!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_editNoteFragment_to_noteFragment)
        }
    }

    private fun updateArchiveMenuItem() {
        val archiveItem = editMenu?.findItem(R.id.action_archive)
        if (isFromArchive) {
            archiveItem?.setIcon(R.drawable.unarchive)
            archiveItem?.title = "Unarchive"
        } else {
            archiveItem?.setIcon(R.drawable.ic_archive)
            archiveItem?.title = "Archive"
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { _, _ ->
                deleteNote(note)
            }
            .show()
    }

    private fun deleteNote(note: Note) {
        viewModel.deleteNote(note)
        Toast.makeText(requireContext(), "Note Deleted", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    private fun isEntryValid(): Boolean {
        val title = binding.titleEditText.text.toString().trim()
        val body = binding.bodyEditText.text.toString().trim()

        return if (title.isBlank() && body.isBlank()) {
            Toast.makeText(requireContext(), "Note can't be empty", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
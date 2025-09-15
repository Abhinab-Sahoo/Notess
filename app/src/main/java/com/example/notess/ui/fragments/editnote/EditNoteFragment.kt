package com.example.notess.ui.fragments.editnote

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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.notess.R
import com.example.notess.data.model.Note
import com.example.notess.databinding.FragmentEditNoteBinding
import com.example.notess.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditNoteFragment : Fragment() {

    private val viewModel: NoteViewModel by viewModels()
    private val navigationArgs: EditNoteFragmentArgs by navArgs()
    private var noteId: Int = 0
    private lateinit var source: String
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

        noteId = navigationArgs.id
        source = navigationArgs.source

        viewModel.retrieveNote(noteId).observe(viewLifecycleOwner) { selectedNote ->
            note = selectedNote
            bindNote(note)
            isFromArchive = note.isArchived
        }

        val toolbar = binding.editNoteToolbar
        (toolbar as? androidx.appcompat.widget.Toolbar)?.let {
            (requireActivity() as AppCompatActivity).setSupportActionBar(it)
            (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

            requireActivity().addMenuProvider(object : MenuProvider {

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    when (source) {
                        "note" -> menuInflater.inflate(R.menu.edit_note_menu, menu)
                        "archive" -> menuInflater.inflate(R.menu.archive_note_menu, menu)
                        "trash" -> menuInflater.inflate(R.menu.trash_note_menu, menu)
                    }
                    editMenu = menu

                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_archive -> {
                            archiveNote()
                            true
                        }
                        R.id.action_unarchive -> {
                            unarchiveNote()
                            true
                        }
                        R.id.action_delete -> {
                            moveToTrash()
                            true
                        }
                        R.id.action_save -> {
                            updateNote()
                            true
                        }
                        R.id.action_delete_forever -> {
                            deleteForever()
                            true
                        }
                        R.id.action_restore -> {
                            restoreNote()
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
        viewModel.archiveNote(note)
        Toast.makeText(requireContext(), "Note Archived!", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_editNoteFragment_to_noteFragment)
    }

    private fun unarchiveNote() {
        viewModel.unArchiveNote(note)
        Toast.makeText(requireContext(), "Note UnArchived!", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_editNoteFragment_to_archiveFragment)
    }

    private fun moveToTrash() {
        viewModel.moveToTrash(note, source)
        Toast.makeText(requireContext(), "Note moved to bin", Toast.LENGTH_SHORT).show()

        if (source == "note") {
            findNavController().navigate(R.id.action_editNoteFragment_to_noteFragment)
        } else if (source == "archive") {
            findNavController().navigate(R.id.action_editNoteFragment_to_archiveFragment)
        }
    }

    private fun deleteForever() {
        viewModel.deleteNoteForever(note)
        Toast.makeText(requireContext(), "Note deleted forever", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_editNoteFragment_to_trashFragment)
    }

    private fun restoreNote() {
        viewModel.restoreNote(note)
        Toast.makeText(requireContext(), "Note restored", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_editNoteFragment_to_trashFragment)
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
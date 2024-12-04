package com.example.notess.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.notess.NoteApplication
import com.example.notess.R
import com.example.notess.databinding.FragmentAddNoteBinding
import com.example.notess.model.Note
import com.example.notess.ui.viewmodel.NoteViewModel
import com.example.notess.ui.viewmodel.NoteViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class AddNoteFragment : Fragment() {

    private val viewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(
            (activity?.application as NoteApplication).database.noteDao()
        )
    }
    private val navigationArgs: AddNoteFragmentArgs by navArgs()
    private lateinit var note: Note
    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = navigationArgs.id
        if (id > 0) {

            viewModel.retrieveNote(id).observe(viewLifecycleOwner) { observeNote ->
                observeNote?.let {
                    note = it
                    bindNote(note)
                }
            }

            binding.deleteBtn.visibility = View.VISIBLE
            binding.deleteBtn.setOnClickListener {
                showDeleteConfirmationDialog()
            }

        } else {
            binding.saveBtn.setOnClickListener {
                addNote()
            }
        }

        binding.notesInput.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.notesInput, InputMethodManager.SHOW_IMPLICIT)

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
        findNavController().navigate(
            R.id.action_addNoteFragment_to_noteFragment
        )
    }

    private fun addNote() {
        if (isEntryValid()) {
            viewModel.addNote(
                binding.nameInput.text.toString(),
                binding.notesInput.text.toString()
            )
            findNavController().navigate(
                R.id.action_addNoteFragment_to_noteFragment
            )
        }
    }

    private fun updateNote() {
        if (isEntryValid()) {
            viewModel.updateNote(
                navigationArgs.id,
                binding.nameInput.text.toString(),
                binding.notesInput.text.toString()
            )
            findNavController().navigate(
                R.id.action_addNoteFragment_to_noteFragment
            )
        }
    }

    private fun bindNote(note: Note) {
        binding.apply {
            nameInput.setText(note.noteHead, TextView.BufferType.SPANNABLE)
            notesInput.setText(note.noteBody, TextView.BufferType.SPANNABLE)

            saveBtn.setOnClickListener { updateNote() }
        }
    }

    private fun isEntryValid() = viewModel.isEntryValid(
        binding.nameInput.text.toString(),
        binding.notesInput.text.toString()
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
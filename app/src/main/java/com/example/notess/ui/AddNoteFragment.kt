package com.example.notess.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.notess.NoteApplication
import com.example.notess.R
import com.example.notess.databinding.FragmentAddNoteBinding
import com.example.notess.ui.viewmodel.NoteViewModel
import com.example.notess.ui.viewmodel.NoteViewModelFactory


class AddNoteFragment : Fragment() {

    private val viewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(
            (activity?.application as NoteApplication).database.noteDao()
        )
    }
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

        binding.saveBtn.setOnClickListener {
            addNote()
        }

        binding.noteBody.requestFocus()
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.noteBody, InputMethodManager.SHOW_IMPLICIT)

    }

    private fun addNote() {
        val noteHead = binding.noteTitle.text.toString()
        val noteBody = binding.noteBody.text.toString()

        if (noteHead.isNotBlank() || noteBody.isNotBlank()) {
            viewModel.addNote(noteHead, noteBody)
            findNavController().navigate(
                R.id.action_addNoteFragment_to_noteFragment
            )
        } else {
            Toast.makeText(requireContext(), "Note cannot be empty!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
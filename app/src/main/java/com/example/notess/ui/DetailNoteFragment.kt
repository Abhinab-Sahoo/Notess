package com.example.notess.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.notess.NoteApplication
import com.example.notess.databinding.FragmentDetailNoteBinding
import com.example.notess.model.Note
import com.example.notess.ui.viewmodel.NoteViewModel
import com.example.notess.ui.viewmodel.NoteViewModelFactory


class DetailNoteFragment : Fragment() {

    private val viewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(
            (activity?.application as NoteApplication).database.noteDao()
        )
    }
    private val navigationArgs: DetailNoteFragmentArgs by navArgs()
    private lateinit var note: Note
    private var _binding: FragmentDetailNoteBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = navigationArgs.id
        viewModel.retrieveNote(id).observe(viewLifecycleOwner) { obserbedNote ->
            obserbedNote?.let {
                note = it
                bindNote()
            }
        }
    }

    private fun bindNote() {
        binding.apply {
            name.text = note.noteHead
            notes.text = note.noteBody

            editForageableFab.setOnClickListener {
                val action = DetailNoteFragmentDirections
                    .actionDetailNoteFragmentToAddNoteFragment(note.id)
                findNavController().navigate(action)
            }
        }
    }

}
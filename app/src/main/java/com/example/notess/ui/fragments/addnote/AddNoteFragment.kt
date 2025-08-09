package com.example.notess.ui.fragments.addnote

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.notess.R
import com.example.notess.data.model.Note
import com.example.notess.databinding.FragmentAddNoteBinding
import com.example.notess.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddNoteFragment : Fragment() {

    private val viewModel: NoteViewModel by viewModels()
    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!
    private var editMenu: Menu? = null


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


        val toolbar = binding.addNoteToolbar
        (toolbar as? androidx.appcompat.widget.Toolbar)?.let {
            (requireActivity() as AppCompatActivity).setSupportActionBar(it)
            (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

            requireActivity().addMenuProvider(object : MenuProvider {

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.add_note_menu, menu)
                    editMenu = menu
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.menu_archive -> {
                            saveAndArchiveNote()
                            true
                        }
                        R.id.menu_save -> {
                            addNote()
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

    fun saveAndArchiveNote() {
        val title = binding.noteTitle.text.toString()
        val body = binding.noteBody.text.toString()

        if (title.isBlank() && body.isBlank()) {
            Toast.makeText(requireContext(), "Note cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val note = Note(
            noteHead = title,
            noteBody = body,
            isArchived = true
        )

        viewModel.insertNote(note)
        Toast.makeText(requireContext(), "Note Archived!", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_addNoteFragment_to_noteFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
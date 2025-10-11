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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.notess.R
import com.example.notess.databinding.FragmentAddNoteBinding
import com.example.notess.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


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

        observeUiEvents()
    }

    private fun observeUiEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    when (event) {
                        is NoteViewModel.UiEvent.NavigateBack -> {
                            findNavController().popBackStack()
                        }
                        is NoteViewModel.UiEvent.ShowEmptyNoteError -> {
                            Toast.makeText(
                                requireContext(),
                                "Note cannot be empty!",
                                Toast.LENGTH_SHORT).show()
                        }
                        is NoteViewModel.UiEvent.ShowArchiveConfirmationAndNavigateBack -> {
                            Toast.makeText(
                                requireContext(),
                                "Note archived!",
                                Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    private fun addNote() {
        val noteHead = binding.noteTitle.text.toString()
        val noteBody = binding.noteBody.text.toString()
        viewModel.addNote(noteHead, noteBody)
    }

    fun saveAndArchiveNote() {
        val title = binding.noteTitle.text.toString()
        val body = binding.noteBody.text.toString()
        viewModel.onSaveAndArchiveClicked(title, body)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
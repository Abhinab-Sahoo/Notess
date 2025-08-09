package com.example.notess.ui.fragments.trashnote

import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notess.MainActivity
import com.example.notess.R
import com.example.notess.databinding.FragmentTrashBinding
import com.example.notess.ui.adapter.NoteAdapter
import com.example.notess.viewmodel.NoteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrashFragment : Fragment() {

    private val noteViewModel: NoteViewModel by viewModels()
    private var _binding: FragmentTrashBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTrashBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = binding.trashToolbar
        (toolbar as? androidx.appcompat.widget.Toolbar)?.let {
            (requireActivity() as AppCompatActivity).setSupportActionBar(it)
            (requireActivity() as AppCompatActivity).supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.ic_hamburger)
            }
            it.setNavigationOnClickListener {
                (requireActivity() as MainActivity).openDrawer()
            }
        }

        requireActivity().addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                Log.d("TrashMenu", "onCreateMenu called")
                menu.clear()
                menuInflater.inflate(R.menu.trash_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                Log.d("TrashMenu", "onMenuItemSelected called")

                return when (menuItem.itemId) {
                    R.id.action_empty_trash -> {
                        showDeleteConfirmationDialog()
                        true
                    }

                    else -> false
                }
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        adapter = NoteAdapter(
            clickListener = { note ->
                val action =
                    TrashFragmentDirections.actionTrashFragmentToEditNoteFragment(note.id, "trash")
                findNavController().navigate(action)
            }
        )

        binding.trashRecyclerView.adapter = adapter
        binding.trashRecyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        noteViewModel.trashedNotes.observe(viewLifecycleOwner) { notes ->
            Log.d("Trash Fragment", "Trashed notes: ${notes.size}")
            adapter.submitList(null)
            adapter.submitList(notes)
        }

    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Empty Recycle Bin?")
            .setMessage("All notes in Recycle Bin will permanently deleted.")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { _, _ ->
                noteViewModel.deleteAllTrashedNotes()
                Toast.makeText(requireContext(), "Trash Emptied!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
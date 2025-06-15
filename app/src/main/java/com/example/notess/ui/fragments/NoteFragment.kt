package com.example.notess.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notess.MainActivity
import com.example.notess.R
import com.example.notess.databinding.FragmentNoteBinding
import com.example.notess.ui.adapter.NoteAdapter
import com.example.notess.viewmodel.NoteViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class NoteFragment : Fragment() {

    private val noteViewModel: NoteViewModel by viewModels()
    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!
    private var isGridLayout: Boolean = true
    private var isFabMenuOpen: Boolean = false
    private lateinit var adapter: NoteAdapter

    private fun updateLayout() {
        binding.recyclerView.layoutManager = if (isGridLayout) {
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            LinearLayoutManager(requireContext())
        }

        binding.layoutToggleButton.setImageResource(
            if (isGridLayout) R.drawable.linear_layout else R.drawable.ic_grid_view
        )

        binding.recyclerView.adapter?.notifyDataSetChanged()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNoteBinding.inflate(layoutInflater, container, false)

        binding.menuButton.setOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFabMenu()
        setUpSearch()
        observeSearchResults()

        adapter = NoteAdapter(
            clickListener = { note ->
                val action =
                    NoteFragmentDirections.actionNoteFragmentToEditNoteFragment(note.id, "note")
                findNavController().navigate(action)
            }
        )

        binding.apply {
            updateLayout()
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapter

            layoutToggleButton.setOnClickListener {
                isGridLayout = !isGridLayout
                updateLayout()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        noteViewModel.activeNotes.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
        }

    }


    private fun setUpSearch() {

        val scope = viewLifecycleOwner.lifecycleScope
        var searchJob: Job? = null

        binding.searchEditText.doAfterTextChanged { text ->
            searchJob?.cancel()
            searchJob = scope.launch {
                delay(300)
                if (text.isNullOrEmpty()) {
                    // Reset to full list when search is cleared
                    noteViewModel.activeNotes.observe(viewLifecycleOwner) { notes ->
                        adapter.submitList(notes)
                    }
                } else {
                    noteViewModel.updateSearchQuery(text.toString())
                }
            }
        }


        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    private fun observeSearchResults() {
        noteViewModel.searchResults.observe(viewLifecycleOwner) { notes ->
            if (binding.searchEditText.text.isNullOrEmpty()) {
                // Reset to full list when search is cleared
                noteViewModel.activeNotes.observe(viewLifecycleOwner) { allNotes ->
                    adapter.submitList(allNotes)
                }
            } else {
                adapter.submitList(notes)
            }
        }
    }

    private fun setupFabMenu() {
        binding.floatingActionButton.setOnClickListener {
            toggleFabMenu()
        }

        binding.fabImage.setOnClickListener {
            showImageDialog()
        }

        binding.fabList.setOnClickListener {
            Toast.makeText(requireContext(), "In Progress, Coming Soon!!", Toast.LENGTH_SHORT)
                .show()
        }

        binding.fabText.setOnClickListener {
            findNavController().navigate(R.id.action_noteFragment_to_addNoteFragment)
        }
    }

    private fun showImageDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_image_options, null)

        val takePhotoOption = view.findViewById<LinearLayout>(R.id.take_photo_option)
        val chooseImageOption = view.findViewById<LinearLayout>(R.id.choose_image_option)

        takePhotoOption.setOnClickListener {
            Toast.makeText(requireContext(), "In Progress, Coming Soon!!", Toast.LENGTH_SHORT)
                .show()
        }

        chooseImageOption.setOnClickListener {
            Toast.makeText(requireContext(), "In Progress, Coming Soon!!", Toast.LENGTH_SHORT)
                .show()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun toggleFabMenu() {
        isFabMenuOpen = !isFabMenuOpen
        binding.fabMenuContainer.animate()
            .alpha(if (isFabMenuOpen) 1f else 0f)
            .setDuration(300)
            .withEndAction {
                binding.fabMenuContainer.visibility =
                    if (isFabMenuOpen) View.VISIBLE else View.GONE
            }
    }

    private val swipeHandler = object :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val note = (binding.recyclerView.adapter as NoteAdapter).currentList[position]

            when (direction) {

                ItemTouchHelper.RIGHT -> {
                    noteViewModel.archiveNote(note)
                    Toast.makeText(requireContext(), "Note Archived!", Toast.LENGTH_SHORT).show()
                }

                ItemTouchHelper.LEFT -> {
                    noteViewModel.moveToTrash(note, "note")
                    Toast.makeText(requireContext(), "Moved to bin", Toast.LENGTH_SHORT).show()
                }
            }
        }

    } // End of NoteFragment

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
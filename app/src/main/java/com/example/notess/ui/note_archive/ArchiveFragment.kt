package com.example.notess.ui.note_archive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notess.MainActivity
import com.example.notess.R
import com.example.notess.databinding.FragmentArchiveBinding
import com.example.notess.ui.adapter.NoteAdapter
import com.example.notess.ui.note_list.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArchiveFragment : Fragment() {

    private val noteViewModel: NoteViewModel by viewModels()
    private var _binding: FragmentArchiveBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NoteAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentArchiveBinding.inflate(layoutInflater, container, false)

        binding.menuButton.setOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NoteAdapter(
            clickListener = { note ->
                val action = ArchiveFragmentDirections.actionArchiveFragmentToEditNoteFragment(
                    note.id
                )
                findNavController().navigate(action)
            }
        )

        binding.apply {
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapter

            layoutToggleButton.setOnClickListener {
                noteViewModel.onLayoutToggleClicked()
            }
        }

        noteViewModel.archivedNotes.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)

            binding.emptyStateView.visibility =
                if (notes.isEmpty()) View.VISIBLE else View.GONE
        }

        collectScreenState()
    }

    private fun collectScreenState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                noteViewModel.screenState.collect { screenState ->
                    binding.recyclerView.layoutManager = if (screenState.isGridLayout) {
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    } else {
                        LinearLayoutManager(requireContext())
                    }
                    binding.layoutToggleButton.setImageResource(
                        if (screenState.isGridLayout) R.drawable.linear_layout else R.drawable.ic_grid_view
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
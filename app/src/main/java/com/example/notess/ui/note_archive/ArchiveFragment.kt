package com.example.notess.ui.note_archive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notess.MainActivity
import com.example.notess.R
import com.example.notess.databinding.FragmentArchiveBinding
import com.example.notess.ui.adapter.NoteAdapter
import com.example.notess.ui.note_list.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArchiveFragment : Fragment() {

    private val noteViewModel: NoteViewModel by viewModels()
    private var _binding: FragmentArchiveBinding? = null
    private val binding get() = _binding!!
    private var isGridLayout: Boolean = true
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
            updateLayout()
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapter

            layoutToggleButton.setOnClickListener {
                isGridLayout = !isGridLayout
                updateLayout()
            }
        }

        noteViewModel.archivedNotes.observe(viewLifecycleOwner) { notes ->

            adapter.submitList(notes)

            binding.emptyStateView.visibility =
                if (notes.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
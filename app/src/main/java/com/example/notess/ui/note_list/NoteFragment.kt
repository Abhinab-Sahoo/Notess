package com.example.notess.ui.note_list

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.notess.MainActivity
import com.example.notess.R
import com.example.notess.databinding.FragmentNoteBinding
import com.example.notess.ui.adapter.NoteAdapter
import com.example.notess.viewmodel.AuthViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteFragment : Fragment() {

    private val noteViewModel: NoteViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NoteAdapter


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

        adapter = NoteAdapter(
            clickListener = { note ->
                val action =
                    NoteFragmentDirections.actionNoteFragmentToEditNoteFragment(note.id)
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

        noteViewModel.notes.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        setupFabMenu()
        setUpSearch()
        goToProfile()
        replaceWithGooglePhoto()
        collectUiEvents()
        collectScreenState()

    }

    private fun collectUiEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            noteViewModel.uiEvent.collect { event ->
                when (event) {
                    is NoteViewModel.UiEvent.ShowToast -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {  }
                }
            }
        }
    }

    private fun collectScreenState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                noteViewModel.screenState.collect { screenState ->
                    val targetAlpha = if (screenState.isFabMenuOpen) 1f else 0f
                    binding.fabMenuContainer.animate()
                        .alpha(targetAlpha)
                        .setDuration(300)
                        .withEndAction {
                            _binding?.fabMenuContainer?.visibility =
                                if (screenState.isFabMenuOpen) View.VISIBLE else View.GONE
                        }
                    val iconRes = if (screenState.isFabMenuOpen) {
                        R.drawable.close
                    } else {
                        R.drawable.ic_input_add
                    }
                    binding.floatingActionButton.setImageResource(iconRes)

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

    private fun replaceWithGooglePhoto() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.userProfilePhotoUrl.collect { photoUrl ->
                if (photoUrl != null) {
                    Glide.with(requireContext())
                        .load(photoUrl)
                        .placeholder(R.drawable.account)
                        .error(R.drawable.account)
                        .circleCrop()
                }
            }
        }
    }

    private fun goToProfile() {
        binding.accountButton.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
    }
    private fun setUpSearch() {

        binding.searchEditText.doAfterTextChanged { text ->
            noteViewModel.onSearchQueryChanged(text.toString())
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

    private fun setupFabMenu() {
        binding.floatingActionButton.setOnClickListener {
            noteViewModel.onFabClicked()
        }

        binding.fabImage.setOnClickListener {
            noteViewModel.closeFabMenu()
            showImageDialog()
        }

        binding.fabList.setOnClickListener {
            noteViewModel.closeFabMenu()
            Toast.makeText(requireContext(), "In Progress, Coming Soon!!", Toast.LENGTH_SHORT)
                .show()
        }

        binding.fabText.setOnClickListener {
            noteViewModel.closeFabMenu()
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

    private val swipeHandler = object :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(
            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean { return false }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val note = (binding.recyclerView.adapter as NoteAdapter).currentList[position]

            when (direction) {
                ItemTouchHelper.RIGHT -> noteViewModel.onNoteSwipedRight(note)
                ItemTouchHelper.LEFT -> noteViewModel.onNoteSwipedLeft(note)
            }
        }

    } // End of NoteFragment

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
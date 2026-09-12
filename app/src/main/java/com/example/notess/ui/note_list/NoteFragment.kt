package com.example.notess.ui.note_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.notess.MainActivity
import com.example.notess.ui.theme.NotessTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                NotessTheme {
                    NoteScreen(
                        onNavigateToEdit = { noteId ->
                            val action = NoteFragmentDirections.actionNoteFragmentToEditNoteFragment(noteId)
                            findNavController().navigate(action)
                        },
                        onNavigateToAdd = {
                            val action = NoteFragmentDirections.actionNoteFragmentToAddNoteFragment()
                            findNavController().navigate(action)
                        },
                        onNavigateToProfile = {
                            val action = NoteFragmentDirections.actionNoteFragmentToProfileFragment()
                            findNavController().navigate(action)
                        },
                        onOpenDrawer = {
                            (activity as? MainActivity)?.openDrawer()
                        }
                    )
                }
            }
        }
    }
}

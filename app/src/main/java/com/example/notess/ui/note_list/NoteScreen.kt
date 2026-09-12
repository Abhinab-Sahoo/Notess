package com.example.notess.ui.note_list

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.notess.ui.theme.NotessTheme
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.notess.R
import com.example.notess.data.model.Note
import com.example.notess.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteScreen(
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onOpenDrawer: () -> Unit,
    noteViewModel: NoteViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val notes by noteViewModel.notes.observeAsState(initial = emptyList())
    val screenState by noteViewModel.screenState.collectAsState()
    val photoUrl by authViewModel.userProfilePhotoUrl.collectAsState()

    LaunchedEffect(Unit) {
        noteViewModel.uiEvent.collect { event ->
            when (event) {
                is NoteViewModel.UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    NoteScreenContent(
        notes = notes,
        screenState = screenState,
        photoUrl = photoUrl,
        onNavigateToEdit = onNavigateToEdit,
        onNavigateToAdd = onNavigateToAdd,
        onNavigateToProfile = onNavigateToProfile,
        onOpenDrawer = onOpenDrawer,
        onToggleLayout = { noteViewModel.onLayoutToggleClicked() },
        onSearchChanged = { noteViewModel.onSearchQueryChanged(it) },
        onFabClicked = { noteViewModel.onFabClicked() },
        onCloseFabMenu = { noteViewModel.closeFabMenu() },
        onNoteSwipedLeft = { noteViewModel.onNoteSwipedLeft(it) },
        onNoteSwipedRight = { noteViewModel.onNoteSwipedRight(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteScreenContent(
    notes: List<Note>,
    screenState: NoteListScreenState,
    photoUrl: String?,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onOpenDrawer: () -> Unit,
    onToggleLayout: () -> Unit,
    onSearchChanged: (String) -> Unit,
    onFabClicked: () -> Unit,
    onCloseFabMenu: () -> Unit,
    onNoteSwipedLeft: (Note) -> Unit,
    onNoteSwipedRight: (Note) -> Unit
) {
    val context = LocalContext.current
    var showImageSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SearchBarUI(
                onOpenDrawer = onOpenDrawer,
                photoUrl = photoUrl,
                onNavigateToProfile = onNavigateToProfile,
                isGrid = screenState.isGridLayout,
                onToggleLayout = onToggleLayout,
                onSearchChanged = onSearchChanged
            )
        },
        floatingActionButton = {
            FabMenuUI(
                isOpen = screenState.isFabMenuOpen,
                onToggle = onFabClicked,
                onTextClick = {
                    onCloseFabMenu()
                    onNavigateToAdd()
                },
                onListClick = {
                    onCloseFabMenu()
                    Toast.makeText(context, "In Progress, Coming Soon!!", Toast.LENGTH_SHORT).show()
                },
                onImageClick = {
                    onCloseFabMenu()
                    showImageSheet = true
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
        ) {
            if (screenState.isGridLayout) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        SwipeableNoteItem(
                            note = note,
                            onClick = { onNavigateToEdit(note.id) },
                            onSwipeLeft = { onNoteSwipedLeft(it) },
                            onSwipeRight = { onNoteSwipedRight(it) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        SwipeableNoteItem(
                            note = note,
                            onClick = { onNavigateToEdit(note.id) },
                            onSwipeLeft = { onNoteSwipedLeft(it) },
                            onSwipeRight = { onNoteSwipedRight(it) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    if (showImageSheet) {
        ModalBottomSheet(onDismissRequest = { showImageSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 8.dp)
            ) {
                ListItem(
                    headlineContent = { Text("Take photo") },
                    leadingContent = { Icon(painterResource(R.drawable.ic_image_new), contentDescription = null) },
                    modifier = Modifier.clickable {
                        showImageSheet = false
                        Toast.makeText(context, "In Progress, Coming Soon!!", Toast.LENGTH_SHORT).show()
                    }
                )
                ListItem(
                    headlineContent = { Text("Choose image") },
                    leadingContent = { Icon(painterResource(R.drawable.ic_image_new), contentDescription = null) },
                    modifier = Modifier.clickable {
                        showImageSheet = false
                        Toast.makeText(context, "In Progress, Coming Soon!!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableNoteItem(
    note: Note,
    onClick: () -> Unit,
    onSwipeLeft: (Note) -> Unit,
    onSwipeRight: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine the swipe threshold and state
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onSwipeRight(note)
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onSwipeLeft(note)
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.padding(4.dp),
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50) // Archive (Green)
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFF44336) // Trash (Red)
                SwipeToDismissBoxValue.Settled -> Color.Transparent
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.Settled -> Alignment.Center
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> R.drawable.ic_list 
                SwipeToDismissBoxValue.EndToStart -> R.drawable.delete_forever_24
                SwipeToDismissBoxValue.Settled -> R.drawable.ic_text 
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = alignment
            ) {
                if (direction != SwipeToDismissBoxValue.Settled) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        },
        content = {
            // Note Card
            Card(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (note.noteHead.isNotEmpty()) {
                        Text(
                            text = note.noteHead,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = note.noteBody ?: "",
                        fontSize = 14.sp,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarUI(
    onOpenDrawer: () -> Unit,
    photoUrl: String?,
    onNavigateToProfile: () -> Unit,
    isGrid: Boolean,
    onToggleLayout: () -> Unit,
    onSearchChanged: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }

            TextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    onSearchChanged(it)
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search your notes") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() }
                )
            )

            IconButton(onClick = onToggleLayout) {
                Icon(
                    painter = painterResource(if (isGrid) R.drawable.linear_layout else R.drawable.ic_grid_view),
                    contentDescription = "Toggle Layout"
                )
            }

            IconButton(onClick = onNavigateToProfile) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.account),
                        placeholder = painterResource(R.drawable.account)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.account),
                        contentDescription = "Profile",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FabMenuUI(
    isOpen: Boolean,
    onToggle: () -> Unit,
    onTextClick: () -> Unit,
    onListClick: () -> Unit,
    onImageClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = isOpen,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 50 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { 50 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                FabMenuItem(text = "Image", iconRes = R.drawable.ic_image_new, onClick = onImageClick)
                Spacer(modifier = Modifier.height(16.dp))
                FabMenuItem(text = "List", iconRes = R.drawable.ic_list, onClick = onListClick)
                Spacer(modifier = Modifier.height(16.dp))
                FabMenuItem(text = "Text", iconRes = R.drawable.ic_text, onClick = onTextClick)
            }
        }

        val rotation by animateFloatAsState(if (isOpen) 45f else 0f, label = "fabRotation")
        
        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Note",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
fun FabMenuItem(
    text: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Medium, modifier = Modifier.padding(end = 12.dp))
        Icon(painter = painterResource(id = iconRes), contentDescription = text)
    }
}

@Preview(showBackground = true)
@Composable
fun NoteScreenPreview() {
    val sampleNotes = listOf(
        Note(
            id = 1,
            noteHead = "Sample Note 1",
            noteBody = "This is a sample note body. It can be quite long."
        ),
        Note(
            id = 2,
            noteHead = "Sample Note 2",
            noteBody = "Another sample note."
        )
    )
    NotessTheme {
        NoteScreenContent(
            notes = sampleNotes,
            screenState = NoteListScreenState(isGridLayout = true, isFabMenuOpen = false),
            photoUrl = null,
            onNavigateToEdit = {},
            onNavigateToAdd = {},
            onNavigateToProfile = {},
            onOpenDrawer = {},
            onToggleLayout = {},
            onSearchChanged = {},
            onFabClicked = {},
            onCloseFabMenu = {},
            onNoteSwipedLeft = {},
            onNoteSwipedRight = {}
        )
    }
}

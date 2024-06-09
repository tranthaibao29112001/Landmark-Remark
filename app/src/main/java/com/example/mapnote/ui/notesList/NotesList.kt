package com.example.mapnote.ui.notesList

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mapnote.util.Constants
import com.example.mapnote.util.Constants.orPlaceHolderList
import com.example.mapnote.R
import com.example.mapnote.data.model.Note
import com.example.mapnote.data.model.getDay
import com.example.mapnote.ui.viewmodel.AuthViewModel
import com.example.mapnote.ui.viewmodel.MapNoteViewModel
import com.example.mapnote.ui.theme.MapNoteTheme
import com.example.mapnote.util.Util
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun NotesList(navController: NavController, mapNoteViewModel: MapNoteViewModel, authViewModel: AuthViewModel) {
    val scope = rememberCoroutineScope()
    val notesQuery = remember { mutableStateOf("") }
    LaunchedEffect(true) {
        scope.launch(Dispatchers.IO) {
            mapNoteViewModel.getNotes()
        }
    }
    val currentUser = authViewModel.currentUser
    val notes by mapNoteViewModel.notes.collectAsState(emptyList())
    var filterMyNote by remember { mutableStateOf(false) }



    MapNoteTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.primary) {
            Scaffold(
                topBar = {

                    TopAppBar(
                        title = { Text(stringResource(R.string.map_notes) )},
                        backgroundColor = MaterialTheme.colors.primary,
                        actions = {
                            IconButton(
                                // Turn on and off the my filter note
                                onClick = {
                                    filterMyNote = !filterMyNote
                                },
                                content = {
                                    val resId = if(filterMyNote) R.drawable.fill_star else R.drawable.unfill_star
                                    Row(modifier = Modifier.size(26.dp)){
                                        Icon(
                                            painter = painterResource(id = resId),
                                            contentDescription = "Filter your note",
                                            tint = Color.Yellow,
                                            modifier = Modifier
                                                .padding(vertical = 4.dp)
                                                .size(24.dp)
                                        )
                                    }
                                }
                            )
                            IconButton(
                                onClick = {
                                    authViewModel.logout()
                                    navController.navigate(Constants.ROUTE_LOGIN)
                                },
                                content = {
                                    Box(modifier = Modifier.size(26.dp)){
                                        Icon(
                                            painter = painterResource(id = R.drawable.logout),
                                            contentDescription = "Logout",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .padding(vertical = 4.dp)
                                                .size(24.dp)
                                        )
                                    }
                                }
                            )
                        }
                    )
                }

            ) {
                Column() {
                    SearchBar(notesQuery)
                    if(notes.isEmpty()){
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No notes available",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                    else{
                        NotesList(
                            notes = notes.orPlaceHolderList(),
                            query = notesQuery,
                            navController = navController,
                            currentUser,
                            filterMyNote
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun SearchBar(query: MutableState<String>) {
    Column(Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 0.dp)) {
        TextField(
            value = query.value,
            placeholder = { Text("Search..") },
            maxLines = 1,
            onValueChange = { query.value = it },
            modifier = Modifier
                .background(Color.White)
                .clip(RoundedCornerShape(12.dp))
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                textColor = Color.Black,
            ),
            trailingIcon = {
                AnimatedVisibility(
                    visible = query.value.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = { query.value = "" }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.icon_cross),
                            contentDescription = stringResource(
                                R.string.clear_search
                            )
                        )
                    }
                }

            })

    }
}

@Composable
fun NotesList(
    notes: List<Note>,
    query: MutableState<String>,
    navController: NavController,
    currentUser: FirebaseUser?,
    filterMyNote: Boolean
) {
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        modifier = Modifier.padding(6.dp)
    ) {
        val queriedNotes = if (query.value.isEmpty()){
            notes.filter { (!filterMyNote || (filterMyNote && currentUser?.uid == it.userId)) }.sortedByDescending  { it.dateUpdated }
        } else {
            notes.filter { (!filterMyNote || (filterMyNote && currentUser?.uid == it.userId)) && (it.userName.contains(query.value) || it.note.contains(query.value) || it.title.contains(query.value)) }.sortedByDescending{it.dateUpdated}
        }
        itemsIndexed(queriedNotes) { index, note ->
            NoteListItem(
                note,
                navController,
                isCurrentUserNote = currentUser?.uid == note.userId
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteListItem(
    note: Note,
    navController: NavController,
    isCurrentUserNote : Boolean
) {

    return Box(
        modifier = Modifier
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))

    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.onSecondary)
                .fillMaxWidth()
                .height(120.dp)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true), // Adjust ripple to be bounded
                    onClick = {
                        navController.navigate(
                            Constants.NAVIGATION_NOTES_CREATE + "/" + note.lat + "/" + note.lng + "/" + note.id
                        )
                    }
                )
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                val bitmap = Util.base64ToBitmap(note.imageBase64 ?: "")
                bitmap?.asImageBitmap()?.let { BitmapPainter(it) }?.let {
                    Image(
                        painter = it,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(112.dp) // Fix the width to make it more visually balanced
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(
                                modifier = Modifier
                                    .width(100.dp)
                                    .padding(vertical = 1.dp),
                                text = note.userName,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis

                            )
                            Text(
                                text = note.title,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                            Text(
                                text = note.note,
                                color = Color.Black,
                                maxLines = 3,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Lat: " + note.lat.toString(),
                                color = Color.Black,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                maxLines = 1,
                                modifier = Modifier
                                    .width(120.dp)
                                    .padding(vertical = 1.dp),
                                overflow = TextOverflow.Ellipsis

                            )
                            Text(
                                text = "Lng: " + note.lng.toString(),
                                color = Color.Black,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                maxLines = 1,
                                modifier = Modifier
                                    .width(120.dp)
                                    .padding(vertical = 1.dp),
                                overflow = TextOverflow.Ellipsis

                            )
                        }
                    }

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.End), horizontalArrangement = Arrangement.SpaceBetween){
                        if(isCurrentUserNote){
                            Icon(
                                painter = painterResource(id = R.drawable.fill_star),
                                contentDescription = if (isCurrentUserNote) "Current User's Note" else "Not Current User's Note",
                                tint = Color.Yellow, // Optional, if you want to tint the icon
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .size(24.dp)
                            )
                        }
                        else{
                            Spacer(modifier = Modifier)
                        }

                        Text(
                            text = note.getDay(),
                            color = Color.Black,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotesFab(contentDescription: String, icon: Int, action: () -> Unit) {
    return FloatingActionButton(
        onClick = { action.invoke() },
        backgroundColor = MaterialTheme.colors.primary,
    ) {
        Icon(
            ImageVector.vectorResource(id = icon),
            contentDescription = contentDescription,
            tint = Color.Black
        )

    }
}

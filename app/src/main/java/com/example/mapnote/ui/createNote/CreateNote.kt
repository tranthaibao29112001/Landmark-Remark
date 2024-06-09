package com.example.mapnote.ui.createNote

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberImagePainter

import com.dbtechprojects.mapnote.ui.GenericAppBar
import com.example.mapnote.BottomBarScreen

import com.example.mapnote.R
import com.example.mapnote.data.model.Note
import com.example.mapnote.ui.notesList.NotesFab
import com.example.mapnote.ui.viewmodel.AuthViewModel

import com.example.mapnote.ui.viewmodel.MapNoteViewModel
import com.example.mapnote.ui.theme.Blue
import com.example.mapnote.ui.theme.MapNoteTheme
import com.example.mapnote.util.Util

import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects
import java.util.UUID


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CreateNoteScreen(
    navController: NavController,
    mapNoteViewModel: MapNoteViewModel,
    authViewModel: AuthViewModel,
    lat: Double,
    lng: Double,
    noteId: String
) {
    val context = LocalContext.current
    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        context.packageName + ".provider", file
    )
    val coroutineScope = rememberCoroutineScope()
    mapNoteViewModel.getNote(noteId)

    val note by mapNoteViewModel.getNoteById.collectAsState()
    val currentNoteContent = remember { mutableStateOf("") }
    val currentTitle = remember { mutableStateOf("") }
    val currentPhotos = remember { mutableStateOf("") }
    val currentUser = authViewModel.currentUser
    val currentUserName = remember { mutableStateOf(note?.userName ?: currentUser?.displayName ?: "") }
    var saveButtonState = remember {
        mutableStateOf(false)
    }
    var capturedImageUri by remember {
        mutableStateOf<Uri>(Uri.EMPTY)
    }
    var base64Image by remember {
        mutableStateOf(note?.imageBase64 ?: "")
    }
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()){
            capturedImageUri = uri
            base64Image = Util.fileToBase64(context,capturedImageUri) ?: ""
        }


    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        if (it)
        {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            cameraLauncher.launch(uri)
        }
        else
        {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(currentNoteContent.value, currentTitle.value) {
        coroutineScope.launch {
            // Can not save the other note
            val isSaveAble = currentUser?.uid == note?.userId || note == null
            saveButtonState.value = isSaveAble && currentTitle.value.isNotEmpty() && currentNoteContent.value.isNotEmpty()
        }
    }
    LaunchedEffect(note) {
        currentNoteContent.value = note?.note ?: ""
        currentTitle.value = note?.title ?: ""
        currentPhotos.value = note?.imageBase64 ?: ""
    }


    MapNoteTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.primary) {
            Scaffold(
                topBar = {
                    GenericAppBar(
                        title = if(note == null) "Create Note" else "Edit Note",
                        icon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.save),
                                contentDescription = stringResource(R.string.save_note),
                                tint = Color.Black,
                            )
                        },
                        onIconClick = {
                            // Click to save note
                            // Check if update or create new note
                            if(note != null){
                                var imageBase64 = Util.fileToBase64(context,capturedImageUri)
                                if(capturedImageUri == Uri.EMPTY){
                                    imageBase64 = note!!.imageBase64
                                }
                                val newNote = Note(
                                    id = note!!.id,
                                    title = currentTitle.value,
                                    note = currentNoteContent.value,
                                    dateUpdated = Date(),
                                    imageBase64 = imageBase64,
                                    lat = lat,
                                    lng = lng,
                                    userId = authViewModel.currentUser?.uid ?: "",
                                    userName = authViewModel.currentUser?.displayName ?: "",
                                )
                                mapNoteViewModel.updateNote(newNote!!)
                            }
                            else{
                                mapNoteViewModel.addNote(
                                    Note(
                                        id = UUID.randomUUID().toString(),
                                        title = currentTitle.value,
                                        note = currentNoteContent.value,
                                        dateUpdated = Date(),
                                        imageBase64 = Util.fileToBase64(context,capturedImageUri),
                                        lat = lat,
                                        lng = lng,
                                        userId = authViewModel.currentUser?.uid ?: "",
                                        userName = authViewModel.currentUser?.displayName ?: "",
                                    )
                                )
                            }
                            navController.popBackStack()
                        },
                        iconState = saveButtonState
                    )
                },
                floatingActionButton = {
                    if(currentUser?.uid == note?.userId || note == null){
                        NotesFab(
                            contentDescription = stringResource(R.string.add_image),
                            action = {
                                // Get take picture permission
                                val permissionCheckResult =
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED)
                                {
                                    cameraLauncher.launch(uri)
                                }
                                else
                                {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            icon = R.drawable.camera
                        )
                    }
                },

                content = {
                    Column(
                        Modifier
                            .padding(12.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Convert to base64image to load and save to fireStore
                        val bitmap = Util.base64ToBitmap(base64Image)
                        bitmap?.asImageBitmap()?.let { BitmapPainter(it) }?.let {
                            Image(
                                painter = it,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(6.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        // Check if the note is the current user's note
                        val isEditable = currentUser?.uid == note?.userId || note == null
                        Spacer(modifier = Modifier.height(12.dp))
                        InputTextCustomize("UserName:",50,currentUserName, false)
                        InputTextCustomize("Title:",50,currentTitle,isEditable )
                        InputTextCustomize("Content:",200,currentNoteContent,isEditable)

                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Text(
                                    text = "Latitude:",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                )
                                Text(
                                    text = lng.toString(),
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                )
                            }

                            Column(
                                Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Longitude:",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                )
                                Text(
                                    text = lat.toString(),
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if(note!=null){
                            Button(modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp) ,onClick = {
                                navController.navigate(BottomBarScreen.Map.route + "/${note?.lat}/${note?.lng}")
                            }) {
                                Text(text = "Navigate to map")
                            }
                        }
                    }
                }

            )
        }
    }
}
@Composable
fun InputTextCustomize(title: String, maxLength: Int, text: MutableState<String>, editAble: Boolean){
    Column {
        val lightBlue = Color(0xffd8e6ff)
        val blue = Color(0xff76a9ff)
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = text.value,
            label = {
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    color = Blue
                )},
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = lightBlue,
                cursorColor = Color.Black,
                disabledLabelColor = lightBlue,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent

            ),
            onValueChange = {
                text.value = it
            },
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            trailingIcon = {
                if (text.value.isNotEmpty() && editAble) {
                    IconButton(onClick = { text.value = "" }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null
                        )
                    }
                }
            },
            enabled = editAble
        )
        Text(
            text = if(editAble) "${text.value.length} / $maxLength"  else "",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            textAlign = TextAlign.End,
            color = blue,

            )
    }
    Spacer(modifier = Modifier.height(6.dp))

}
fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH:mm:ss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        externalCacheDir
    )

    return image
}

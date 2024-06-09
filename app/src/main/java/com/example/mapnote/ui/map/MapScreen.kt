package com.example.mapnote.ui.map

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mapnote.R
import com.example.mapnote.data.model.Note
import com.example.mapnote.data.model.getDay
import com.example.mapnote.ui.viewmodel.MapNoteViewModel
import com.example.mapnote.util.Constants
import com.example.mapnote.util.Util
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MapScreen(navController: NavController, mapNoteViewModel: MapNoteViewModel, latLng:LatLng?) {

    val currentPosition = remember { mutableStateOf(latLng ?: LatLng(0.0,0.0)) }
    val thisContext = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(thisContext)
    val scope = rememberCoroutineScope()
    LaunchedEffect(true) {
        scope.launch(Dispatchers.IO) {
            mapNoteViewModel.getNotes()
            if(latLng == null){
                mapNoteViewModel.getDeviceLocation(fusedLocationClient)
            }
        }
    }

    val mapProperties = MapProperties(
        isMyLocationEnabled = true,
    )
    var isConfigPosition by remember {
        mutableStateOf(false)
    }
    // Load current position
    val positionState by mapNoteViewModel.currentPosition.collectAsState(LatLng(0.0,0.0))
    LaunchedEffect(positionState) {
        if(latLng != null){
            currentPosition.value = latLng
        }
        else{
            currentPosition.value = positionState ?: LatLng(0.0, 0.0)
        }

    }

    val cameraPositionState = rememberCameraPositionState{
        position = CameraPosition.fromLatLngZoom(currentPosition.value, 20f)
    }
    LaunchedEffect(currentPosition) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentPosition.value, 20f)
    }
    // Load notes
    val notes by mapNoteViewModel.notes.collectAsState(emptyList())


    Scaffold(
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column {
                    FloatingActionButton(
                        onClick = {
                            // Config for user change the current position on the map
                            if(!isConfigPosition){
                                Toast.makeText(thisContext, "You can change your position now !", Toast.LENGTH_SHORT).show()
                            }
                            isConfigPosition = !isConfigPosition
                        },
                        backgroundColor = MaterialTheme.colors.primary
                    ) {
                        Icon(imageVector = Icons.Default.Edit, tint = Color.Black, contentDescription = "Add")
                    }
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(
                                Constants.NAVIGATION_NOTES_CREATE + "/${cameraPositionState.position.target.latitude}/${cameraPositionState.position.target.longitude}/{noteId}"
                            )
                        },
                        modifier = Modifier.padding(top = 16.dp),
                        backgroundColor = MaterialTheme.colors.primary
                    ) {
                        Icon(imageVector = Icons.Default.Add,  tint = Color.Black, contentDescription = "Edit")
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Start,
        ){
        val bottomPadding = it.calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    properties = mapProperties,
                    cameraPositionState = cameraPositionState
                ){
                    notes.forEach{
                        MakerFromNote(note = it)
                    }
                }

                if(isConfigPosition){
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                // Handle click
                            }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.pin),
                                modifier = Modifier.size(24.dp),
                                contentDescription = "marker",
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun MakerFromNote(note: Note){
    val position = LatLng(note.lat, note.lng)

    MarkerInfoWindow(
        state = MarkerState(position),
        title = note.title,
        snippet = note.note,
        content = {
            CustomerInfoWindow(note)
        }
    )
}

// Customer the Marker information window
@Composable
fun CustomerInfoWindow(note:Note){
    Box(
        modifier = Modifier
            .background(
            color = MaterialTheme.colors.onSecondary,
            shape = RoundedCornerShape(35.dp, 35.dp, 35.dp, 35.dp)
        )
    ){
        Column(
            modifier = Modifier
                .padding(16.dp).background(MaterialTheme.colors.onSecondary)
        ) {
            Row(modifier = Modifier, horizontalArrangement = Arrangement.Center) {
                val bitmap = Util.base64ToBitmap(note.imageBase64 ?: "")
                bitmap?.asImageBitmap()?.let { BitmapPainter(it) }?.let {
                    Image(
                        painter = it,
                        contentDescription = null,
                        modifier = Modifier
                            .width(60.dp)
                            .height(60.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                Column {

                    Text(
                        text = note.userName,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                    Text(
                        text = note.title,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                    Text(
                        text = note.note,
                        color = Color.Black,
                        maxLines = 2,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
            Text(
                text = note.getDay(),
                color = Color.Black,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .align(Alignment.End)
            )
        }
    }

}

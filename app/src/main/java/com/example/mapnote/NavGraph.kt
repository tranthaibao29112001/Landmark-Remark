package com.example.mapnote

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mapnote.ui.map.MapScreen
import com.example.mapnote.ui.viewmodel.AuthViewModel
import com.example.mapnote.ui.viewmodel.MapNoteViewModel
import com.example.mapnote.ui.createNote.CreateNoteScreen
import com.example.mapnote.ui.login.LoginScreen
import com.example.mapnote.ui.notesList.NotesList
import com.example.mapnote.ui.signup.SignupScreen
import com.example.mapnote.util.Constants
import com.google.android.gms.maps.model.LatLng

@Composable
fun BottomNavGraph(navController: NavHostController, mapNoteViewModel : MapNoteViewModel, authViewModel: AuthViewModel) {
    NavHost(
        navController = navController,
        startDestination = Constants.ROUTE_LOGIN

    ) {

        composable(route = BottomBarScreen.Home.route) {
            NotesList(navController, mapNoteViewModel,authViewModel)
        }
        composable(route = BottomBarScreen.Map.route) {
            MapScreen(navController, mapNoteViewModel, null)
        }
        composable(
            Constants.NAVIGATION_NOTES_CREATE_POSITION,
            arguments = listOf(
                navArgument(Constants.NAVIGATION_Lat_Argument) {type = NavType.StringType  },
                navArgument(Constants.NAVIGATION_Lng_Argument) {type = NavType.StringType },
                navArgument(Constants.NAVIGATION_NoteId_Argument) {type = NavType.StringType }),
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString(Constants.NAVIGATION_Lat_Argument)?.toDoubleOrNull() ?:0.0
            val lng = backStackEntry.arguments?.getString(Constants.NAVIGATION_Lng_Argument)?.toDoubleOrNull() ?:0.0
            val noteId = backStackEntry.arguments?.getString(Constants.NAVIGATION_NoteId_Argument) ?: ""
            CreateNoteScreen(
                navController,
                mapNoteViewModel,
                authViewModel,
                lat, lng, noteId
            )
        }
        composable(
            Constants.NAVIGATION_MAP_POSITION,
            arguments = listOf(
                navArgument(Constants.NAVIGATION_Lat_Argument) {type = NavType.StringType  },
                navArgument(Constants.NAVIGATION_Lng_Argument) {type = NavType.StringType }),
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString(Constants.NAVIGATION_Lat_Argument)?.toDoubleOrNull() ?:0.0
            val lng = backStackEntry.arguments?.getString(Constants.NAVIGATION_Lng_Argument)?.toDoubleOrNull() ?:0.0
            MapScreen(navController, mapNoteViewModel, LatLng(lat,lng))
        }
        composable(Constants.ROUTE_LOGIN) {
            LoginScreen(authViewModel, navController)
        }
        composable(Constants.ROUTE_SIGNUP) {
            SignupScreen(authViewModel, navController)
        }
    }
}
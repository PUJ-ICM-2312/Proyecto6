package com.example.travelscolombia

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.travelscolombia.ui.navigation.BottomNavBar
import com.example.travelscolombia.ui.navigation.BottomNavItem
import com.example.travelscolombia.ui.screens.*

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()

    val navItems = listOf(
        BottomNavItem("home", "Inicio", R.drawable.home),
        BottomNavItem("map", "Mapa", R.drawable.map),
        BottomNavItem("friends", "Amigos", R.drawable.friends),
        BottomNavItem("profile", "Perfil", R.drawable.profile),
        BottomNavItem("more", "Más", R.drawable.more)
    )

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController, items = navItems)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("map") { MapScreen() }
            composable("friends") { FriendsScreen(navController = navController) }
            composable("profile") { ProfileScreen() }
            composable("more") { MoreScreen(onLogout = onLogout) }

            composable(
                "chat/{friendId}/{friendName}/{friendPhoto}",
                arguments = listOf(
                    navArgument("friendId") { type = NavType.StringType },
                    navArgument("friendName") { type = NavType.StringType },
                    navArgument("friendPhoto") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                ChatScreen(
                    friendId = backStackEntry.arguments?.getString("friendId") ?: "",
                    friendName = backStackEntry.arguments?.getString("friendName") ?: "",
                    friendPhoto = backStackEntry.arguments?.getString("friendPhoto")
                )
            }
        }
    }
}

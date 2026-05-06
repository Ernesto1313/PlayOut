package com.ernesto.playout

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ernesto.playout.ui.detail.DetailScreen
import com.ernesto.playout.ui.list.ListScreen
import com.ernesto.playout.ui.map.MapScreen
import com.ernesto.playout.ui.onboarding.PermissionScreen
import com.ernesto.playout.ui.theme.PlayOutTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String) {
    object Permission : Screen("permission")
    object Map : Screen("map")
    object List : Screen("list")
    object Detail : Screen("detail/{fid}") {
        fun createRoute(fid: Int) = "detail/$fid"
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlayOutTheme {
                val context = LocalContext.current
                val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                val permissionDone = prefs.getBoolean("location_permission_requested", false)

                val navController = rememberNavController()

                if (!permissionDone) {
                    NavHost(navController = navController, startDestination = Screen.Permission.route) {
                        composable(Screen.Permission.route) {
                            PermissionScreen(onDone = {
                                navController.navigate(Screen.Map.route) {
                                    popUpTo(Screen.Permission.route) { inclusive = true }
                                }
                            })
                        }
                        composable(Screen.Map.route) { MapScreen() }
                        composable(Screen.List.route) {
                            ListScreen(onInstalacionClick = { fid ->
                                navController.navigate(Screen.Detail.createRoute(fid))
                            })
                        }
                        composable(
                            route = Screen.Detail.route,
                            arguments = listOf(navArgument("fid") { type = NavType.IntType })
                        ) {
                            DetailScreen(onBack = { navController.navigateUp() })
                        }
                    }
                } else {
                    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(Icons.Default.Map, contentDescription = "Mapa") },
                                    label = { Text("Mapa") }
                                )
                                NavigationBarItem(
                                    selected = false,
                                    onClick = { /* no-op */ },
                                    icon = {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF4CAF50),
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = "Añadir",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    },
                                    label = {}
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    icon = { Icon(Icons.Default.List, contentDescription = "Lista") },
                                    label = { Text("Lista") }
                                )
                            }
                        }
                    ) { innerPadding ->
                        val mainNavController = rememberNavController()

                        NavHost(
                            navController = mainNavController,
                            startDestination = Screen.Map.route
                        ) {
                            composable(Screen.Map.route) { MapScreen() }
                            composable(Screen.List.route) {
                                ListScreen(onInstalacionClick = { fid ->
                                    mainNavController.navigate(Screen.Detail.createRoute(fid))
                                })
                            }
                            composable(
                                route = Screen.Detail.route,
                                arguments = listOf(navArgument("fid") { type = NavType.IntType })
                            ) {
                                DetailScreen(onBack = { mainNavController.navigateUp() })
                            }
                        }

                        androidx.compose.runtime.LaunchedEffect(selectedTab) {
                            when (selectedTab) {
                                0 -> mainNavController.navigate(Screen.Map.route) {
                                    popUpTo(Screen.Map.route) { inclusive = true }
                                }
                                2 -> mainNavController.navigate(Screen.List.route) {
                                    popUpTo(Screen.Map.route)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

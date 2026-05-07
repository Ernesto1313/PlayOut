package com.ernesto.playout

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
    @OptIn(ExperimentalMaterial3Api::class)
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
                        composable(Screen.Map.route) {
                            MapScreen(onInstalacionClick = { fid ->
                                navController.navigate(Screen.Detail.createRoute(fid))
                            })
                        }
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
                    val mainNavController = rememberNavController()
                    val currentBackStackEntry by mainNavController.currentBackStackEntryAsState()
                    val currentRoute = currentBackStackEntry?.destination?.route

                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4CAF50),
                        selectedTextColor = Color(0xFF4CAF50),
                        unselectedIconColor = Color(0xFF8B949E),
                        unselectedTextColor = Color(0xFF8B949E),
                        indicatorColor = Color.Transparent
                    )

                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = "PlayOut",
                                        color = Color(0xFFF5F5F5),
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                navigationIcon = {
                                    Box(modifier = Modifier.size(48.dp))
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = Color(0xFF2C332D)
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(containerColor = Color(0xFF2C332D)) {
                                NavigationBarItem(
                                    selected = currentRoute == Screen.List.route,
                                    onClick = {
                                        mainNavController.navigate(Screen.List.route) {
                                            popUpTo(Screen.Map.route)
                                            launchSingleTop = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            Icons.Default.FormatListBulleted,
                                            contentDescription = "Lista"
                                        )
                                    },
                                    label = { Text("Lista") },
                                    colors = navItemColors
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
                                    label = {},
                                    colors = navItemColors
                                )
                                NavigationBarItem(
                                    selected = false,
                                    onClick = { /* no-op */ },
                                    icon = {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = "Ajustes"
                                        )
                                    },
                                    label = { Text("Ajustes") },
                                    colors = navItemColors
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = mainNavController,
                            startDestination = Screen.Map.route,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable(Screen.Map.route) {
                                MapScreen(
                                    onInstalacionClick = { fid ->
                                        mainNavController.navigate(Screen.Detail.createRoute(fid))
                                    },
                                    contentPadding = innerPadding
                                )
                            }
                            composable(Screen.List.route) {
                                Box(modifier = Modifier.padding(innerPadding)) {
                                    ListScreen(onInstalacionClick = { fid ->
                                        mainNavController.navigate(Screen.Detail.createRoute(fid))
                                    })
                                }
                            }
                            composable(
                                route = Screen.Detail.route,
                                arguments = listOf(navArgument("fid") { type = NavType.IntType })
                            ) {
                                Box(modifier = Modifier.padding(innerPadding)) {
                                    DetailScreen(onBack = { mainNavController.navigateUp() })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

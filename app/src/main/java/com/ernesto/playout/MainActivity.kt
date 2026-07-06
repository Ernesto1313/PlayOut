package com.ernesto.playout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ernesto.playout.ui.add.AddFacilityScreen
import com.ernesto.playout.ui.add.EditFacilityScreen
import com.ernesto.playout.ui.detail.DetailScreen
import com.ernesto.playout.ui.feed.FeedScreen
import com.ernesto.playout.ui.map.MapScreen
import com.ernesto.playout.ui.settings.SettingsScreen
import com.ernesto.playout.ui.theme.PlayOutTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String) {
    object Map : Screen("map")
    object Feed : Screen("feed")
    object Detail : Screen("detail/{fid}") {
        fun createRoute(fid: Int) = "detail/$fid"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapListScaffold(
    currentRoute: String,
    navController: NavController,
    content: @Composable (PaddingValues) -> Unit
) {
    val onMap = currentRoute == Screen.Map.route
    val onFeed = currentRoute == Screen.Feed.route
    val showTopBar = currentRoute == Screen.Map.route || currentRoute == Screen.Feed.route

    Scaffold(
        topBar = {
            if (showTopBar) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "PlayOut",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            color = Color(0xFF00AEFF),
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color(0xFFF5F5F5),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 0f
                                )
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF806B40)
                    )
                )
            }
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFF806B40),
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = {
                        if (onMap) {
                            navController.navigate(Screen.Feed.route) {
                                popUpTo(Screen.Map.route)
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate(Screen.Map.route) {
                                popUpTo(Screen.Feed.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (onMap) Icons.Default.GridView else Icons.Default.Place,
                            contentDescription = if (onMap) "Grid" else "Map",
                            tint = if (onFeed) Color(0xFF00AEFF) else Color(0xFFF5F5F5)
                        )
                    }
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(
                        onClick = { navController.navigate("add") },
                        modifier = Modifier.size(48.dp),
                        containerColor = Color(0xFF00AEFF),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFFF5F5F5)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlayOutTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Map.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Map.route) {
                        MapListScaffold(
                            currentRoute = Screen.Map.route,
                            navController = navController
                        ) { padding ->
                            MapScreen(
                                onInstalacionClick = { fid ->
                                    navController.navigate(Screen.Detail.createRoute(fid))
                                },
                                contentPadding = padding
                            )
                        }
                    }
                    composable(Screen.Feed.route) {
                        MapListScaffold(
                            currentRoute = Screen.Feed.route,
                            navController = navController
                        ) { padding ->
                            FeedScreen(
                                onInstalacionClick = { fid ->
                                    navController.navigate(Screen.Detail.createRoute(fid))
                                },
                                contentPadding = padding
                            )
                        }
                    }
                    composable(
                        route = Screen.Detail.route,
                        arguments = listOf(navArgument("fid") { type = NavType.IntType })
                    ) {
                        DetailScreen(onBack = { navController.navigateUp() })
                    }
                    composable("settings") {
                        SettingsScreen(
                            onBack = { navController.navigateUp() },
                            onEditFacility = { fid ->
                                navController.navigate("edit/$fid")
                            }
                        )
                    }
                    composable("add") {
                        AddFacilityScreen(
                            onBack = { navController.navigateUp() },
                            onNavigateToSettings = {
                                navController.navigate("settings") {
                                    popUpTo("settings") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(
                        route = "edit/{fid}",
                        arguments = listOf(navArgument("fid") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val fid = backStackEntry.arguments?.getInt("fid")
                            ?: return@composable
                        EditFacilityScreen(
                            fid = fid,
                            onBack = { navController.navigateUp() },
                            onNavigateToSettings = {
                                navController.navigate("settings") {
                                    popUpTo("settings") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

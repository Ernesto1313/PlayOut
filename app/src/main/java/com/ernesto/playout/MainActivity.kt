package com.ernesto.playout

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ernesto.playout.ui.add.AddInstalacionScreen
import com.ernesto.playout.ui.add.EditInstalacionScreen
import com.ernesto.playout.ui.detail.DetailScreen
import com.ernesto.playout.ui.list.ListScreen
import com.ernesto.playout.ui.map.MapScreen
import com.ernesto.playout.ui.onboarding.PermissionScreen
import com.ernesto.playout.ui.settings.SettingsScreen
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapListScaffold(
    currentRoute: String,
    navController: NavController,
    content: @Composable (PaddingValues) -> Unit
) {
    val onMap = currentRoute == Screen.Map.route

    Scaffold(

            topBar = {
                if (onMap) {
                    TopAppBar(
                        title = {
                            Image(
                                painter = painterResource(R.drawable.playout_banner),
                                contentDescription = "PlayOut",
                                modifier = Modifier.height(36.dp),
                                contentScale = ContentScale.Fit
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
                            navController.navigate(Screen.List.route) {
                                popUpTo(Screen.Map.route)
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate(Screen.Map.route) {
                                popUpTo(Screen.Map.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (onMap) Icons.Default.FormatListBulleted else Icons.Default.Place,
                            contentDescription = if (onMap) "Lista" else "Mapa",
                            tint = Color(0xFFF5F5F5)
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
                        Icon(Icons.Default.Add, contentDescription = "Añadir")
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Ajustes",
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

                    NavHost(
                        navController = mainNavController,
                        startDestination = Screen.Map.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Screen.Map.route) {
                            MapListScaffold(
                                currentRoute = Screen.Map.route,
                                navController = mainNavController
                            ) { padding ->
                                MapScreen(
                                    onInstalacionClick = { fid ->
                                        mainNavController.navigate(Screen.Detail.createRoute(fid))
                                    },
                                    contentPadding = padding
                                )
                            }
                        }
                        composable(Screen.List.route) {
                            MapListScaffold(
                                currentRoute = Screen.List.route,
                                navController = mainNavController
                            ) { padding ->
                                Box(modifier = Modifier.padding(padding)) {
                                    ListScreen(onInstalacionClick = { fid ->
                                        mainNavController.navigate(Screen.Detail.createRoute(fid))
                                    })
                                }
                            }
                        }
                        composable(
                            route = Screen.Detail.route,
                            arguments = listOf(navArgument("fid") { type = NavType.IntType })
                        ) {
                            DetailScreen(onBack = { mainNavController.navigateUp() })
                        }
                        composable("settings") {
                            SettingsScreen(
                                onBack = { mainNavController.navigateUp() },
                                onEditInstalacion = { fid ->
                                    mainNavController.navigate("edit/$fid")
                                }
                            )
                        }
                        composable("add") {
                            AddInstalacionScreen(onBack = { mainNavController.navigateUp() })
                        }
                        composable(
                            route = "edit/{fid}",
                            arguments = listOf(navArgument("fid") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val fid = backStackEntry.arguments?.getInt("fid")
                                ?: return@composable
                            EditInstalacionScreen(
                                fid = fid,
                                onBack = { mainNavController.navigateUp() }
                            )
                        }
                    }
                }
            }
        }
    }
}

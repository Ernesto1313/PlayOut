package com.ernesto.playout

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ernesto.playout.ui.detail.DetailScreen
import com.ernesto.playout.ui.list.ListScreen
import com.ernesto.playout.ui.onboarding.PermissionScreen
import com.ernesto.playout.ui.theme.PlayOutTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String) {
    object Permission : Screen("permission")
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
                val startDest = if (prefs.getBoolean("location_permission_requested", false)) {
                    Screen.List.route
                } else {
                    Screen.Permission.route
                }

                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = startDest) {
                    composable(Screen.Permission.route) {
                        PermissionScreen(onDone = {
                            navController.navigate(Screen.List.route) {
                                popUpTo(Screen.Permission.route) { inclusive = true }
                            }
                        })
                    }
                    composable(Screen.List.route) {
                        ListScreen(
                            onInstalacionClick = { fid ->
                                navController.navigate(Screen.Detail.createRoute(fid))
                            }
                        )
                    }
                    composable(
                        route = Screen.Detail.route,
                        arguments = listOf(navArgument("fid") { type = NavType.IntType })
                    ) {
                        DetailScreen(onBack = { navController.navigateUp() })
                    }
                }
            }
        }
    }
}

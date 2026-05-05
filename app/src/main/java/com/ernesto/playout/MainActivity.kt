package com.ernesto.playout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ernesto.playout.ui.detail.DetailScreen
import com.ernesto.playout.ui.list.ListScreen
import com.ernesto.playout.ui.theme.PlayOutTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String) {
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
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Screen.List.route) {
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

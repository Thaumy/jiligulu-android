package component

import java.util.*
import global.AppRoute
import unilang.alias.*
import android.os.Build
import component.screen.*
import global.bottomNavBarItems
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.annotation.RequiresApi
import android.annotation.SuppressLint
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.compose.foundation.layout.*
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.composable
import androidx.compose.material.icons.filled.*
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
) {
    val navController = rememberNavController()
    val state by navController.currentBackStackEntryAsState()

    var title by remember { mutableStateOf("Posts") }

    val idNavArg = navArgument("id") { type = NavType.LongType }
    val getIdNavArg = { entry: NavBackStackEntry ->
        entry.arguments!!.getLong("id")
    }

    Scaffold(
        modifier = Modifier,
        floatingActionButton = {
            state?.destination?.hierarchy?.any {
                when (it.route) {
                    AppRoute.POST_LIST -> {
                        CreateButton {
                            navController.navigate(AppRoute.CREATE_POST)
                        }
                        true
                    }
                    AppRoute.COMMENT_LIST -> {
                        CreateButton {
                            navController.navigate(AppRoute.CREATE_COMMENT)
                        }
                        true
                    }
                    else -> false
                }
            }
        },
        topBar = {
            TopBar(title)
        },
        bottomBar = {
            BottomNavBar(navController, bottomNavBarItems) {
                navController.navigate(it)
            }
        }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.POST_LIST
        ) {
            composable(AppRoute.POST_LIST) {
                title = "Posts"

                PostScreen(
                    contentPadding = contentPadding,
                    navToPostDiff = { id: i64 ->
                        navController
                            .navigate("${AppRoute.POST_DIFF}/$id")
                    },
                    navToPostEditor = { id: i64 ->
                        navController
                            .navigate("${AppRoute.MODIFY_POST}/$id")
                    }
                )
            }
            composable(
                "${AppRoute.POST_DIFF}/{id}",
                arguments = listOf(idNavArg)
            ) { entry ->
                title = "Post conflict"

                val id = getIdNavArg(entry)
                PostDiffScreen(contentPadding = contentPadding, id = id)
            }
            composable(AppRoute.CREATE_POST) {
                title = "Create post"

                PostEditScreen(contentPadding = contentPadding, id = Optional.empty())
            }
            composable(
                "${AppRoute.MODIFY_POST}/{id}",
                arguments = listOf(idNavArg)
            ) { entry ->
                title = "Edit post"

                val id = getIdNavArg(entry)
                PostEditScreen(contentPadding = contentPadding, id = Optional.of(id))
            }

            composable(AppRoute.COMMENT_LIST) {
                title = "Comments"

                CommentScreen(
                    contentPadding = contentPadding,
                    navToCommentDiff = { id: i64 ->
                        navController
                            .navigate("${AppRoute.COMMENT_DIFF}/$id")
                    },
                    navToCommentEditor = { id: i64 ->
                        navController
                            .navigate("${AppRoute.MODIFY_COMMENT}/$id")
                    }
                )
            }
            composable(
                "${AppRoute.COMMENT_DIFF}/{id}",
                arguments = listOf(idNavArg)
            ) { entry ->
                title = "Comment conflict"

                val id = getIdNavArg(entry)
                CommentDiffScreen(contentPadding = contentPadding, id = id)
            }
            composable(AppRoute.CREATE_COMMENT) {
                title = "Create comment"

                CommentEditScreen(contentPadding = contentPadding, id = Optional.empty())
            }
            composable(
                "${AppRoute.MODIFY_COMMENT}/{id}",
                arguments = listOf(idNavArg)
            ) { entry ->
                title = "Edit comment"

                val id = getIdNavArg(entry)
                CommentEditScreen(contentPadding = contentPadding, id = Optional.of(id))
            }

            composable(AppRoute.SETTINGS) {
                title = "Settings"

                SettingsScreen(contentPadding = contentPadding)
            }
        }
    }
}
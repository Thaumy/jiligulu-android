package data.ui

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavBarItemData(
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

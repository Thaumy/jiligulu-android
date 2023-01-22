package component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (isSystemInDarkTheme())
            darkColorScheme()
        else
            lightColorScheme()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        shapes = Shapes(),
    ) {
        content()
    }
}
package component.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.editor.PostEditor
import data.ui.PostEditMode
import unilang.alias.i64
import java.util.*

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun PostEditScreen(
    contentPadding: PaddingValues,
    mode: PostEditMode,
    navBack: () -> Unit
) {
    Column(Modifier.padding(contentPadding)) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(10.dp))

            PostEditor(
                mode,
                navBack
            )
        }
    }
}

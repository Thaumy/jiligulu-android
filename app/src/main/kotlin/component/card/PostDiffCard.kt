package component.card

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.db.LocalCommentDbSingleton
import data.db.LocalPostDbSingleton
import data.grpc.PostServiceSingleton
import data.ui.ConflictType
import data.ui.PostData
import data.ui.sha256
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.FillMaxWidthModifier
import unilang.time.format
import unilang.time.yyMdHmm
import unilang.type.whenFalse
import java.util.*

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("SimpleDateFormat")
@Composable
fun PostDiffCard(
    afterApplyLocal: () -> Unit,
    afterApplyRemote: () -> Unit,
    showSnackBar: (String) -> Unit,
    localData: Optional<PostData>,
    remoteData: Optional<PostData>,
) {
    val conflictType = when {
        localData.isPresent && remoteData.isEmpty -> ConflictType.LocalOnly
        localData.isEmpty && remoteData.isPresent -> ConflictType.RemoteOnly
        localData.isPresent && remoteData.isPresent -> ConflictType.DataDiff
        else -> return
    }

    val coroutineScope = rememberCoroutineScope()
    val ctx = LocalContext.current

    fun applyLocal() = coroutineScope.launch {
        val service = PostServiceSingleton(ctx).get()
        when (conflictType) {
            ConflictType.LocalOnly -> {
                val postDao = LocalPostDbSingleton(ctx).localPostDao()
                val commentDao = LocalCommentDbSingleton(ctx).localCommentDao()

                val remoteCreated = service.create(localData.get())
                if (remoteCreated.isEmpty) {
                    showSnackBar("Operation failed: please check your network status.")
                    false
                } else {
                    val oldId = localData.get().id
                    val newId = remoteCreated.get().id
                    postDao.chId(oldId, newId)
                    commentDao.chBindingId(oldId, newId, false)
                    true
                }
            }
            ConflictType.RemoteOnly -> service.delete(remoteData.get())
            ConflictType.DataDiff -> service.update(localData.get())
        }.whenFalse(::cancel)
        afterApplyLocal()
    }

    fun applyRemote() = coroutineScope.launch {
        val dao = LocalPostDbSingleton(ctx).localPostDao()
        when (conflictType) {
            ConflictType.LocalOnly -> dao.delete(localData.get().id)
            ConflictType.RemoteOnly -> dao.insert(remoteData.get())
            ConflictType.DataDiff -> dao.update(remoteData.get())
        }
        afterApplyRemote()
    }

    Column(FillMaxWidthModifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Numbers,
                contentDescription = "Post id",
            )
            Text(
                text = localData
                    .or { remoteData }
                    .map { it.id }
                    .orElseThrow()
                    .toString(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = FillMaxWidthModifier,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "Local data"
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "Remote data"
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = FillMaxWidthModifier,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sha256",
                    style = MaterialTheme.typography.labelLarge,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = localData.map { it.sha256() }.orElse("-"),
                    style = MaterialTheme.typography.labelMedium,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sha256",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = remoteData.map { it.sha256() }.orElse("-"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = FillMaxWidthModifier,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Title",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = localData.map { it.title }.orElse("-"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Title",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = remoteData.map { it.title }.orElse("-"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = FillMaxWidthModifier,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Body",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = localData.map { it.body }.orElse("-"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Body",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = remoteData.map { it.body }.orElse("-"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = FillMaxWidthModifier,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Modify time",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = localData.map { it.modifyTime.format(yyMdHmm) }.orElse("-"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Modify time",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = remoteData.map { it.modifyTime.format(yyMdHmm) }.orElse("-"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = FillMaxWidthModifier,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = ::applyLocal,
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                val (text, icon) = when (conflictType) {
                    ConflictType.RemoteOnly -> Pair(
                        "Delete Remote",
                        Icons.Rounded.Delete
                    )
                    else -> Pair(
                        "Push Local",
                        Icons.Rounded.FileUpload
                    )
                }
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = icon,
                    contentDescription = text
                )
                Text(text = text, fontSize = 12.sp)
            }

            Button(
                onClick = ::applyRemote,
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                val (text, icon) = when (conflictType) {
                    ConflictType.LocalOnly -> Pair(
                        "Delete Local",
                        Icons.Rounded.Delete
                    )
                    else -> Pair(
                        "Fetch Remote",
                        Icons.Rounded.FileDownload
                    )
                }
                Text(text = text, fontSize = 12.sp)
                Spacer(Modifier.width(2.dp))
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = icon,
                    contentDescription = text
                )
            }
        }
    }
}

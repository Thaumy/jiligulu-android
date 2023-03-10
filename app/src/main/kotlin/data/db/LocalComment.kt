package data.db

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.*
import data.db.converter.DateConverter
import data.ui.CommentData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import unilang.alias.i64
import unilang.type.none
import unilang.type.some
import java.util.*

@Dao
interface LocalCommentDao {
    @Query("SELECT * FROM local_comment WHERE comment_id = (:id)")
    suspend fun maybe(id: i64): CommentData?

    @Query("SELECT * FROM local_comment WHERE comment_id = (:id)")
    suspend fun getOne(id: i64): CommentData

    @Query("SELECT * FROM local_comment")
    suspend fun getAll(): List<CommentData>

    @Insert
    suspend fun insert(data: CommentData)

    @Update
    suspend fun update(data: CommentData)

    @Query("DELETE FROM local_comment WHERE comment_id = (:id)")
    suspend fun delete(id: i64)

    @Query("UPDATE local_comment SET comment_id = (:newId) WHERE comment_id = (:oldId)")
    suspend fun chId(oldId: i64, newId: i64)

    @Query("UPDATE local_comment SET comment_binding_id = (:newId) WHERE comment_binding_id = (:oldId) AND comment_is_reply = (:isReply)")
    suspend fun chBindingId(oldId: i64, newId: i64, isReply: Boolean)

    @Query("SELECT MIN(comment_id) FROM local_comment")
    suspend fun getMinId(): i64?
}

@TypeConverters(DateConverter::class)
@Database(entities = [CommentData::class], version = 1)
abstract class LocalCommentDatabase : RoomDatabase() {
    abstract fun localCommentDao(): LocalCommentDao
}

object LocalCommentDbSingleton {
    private var db = none<LocalCommentDatabase>()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend operator fun invoke(ctx: Context) =
        withContext(Dispatchers.IO) {
            if (db.isEmpty)
                synchronized(this) {
                    db = Room
                        .databaseBuilder(
                            ctx,
                            LocalCommentDatabase::class.java,
                            "local_comment_database"
                        )
                        .build()
                        .some()
                }

            db.get()
        }
}

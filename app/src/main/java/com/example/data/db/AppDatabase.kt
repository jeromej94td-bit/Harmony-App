package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AnswerEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.CoupleStatsEntity
import com.example.data.model.MomentEntity
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.ProfileEntity
import com.example.data.model.SharedPicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: ProfileEntity)
}

@Dao
interface AnswerDao {
    @Query("SELECT * FROM answers")
    fun getAllAnswers(): Flow<List<AnswerEntity>>

    @Query("SELECT * FROM answers WHERE packId = :packId")
    fun getAnswersForPack(packId: String): Flow<List<AnswerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: AnswerEntity)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
}

@Dao
interface SharedPicDao {
    @Query("SELECT * FROM shared_pics ORDER BY timestamp DESC")
    fun getAllPics(): Flow<List<SharedPicEntity>>

    @Query("SELECT * FROM shared_pics WHERE selectedForWidget = 1 ORDER BY timestamp DESC")
    suspend fun getWidgetPics(): List<SharedPicEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPic(pic: SharedPicEntity): Long

    @Update
    suspend fun updatePic(pic: SharedPicEntity)
}

@Dao
interface MomentDao {
    @Query("SELECT * FROM moments ORDER BY timestamp DESC")
    fun getAllMoments(): Flow<List<MomentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoment(moment: MomentEntity)

    @Query("DELETE FROM moments WHERE id = :id")
    suspend fun deleteMoment(id: Long)
}

@Dao
interface CoupleStatsDao {
    @Query("SELECT * FROM couple_stats WHERE id = 1 LIMIT 1")
    fun getStats(): Flow<CoupleStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: CoupleStatsEntity)
}

@Database(
    entities = [
        ProfileEntity::class,
        AnswerEntity::class,
        ChatMessageEntity::class,
        SharedPicEntity::class,
        MomentEntity::class,
        CoupleStatsEntity::class,
        MemoryCategoryEntity::class,
        MemoryEntryEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class HarmonyDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun answerDao(): AnswerDao
    abstract fun chatDao(): ChatDao
    abstract fun sharedPicDao(): SharedPicDao
    abstract fun momentDao(): MomentDao
    abstract fun coupleStatsDao(): CoupleStatsDao
    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile
        private var INSTANCE: HarmonyDatabase? = null

        fun getInstance(context: Context): HarmonyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HarmonyDatabase::class.java,
                    "harmony_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE profiles ADD COLUMN userAvatarPath TEXT")
                db.execSQL("ALTER TABLE profiles ADD COLUMN partnerAvatarPath TEXT")
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN imagePath TEXT")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS shared_pics (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        filePath TEXT NOT NULL,
                        caption TEXT NOT NULL,
                        addedBy TEXT NOT NULL,
                        target TEXT NOT NULL,
                        status TEXT NOT NULL,
                        selectedForWidget INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS memory_categories (
                        id TEXT NOT NULL PRIMARY KEY,
                        systemKey TEXT,
                        customName TEXT,
                        colorKey TEXT NOT NULL,
                        iconKey TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS memory_entries (
                        id TEXT NOT NULL PRIMARY KEY,
                        categoryId TEXT NOT NULL,
                        kind TEXT NOT NULL,
                        title TEXT NOT NULL,
                        body TEXT,
                        url TEXT,
                        previewTitle TEXT,
                        previewDescription TEXT,
                        previewImageUrl TEXT,
                        previewSiteName TEXT,
                        previewFetchedAt INTEGER,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        completedAt INTEGER,
                        FOREIGN KEY(categoryId) REFERENCES memory_categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_memory_entries_categoryId ON memory_entries(categoryId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_memory_entries_completedAt ON memory_entries(completedAt)")
            }
        }

        internal val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE memory_categories ADD COLUMN isVisible INTEGER NOT NULL DEFAULT 1"
                )
            }
        }
    }
}

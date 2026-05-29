package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "coping_insights")
data class CopingInsight(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val struggleText: String,
    val insightText: String,
    val language: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface CopingInsightDao {
    @Query("SELECT * FROM coping_insights ORDER BY timestamp DESC")
    fun getAllInsights(): Flow<List<CopingInsight>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: CopingInsight)

    @Query("DELETE FROM coping_insights WHERE id = :id")
    suspend fun deleteInsight(id: Int)

    @Query("DELETE FROM coping_insights")
    suspend fun clearAll()
}

@Entity(tableName = "neuro_habits")
data class NeuroHabit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titleEn: String,
    val titleFa: String,
    val descriptionEn: String,
    val descriptionFa: String,
    val iconName: String,
    val streakCount: Int = 0,
    val lastCompletedTimestamp: Long = 0L,
    val isCompletedToday: Boolean = false
)

@Dao
interface NeuroHabitDao {
    @Query("SELECT * FROM neuro_habits")
    fun getAllHabits(): Flow<List<NeuroHabit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: NeuroHabit)

    @Update
    suspend fun updateHabit(habit: NeuroHabit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(habits: List<NeuroHabit>)
}

@Database(entities = [CopingInsight::class, NeuroHabit::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun copingInsightDao(): CopingInsightDao
    abstract fun neuroHabitDao(): NeuroHabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sina_mind_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

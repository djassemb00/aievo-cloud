package com.djb.aievo.core.data

import android.content.Context
import androidx.room.*

@Entity(tableName = "system_status")
data class SystemStatusEntity(
    @PrimaryKey val id: Int = 1,
    val convergenceFactor: Double,
    val activeNodes: Int,
    val credits: Double
)

@Dao
interface SystemStatusDao {
    @Query("SELECT * FROM system_status WHERE id = 1")
    suspend fun getStatus(): SystemStatusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStatus(status: SystemStatusEntity)
}

@Database(entities = [SystemStatusEntity::class], version = 1)
abstract class AIEVODatabase : RoomDatabase() {
    abstract fun systemStatusDao(): SystemStatusDao

    companion object {
        @Volatile private var instance: AIEVODatabase? = null
        fun getInstance(context: Context): AIEVODatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AIEVODatabase::class.java, "aievo_master.db"
                ).build().also { instance = it }
            }
        }
    }
}

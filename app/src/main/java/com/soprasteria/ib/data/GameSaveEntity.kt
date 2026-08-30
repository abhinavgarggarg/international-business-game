package com.soprasteria.ib.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * One row = one save slot. [stateJson] is the full serialized GameEngine
 * state (players, property ownership, turn index, etc.) produced by
 * GameStateSerializer. Keeping it as a single JSON blob avoids needing a
 * dozen normalized tables for a game this size, and makes "resume where I
 * left off" a single read.
 */
@Entity(tableName = "game_saves")
data class GameSaveEntity(
    @PrimaryKey val slotId: Int = 1, // single always-on autosave slot
    @ColumnInfo(name = "state_json") val stateJson: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Dao
interface GameSaveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(save: GameSaveEntity)

    @Query("SELECT * FROM game_saves WHERE slotId = 1 LIMIT 1")
    suspend fun loadLatest(): GameSaveEntity?

    @Query("DELETE FROM game_saves")
    suspend fun clear()
}

@Database(entities = [GameSaveEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameSaveDao(): GameSaveDao
}

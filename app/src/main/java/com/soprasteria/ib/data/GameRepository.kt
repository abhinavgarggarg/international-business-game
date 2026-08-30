package com.soprasteria.ib.data

import com.soprasteria.ib.engine.GameEngine

class GameRepository(private val dao: GameSaveDao) {

    suspend fun autoSave(engine: GameEngine) {
        val json = GameStateSerializer.serialize(engine)
        dao.save(GameSaveEntity(stateJson = json, updatedAt = System.currentTimeMillis()))
    }

    /** Returns true if a saved game was found and applied to [engine]. */
    suspend fun tryResume(engine: GameEngine): Boolean {
        val saved = dao.loadLatest() ?: return false
        GameStateSerializer.restoreInto(engine, saved.stateJson)
        return true
    }

    suspend fun clearSave() = dao.clear()
}

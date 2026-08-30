package com.soprasteria.ib.data

import com.soprasteria.ib.engine.GameEngine
import com.soprasteria.ib.engine.Player
import org.json.JSONArray
import org.json.JSONObject

object GameStateSerializer {

    fun serialize(engine: GameEngine): String {
        val root = JSONObject()
        root.put("currentTurnIndex", engine.currentTurnIndex)
        root.put("bankBalance", engine.bankBalance)
        root.put("gameWinner", engine.gameWinner ?: JSONObject.NULL)

        val playersJson = JSONArray()
        for (p in engine.players) {
            val pj = JSONObject()
            pj.put("id", p.id)
            pj.put("name", p.name)
            pj.put("balance", p.balance)
            pj.put("position", p.position)
            pj.put("inJail", p.inJail)
            pj.put("jailTurns", p.jailTurns)
            pj.put("isBankrupt", p.isBankrupt)
            pj.put("ownedProperties", JSONArray(p.ownedProperties))
            playersJson.put(pj)
        }
        root.put("players", playersJson)

        val propsJson = JSONArray()
        for ((id, prop) in engine.properties) {
            val prj = JSONObject()
            prj.put("id", id)
            prj.put("houses", prop.houses)
            prj.put("ownerId", prop.ownerId ?: JSONObject.NULL)
            propsJson.put(prj)
        }
        root.put("properties", propsJson)

        return root.toString()
    }

    /**
     * Applies a previously serialized snapshot onto a freshly built engine
     * (same board/property definitions, since those are deterministic and
     * don't need to be persisted). Returns the same engine instance for
     * chaining convenience.
     */
    fun restoreInto(engine: GameEngine, json: String): GameEngine {
        val root = JSONObject(json)

        val playersJson = root.getJSONArray("players")
        for (i in 0 until playersJson.length()) {
            val pj = playersJson.getJSONObject(i)
            val id = pj.getInt("id")
            val player = engine.players.firstOrNull { it.id == id } ?: continue
            player.balance = pj.getInt("balance")
            player.position = pj.getInt("position")
            player.inJail = pj.getBoolean("inJail")
            player.jailTurns = pj.getInt("jailTurns")
            player.isBankrupt = pj.getBoolean("isBankrupt")
            player.ownedProperties.clear()
            val owned = pj.getJSONArray("ownedProperties")
            for (j in 0 until owned.length()) player.ownedProperties.add(owned.getInt(j))
        }

        val propsJson = root.getJSONArray("properties")
        for (i in 0 until propsJson.length()) {
            val prj = propsJson.getJSONObject(i)
            val id = prj.getInt("id")
            val prop = engine.properties[id] ?: continue
            prop.houses = prj.getInt("houses")
            prop.ownerId = if (prj.isNull("ownerId")) null else prj.getInt("ownerId")
        }

        val winner = if (root.isNull("gameWinner")) null else root.getInt("gameWinner")
        engine.restoreMeta(
            turnIndex = root.getInt("currentTurnIndex"),
            bank = root.getInt("bankBalance"),
            winner = winner
        )

        return engine
    }
}

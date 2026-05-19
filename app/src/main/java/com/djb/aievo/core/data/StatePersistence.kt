package com.djb.aievo.core.data

import android.content.Context
import com.djb.aievo.core.models.SystemState
import org.json.JSONObject
import java.io.File

class StatePersistence(private val context: Context) {
    private val stateFile = File(context.filesDir, "system_state.json")

    fun saveState(state: SystemState) {
        val json = JSONObject().apply {
            put("convergenceFactor", state.convergenceFactor)
            put("activeNodes", state.activeNodes)
            put("credits", state.credits)
            put("timestamp", state.timestamp)
        }
        stateFile.writeText(json.toString())
    }

    fun loadState(): SystemState {
        if (!stateFile.exists()) return SystemState()
        return try {
            val json = JSONObject(stateFile.readText())
            SystemState(
                convergenceFactor = json.getDouble("convergenceFactor"),
                activeNodes = json.getInt("activeNodes"),
                credits = json.getDouble("credits"),
                timestamp = json.getLong("timestamp")
            )
        } catch (e: Exception) {
            SystemState()
        }
    }
}

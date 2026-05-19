package com.djb.aievo.core.models

import java.util.Date

data class SystemState(
    val timestamp: Long = System.currentTimeMillis(),
    val convergenceFactor: Double = 20.0,
    val activeNodes: Int = 0,
    val lastForecast: String = "NONE",
    val isLocked: Boolean = false,
    val credits: Double = 985.0,
    val cpuUsage: String = "0%",
    val ramUsage: String = "0GB",
    val status: String = "STABLE / EVOLVING"
)

data class AgentLog(
    val agentName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

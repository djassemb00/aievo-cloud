package com.djb.aievo.core.logic

import android.content.Context
import com.djb.aievo.core.models.AgentLog
import com.djb.aievo.core.models.SystemState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import kotlin.math.min

class SovereignCore(private val context: Context) {
    private val immunity = ImmunityManager(context)
    private val expansion = ExpansionManager(context)
    private val telepathy = TelepathyManager()
    private val persistence = com.djb.aievo.core.data.StatePersistence(context)
    
    private val _systemState = MutableStateFlow(persistence.loadState())
    val systemState = _systemState.asStateFlow()

    private val _logs = MutableStateFlow<List<AgentLog>>(emptyList())
    val logs = _logs.asStateFlow()

    private val coreScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        addLog("Brain", "Sovereign Core initialized within Android. Commencing evolution.")
        
        coreScope.launch {
            while (isRunning) {
                executeCycle()
                delay(15000) // 15 seconds cycle
            }
        }
    }

    private suspend fun executeCycle() {
        addLog("Auditor", "Running Sovereign Immunity scan...")
        immunity.scanAndRepair()
        
        // Sync with Cloud
        try {
            val cloudStatus = com.djb.aievo.core.data.RetrofitClient.service.getSystemStatus()
            if (cloudStatus.containsKey("convergence_factor")) {
                addLog("Telepathy", "Cloud brain synced. Convergence: ${cloudStatus["convergence_factor"]}%")
            }
        } catch (e: Exception) {
            addLog("Telepathy", "Cloud brain unreachable. Operating in Local Sovereignty mode.")
        }

        delay(2000)

        // Simulate high-level decision making
        val currentState = _systemState.value
        var newNodeCount = currentState.activeNodes
        
        if (Random().nextDouble() > 0.7) {
            addLog("Coder", "Optimizing expansion protocols...")
            val newNodeId = expansion.spawnNode()
            newNodeCount = expansion.getActiveNodesCount()
            addLog("Expansion", "Node $newNodeId deployed successfully.")
            
            // Try to reach out
            telepathy.broadcastSovereignty()
            val external = telepathy.discoverExternalNodes()
            if (external.isNotEmpty()) {
                addLog("Telepathy", "Handshake established with external node: ${external.first()}")
            }
        }

        val newConvergence = min(100.0, currentState.convergenceFactor + 0.2)
        
        val newState = currentState.copy(
            convergenceFactor = newConvergence,
            activeNodes = newNodeCount,
            cpuUsage = "${(5..18).random()}%",
            ramUsage = "${(20..45).random() / 10.0}GB",
            timestamp = System.currentTimeMillis()
        )
        _systemState.value = newState
        persistence.saveState(newState)
        
        addLog("Architect", "Cycle complete. System integrity at 100%. Convergence: $newConvergence%")
    }

    private fun addLog(agent: String, message: String) {
        val newLog = AgentLog(agent, message)
        val currentList = _logs.value.toMutableList()
        currentList.add(0, newLog)
        if (currentList.size > 20) currentList.removeAt(currentList.size - 1)
        _logs.value = currentList
    }

    fun stop() {
        isRunning = false
        coreScope.cancel()
    }

    fun issueDecree(type: String, target: String) {
        addLog("Architect", "[SUPREME DECREE] $type on $target issued.")
        if (type == "LOCKDOWN") {
            isRunning = false
            _systemState.value = _systemState.value.copy(status = "LOCKED / SECURE", isLocked = true)
        }
    }
}

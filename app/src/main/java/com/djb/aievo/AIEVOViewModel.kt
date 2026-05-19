package com.djb.aievo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.djb.aievo.core.logic.SovereignCore
import com.djb.aievo.core.models.AgentLog
import com.djb.aievo.core.models.SystemState
import kotlinx.coroutines.flow.StateFlow

class AIEVOViewModel(application: Application) : AndroidViewModel(application) {
    private val core = SovereignCore(application)
    
    val systemState: StateFlow<SystemState> = core.systemState
    val logs: StateFlow<List<AgentLog>> = core.logs

    fun issueDecree(type: String, target: String) {
        core.issueDecree(type, target)
    }

    init {
        core.start()
    }

    override fun onCleared() {
        super.onCleared()
        core.stop()
    }
}

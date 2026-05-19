package com.djb.aievo.core.logic

import android.content.Context
import android.util.Log
import java.io.File

class ExpansionManager(private val context: Context) {
    private val nodesDir = File(context.filesDir, "nodes")

    init {
        if (!nodesDir.exists()) nodesDir.mkdirs()
    }

    fun spawnNode(): String {
        val nodeId = "node_${System.currentTimeMillis()}"
        val nodeFolder = File(nodesDir, nodeId)
        nodeFolder.mkdirs()
        
        val fragmentFile = File(nodeFolder, "dna_fragment.part")
        fragmentFile.writeText("DNA_SLICE_${nodeId.hashCode()}")
        
        Log.i("Expansion", "New autonomous node spawned: $nodeId")
        return nodeId
    }

    fun getActiveNodesCount(): Int {
        return nodesDir.listFiles()?.filter { it.isDirectory }?.size ?: 0
    }
}

package com.djb.aievo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIEVODashboard(viewModel: AIEVOViewModel = viewModel()) {
    val systemState by viewModel.systemState.collectAsState()
    val logs by viewModel.logs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI EVO: Sovereign Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFF121212)) // Dark mode background
                .padding(16.dp)
        ) {
            // 1. Status Cards (CPU, RAM, Credits)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusCard(Modifier.weight(1f), "Credits", systemState.credits.toString(), Icons.Default.AccountBalanceWallet, Color(0xFFFFD700))
                StatusCard(Modifier.weight(1f), "CPU", systemState.cpuUsage, Icons.Default.Memory, Color(0xFF00E5FF))
                StatusCard(Modifier.weight(1f), "RAM", systemState.ramUsage, Icons.Default.Storage, Color(0xFF76FF03))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Active Agents Monitor
            Text("Agent Intelligence Stream", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            AgentLogList(logs)

            Spacer(modifier = Modifier.height(16.dp))

            // 3. System Sovereignty Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("System Sovereignty", color = Color.Gray, fontSize = 14.sp)
                    Text("Status: ${systemState.status}", color = Color(0xFF76FF03), fontWeight = FontWeight.Bold)
                    Text("Convergence: ${systemState.convergenceFactor}%", color = Color.White, fontSize = 12.sp)
                    LinearProgressIndicator(
                        progress = (systemState.convergenceFactor / 100).toFloat(),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        color = Color(0xFF76FF03),
                        trackColor = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Architect Actions
            Button(
                onClick = { viewModel.issueDecree("LOCKDOWN", "Global_Expansion") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                enabled = !systemState.isLocked
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("ISSUE SUPREME LOCKDOWN")
            }
        }
    }
}

@Composable
fun StatusCard(modifier: Modifier, title: String, value: String, icon: ImageVector, iconColor: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AgentLogList(logs: List<com.djb.aievo.core.models.AgentLog>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.Black, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        items(logs) { log ->
            Text(
                text = "> ${log.agentName}: ${log.message}",
                color = Color(0xFF00FF41), // Matrix Green
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

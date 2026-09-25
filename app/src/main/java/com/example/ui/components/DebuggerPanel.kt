package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DebugSession
import com.example.model.DebugState
import com.example.model.IdeThemeMode

enum class DebugTab(val label: String) {
    CONSOLE("Terminal & Logs"),
    VARIABLES("Variables Locales"),
    CALL_STACK("Pila (Call Stack)"),
    BREAKPOINTS("Puntos de Interrupción")
}

@Composable
fun DebuggerPanel(
    session: DebugSession,
    breakpoints: Set<Int>,
    themeMode: IdeThemeMode,
    onStepOver: () -> Unit,
    onContinue: () -> Unit,
    onStop: () -> Unit,
    onClose: () -> Unit,
    onApplyFix: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(themeMode.bgHex)
    val surfaceColor = Color(themeMode.surfaceHex)
    val textColor = Color(themeMode.textHex)
    val primaryColor = Color(themeMode.primaryHex)

    var currentTab by remember { mutableStateOf(DebugTab.CONSOLE) }

    Surface(
        color = surfaceColor,
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Control Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status pill
                val (statusText, statusColor) = when (session.state) {
                    DebugState.IDLE -> Pair("LISTO", Color(0xFF6B7280))
                    DebugState.RUNNING -> Pair("EJECUTANDO", Color(0xFF10B981))
                    DebugState.PAUSED_BREAKPOINT -> Pair("PAUSADO L${session.currentLine}", Color(0xFFFBBF24))
                    DebugState.STEPPING -> Pair("PASO L${session.currentLine}", Color(0xFF3B82F6))
                    DebugState.ERROR_HALTED -> Pair("ERROR L${session.currentLine}", Color(0xFFEF4444))
                    DebugState.FINISHED -> Pair("FINALIZADO", Color(0xFF10B981))
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(statusText, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Debug action controls
                IconButton(
                    onClick = onContinue,
                    enabled = session.state == DebugState.PAUSED_BREAKPOINT || session.state == DebugState.STEPPING,
                    modifier = Modifier.size(32.dp).testTag("debug_continue_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Continuar", tint = Color(0xFF10B981))
                }

                IconButton(
                    onClick = onStepOver,
                    enabled = session.state == DebugState.PAUSED_BREAKPOINT || session.state == DebugState.STEPPING,
                    modifier = Modifier.size(32.dp).testTag("debug_step_over_button")
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Paso por encima", tint = Color(0xFF3B82F6))
                }

                IconButton(
                    onClick = onStop,
                    enabled = session.state != DebugState.IDLE,
                    modifier = Modifier.size(32.dp).testTag("debug_stop_button")
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Detener", tint = Color(0xFFEF4444))
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimizar", tint = textColor)
                }
            }

            // Tab bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColor)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DebugTab.values().forEach { tab ->
                    val isSelected = tab == currentTab
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) primaryColor.copy(alpha = 0.2f) else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { currentTab = tab }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tab.label,
                            color = if (isSelected) primaryColor else textColor.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Error banner if halted
            if (session.diagnosedError != null && session.suggestedFix != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x33EF4444)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(session.diagnosedError, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Sugerencia: ${session.suggestedFix}", color = textColor.copy(alpha = 0.8f), fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onApplyFix(session.suggestedFix) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Aplicar Fix", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(bgColor)
                    .padding(8.dp)
            ) {
                when (currentTab) {
                    DebugTab.CONSOLE -> {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(session.consoleLogs) { log ->
                                Text(
                                    text = log,
                                    color = if (log.contains("❌") || log.contains("FATAL")) Color(0xFFEF4444)
                                    else if (log.contains("✅")) Color(0xFF10B981)
                                    else if (log.contains("⏸️")) Color(0xFFFBBF24)
                                    else textColor.copy(alpha = 0.85f),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                    DebugTab.VARIABLES -> {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            if (session.variables.isEmpty()) {
                                item {
                                    Text("Sin variables en el ámbito actual.", color = textColor.copy(alpha = 0.5f), fontSize = 12.sp)
                                }
                            }
                            items(session.variables) { variable ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row {
                                        Text(variable.name, color = primaryColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(" (${variable.type}): ", color = textColor.copy(alpha = 0.6f), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                        Text(variable.value, color = textColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                    }
                                    Text(variable.scope, color = textColor.copy(alpha = 0.4f), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                    DebugTab.CALL_STACK -> {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            if (session.callStack.isEmpty()) {
                                item {
                                    Text("Pila de llamadas inactiva.", color = textColor.copy(alpha = 0.5f), fontSize = 12.sp)
                                }
                            }
                            items(session.callStack) { frame ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("⚡ ${frame.functionName}", color = textColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                    Text("${frame.fileName}:${frame.lineNumber}", color = primaryColor, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    DebugTab.BREAKPOINTS -> {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            if (breakpoints.isEmpty()) {
                                item {
                                    Text("No hay puntos de interrupción activos. Toca los números de línea para añadir.", color = textColor.copy(alpha = 0.5f), fontSize = 12.sp)
                                }
                            }
                            items(breakpoints.toList().sorted()) { line ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFEF4444), CircleShape))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Breakpoint en Línea $line", color = textColor, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

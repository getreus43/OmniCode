package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.IdeThemeMode
import com.example.model.ProjectEntity
import com.example.model.ThirdPartyIntegration

@Composable
fun IntegrationsAndSecurityDialog(
    isOpen: Boolean,
    project: ProjectEntity?,
    integrations: List<ThirdPartyIntegration>,
    themeMode: IdeThemeMode,
    onToggleEncryption: () -> Unit,
    onSyncCloud: () -> Unit,
    onClose: () -> Unit
) {
    if (!isOpen) return

    val surfaceColor = Color(themeMode.surfaceHex)
    val bgColor = Color(themeMode.bgHex)
    val textColor = Color(themeMode.textHex)
    val primaryColor = Color(themeMode.primaryHex)

    val isEncrypted = project?.isEncrypted ?: false

    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Seguridad E2E e Integraciones", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Privacidad estricta & sincronización en la nube", color = textColor.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = textColor)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Encryption Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEncrypted) Color(0x2210B981) else bgColor
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isEncrypted) Icons.Default.Lock else Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (isEncrypted) Color(0xFF10B981) else textColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Cifrado Extremo a Extremo (AES-256-GCM)",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                if (isEncrypted) "Activo: El código se almacena cifrado con clave local única."
                                else "Inactivo: Código almacenado en texto plano.",
                                color = textColor.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = isEncrypted,
                            onCheckedChange = { onToggleEncryption() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981)),
                            modifier = Modifier.testTag("encryption_toggle")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sync action card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Estado de Sincronización:", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Estado: ${project?.syncStatus ?: "SYNCED"} • Modo offline listo",
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }

                    Button(
                        onClick = onSyncCloud,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("sync_cloud_button")
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sincronizar", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3rd party integrations list
                Text("Integraciones con Terceros:", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(integrations) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(if (item.isConnected) Color(0xFF10B981) else Color(0xFF6B7280), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(item.description, color = textColor.copy(alpha = 0.6f), fontSize = 10.sp)
                                    Text("Último enlace: ${item.lastSyncTime}", color = primaryColor, fontSize = 9.sp)
                                }
                                Text(
                                    if (item.isConnected) "Conectado" else "Desconectado",
                                    color = if (item.isConnected) Color(0xFF10B981) else textColor.copy(alpha = 0.5f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

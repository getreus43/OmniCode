package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.window.Dialog
import com.example.model.DiffType
import com.example.model.GitCommitEntity
import com.example.model.GitDiff
import com.example.model.IdeThemeMode
import com.example.vcs.VcsEngine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GitVcsDialog(
    isOpen: Boolean,
    commits: List<GitCommitEntity>,
    activeBranch: String,
    activeDiff: GitDiff?,
    themeMode: IdeThemeMode,
    onCommit: (String) -> Unit,
    onSwitchBranch: (String) -> Unit,
    onInspectDiff: (GitCommitEntity) -> Unit,
    onCloseDiff: () -> Unit,
    onClose: () -> Unit
) {
    if (!isOpen) return

    val surfaceColor = Color(themeMode.surfaceHex)
    val bgColor = Color(themeMode.bgHex)
    val textColor = Color(themeMode.textHex)
    val primaryColor = Color(themeMode.primaryHex)

    var commitMsg by remember { mutableStateOf("") }

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
                    Icon(Icons.Default.Commit, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Control de Versiones (Git Local)", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Rama activa: $activeBranch", color = textColor.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = textColor)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Branches Selector
                Text("Ramas Disponibles:", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VcsEngine.defaultBranches.forEach { branch ->
                        val isSelected = branch == activeBranch
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) primaryColor else surfaceColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, if (isSelected) primaryColor else textColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .clickable { onSwitchBranch(branch) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.AutoMirrored.Filled.AltRoute,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else textColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = branch,
                                    color = if (isSelected) Color.White else textColor,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Commit Input Form
                Text("Crear Nuevo Commit:", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commitMsg,
                        onValueChange = { commitMsg = it },
                        placeholder = { Text("feat: descripción del cambio...", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (commitMsg.isNotBlank()) {
                                onCommit(commitMsg)
                                commitMsg = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("commit_button")
                    ) {
                        Text("Commit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Commit History
                Text("Historial de Commits:", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(commits) { commit ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onInspectDiff(commit) }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(primaryColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(commit.message, color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    val dateStr = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()).format(Date(commit.timestamp))
                                    Text("${commit.hash} • ${commit.author} • $dateStr", color = textColor.copy(alpha = 0.5f), fontSize = 10.sp)
                                }
                                Icon(Icons.Default.Difference, contentDescription = "Ver Diff", tint = primaryColor, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Visual Diff Dialog
    if (activeDiff != null) {
        Dialog(onDismissRequest = onCloseDiff) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = surfaceColor,
                tonalElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Difference, contentDescription = null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Inspector de Diferencias (Diff)", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = onCloseDiff, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = textColor)
                        }
                    }

                    Text(activeDiff.summary, color = textColor.copy(alpha = 0.7f), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(bgColor, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        items(activeDiff.diffLines) { line ->
                            val lineBg = when (line.type) {
                                DiffType.ADDED -> Color(0x3310B981)
                                DiffType.REMOVED -> Color(0x33EF4444)
                                DiffType.UNCHANGED -> Color.Transparent
                            }
                            val prefix = when (line.type) {
                                DiffType.ADDED -> "+ "
                                DiffType.REMOVED -> "- "
                                DiffType.UNCHANGED -> "  "
                            }
                            val lineFg = when (line.type) {
                                DiffType.ADDED -> Color(0xFF10B981)
                                DiffType.REMOVED -> Color(0xFFEF4444)
                                DiffType.UNCHANGED -> textColor.copy(alpha = 0.7f)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(lineBg)
                                    .padding(vertical = 1.dp)
                            ) {
                                Text(
                                    text = "${line.lineNumber} $prefix${line.text}",
                                    color = lineFg,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

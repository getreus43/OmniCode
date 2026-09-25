package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Collaborator
import com.example.model.IdeThemeMode
import com.example.model.ProgrammingLanguage

@Composable
fun CodeEditorView(
    code: String,
    onCodeChange: (String) -> Unit,
    language: ProgrammingLanguage,
    breakpoints: Set<Int>,
    currentDebugLine: Int,
    collaborators: List<Collaborator>,
    themeMode: IdeThemeMode,
    fontSizeSp: Float,
    isSearchOpen: Boolean,
    searchQuery: String,
    replaceQuery: String,
    onSearchChange: (String) -> Unit,
    onReplaceChange: (String) -> Unit,
    onExecuteReplace: () -> Unit,
    onCloseSearch: () -> Unit,
    onToggleBreakpoint: (Int) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lines = remember(code) { code.lines() }
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    val bgColor = Color(themeMode.bgHex)
    val surfaceColor = Color(themeMode.surfaceHex)
    val primaryColor = Color(themeMode.primaryHex)
    val textColor = Color(themeMode.textHex)

    val quickSymbols = listOf("(", ")", "{", "}", "[", "]", "\"", "'", ":", ";", "=", "+", "-", "*", "/", "<", ">", "_")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Quick Symbols & Action Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceColor)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onUndo,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("editor_undo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Deshacer",
                    tint = textColor.copy(alpha = 0.8f)
                )
            }
            IconButton(
                onClick = onRedo,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("editor_redo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Rehacer",
                    tint = textColor.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Quick symbols row
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                quickSymbols.forEach { sym ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .background(bgColor.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                            .clickable { onCodeChange(code + sym) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sym,
                            color = primaryColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Search & Replace Bar
        if (isSearchOpen) {
            Surface(
                color = surfaceColor,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            placeholder = { Text("Buscar...", fontSize = 12.sp) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, Modifier.size(16.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = onCloseSearch, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar búsqueda", tint = textColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replaceQuery,
                            onValueChange = onReplaceChange,
                            placeholder = { Text("Reemplazar con...", fontSize = 12.sp) },
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
                            onClick = onExecuteReplace,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(Icons.Default.FindReplace, contentDescription = null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reemplazar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Main Editor Canvas (Gutter + Text Field)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScroll)
        ) {
            // Line numbers gutter
            Column(
                modifier = Modifier
                    .width(44.dp)
                    .background(surfaceColor.copy(alpha = 0.5f))
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                lines.indices.forEach { index ->
                    val lineNum = index + 1
                    val hasBreakpoint = breakpoints.contains(lineNum)
                    val isCurrentStep = currentDebugLine == lineNum

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clickable { onToggleBreakpoint(lineNum) }
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (hasBreakpoint) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFFEF4444), CircleShape)
                            )
                        }
                        if (isCurrentStep) {
                            Text("▶", color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "$lineNum",
                            color = if (isCurrentStep) Color(0xFFFBBF24) else textColor.copy(alpha = 0.4f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = (fontSizeSp - 2).sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Code Editor Area with syntax highlighting
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScroll)
                    .padding(8.dp)
            ) {
                // Background visual highlights for active debug line
                if (currentDebugLine > 0 && currentDebugLine <= lines.size) {
                    val topOffset = ((currentDebugLine - 1) * 24).dp
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .padding(top = topOffset)
                            .background(Color(0x33FBBF24), RoundedCornerShape(2.dp))
                    )
                }

                // Collaborative line tags
                collaborators.forEach { collab ->
                    if (collab.cursorLine > 0 && collab.cursorLine <= lines.size) {
                        val topOffset = ((collab.cursorLine - 1) * 24).dp
                        Row(
                            modifier = Modifier
                                .padding(top = topOffset)
                                .background(Color(collab.avatarColor).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "👤 ${collab.name}",
                                color = Color(collab.avatarColor),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // The interactive editable text
                BasicTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = fontSizeSp.sp,
                        color = textColor,
                        lineHeight = 24.sp
                    ),
                    cursorBrush = SolidColor(primaryColor),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("code_editor_text_field")
                )
            }
        }
    }
}


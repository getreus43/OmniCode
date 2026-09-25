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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.IdeThemeMode
import com.example.model.ProgrammingLanguage
import com.example.model.ProjectEntity
import com.example.model.SourceFileEntity

@Composable
fun ProjectDrawerContent(
    currentProject: ProjectEntity?,
    projects: List<ProjectEntity>,
    files: List<SourceFileEntity>,
    activeFile: SourceFileEntity?,
    themeMode: IdeThemeMode,
    onSelectProject: (String) -> Unit,
    onSelectFile: (SourceFileEntity) -> Unit,
    onCreateProject: (name: String, language: ProgrammingLanguage, isEncrypted: Boolean) -> Unit,
    onCreateFile: (fileName: String) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(themeMode.bgHex)
    val surfaceColor = Color(themeMode.surfaceHex)
    val textColor = Color(themeMode.textHex)
    val primaryColor = Color(themeMode.primaryHex)

    var showNewProjectModal by remember { mutableStateOf(false) }
    var showNewFileModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(surfaceColor)
            .padding(16.dp)
    ) {
        // Drawer Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Explorador de Proyectos", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onCloseDrawer, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar menú", tint = textColor)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Projects list header & New Project button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("PROYECTOS", color = textColor.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showNewProjectModal = true }, modifier = Modifier.size(24.dp).testTag("new_project_button")) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Proyecto", tint = primaryColor, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Project Cards
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(projects) { proj ->
                val isSelected = proj.id == currentProject?.id
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) primaryColor.copy(alpha = 0.2f) else bgColor, RoundedCornerShape(6.dp))
                        .border(1.dp, if (isSelected) primaryColor else Color.Transparent, RoundedCornerShape(6.dp))
                        .clickable { onSelectProject(proj.id) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = proj.name,
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (proj.isEncrypted) {
                            Icon(Icons.Default.Lock, contentDescription = "Cifrado", tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Box(
                            modifier = Modifier
                                .background(primaryColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(proj.primaryLanguage, color = primaryColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Files Tree for Active Project
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ARCHIVOS DEL PROYECTO", color = textColor.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showNewFileModal = true }, modifier = Modifier.size(24.dp).testTag("new_file_button")) {
                Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = "Nuevo Archivo", tint = primaryColor, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(files) { file ->
                val isSelected = file.id == activeFile?.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) primaryColor.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(6.dp))
                        .clickable { onSelectFile(file) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                        contentDescription = null,
                        tint = if (isSelected) primaryColor else textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = file.name,
                        color = if (isSelected) primaryColor else textColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (file.isDirty) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFFFBBF24), CircleShape))
                    }
                }
            }
        }
    }

    // Modal: Nuevo Proyecto
    if (showNewProjectModal) {
        var projName by remember { mutableStateOf("") }
        var selectedLang by remember { mutableStateOf(ProgrammingLanguage.PYTHON) }
        var isEncrypted by remember { mutableStateOf(true) }

        Dialog(onDismissRequest = { showNewProjectModal = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = surfaceColor,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nuevo Proyecto Multi-Lenguaje", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = projName,
                        onValueChange = { projName = it },
                        placeholder = { Text("Nombre del proyecto...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Lenguaje Principal:", color = textColor, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ProgrammingLanguage.values().forEach { lang ->
                            val isSel = lang == selectedLang
                            Box(
                                modifier = Modifier
                                    .background(if (isSel) primaryColor else bgColor, RoundedCornerShape(6.dp))
                                    .clickable { selectedLang = lang }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(lang.displayName, color = if (isSel) Color.White else textColor, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isEncrypted,
                            onCheckedChange = { isEncrypted = it },
                            colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cifrado de extremo a extremo (E2E)", color = textColor, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (projName.isNotBlank()) {
                                onCreateProject(projName, selectedLang, isEncrypted)
                                showNewProjectModal = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Crear Proyecto")
                    }
                }
            }
        }
    }

    // Modal: Nuevo Archivo
    if (showNewFileModal) {
        var fileName by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showNewFileModal = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = surfaceColor,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nuevo Archivo de Código", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        placeholder = { Text("ej. service.py, Engine.kt, util.rs") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (fileName.isNotBlank()) {
                                onCreateFile(fileName)
                                showNewFileModal = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Crear Archivo")
                    }
                }
            }
        }
    }
}

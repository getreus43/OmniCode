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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.model.IdeThemeMode
import com.example.model.ProgrammingLanguage

@Composable
fun AiAssistantDialog(
    isOpen: Boolean,
    isLoading: Boolean,
    response: String,
    language: ProgrammingLanguage,
    themeMode: IdeThemeMode,
    onExplainCode: () -> Unit,
    onClose: () -> Unit
) {
    if (!isOpen) return

    val surfaceColor = Color(themeMode.surfaceHex)
    val bgColor = Color(themeMode.bgHex)
    val textColor = Color(themeMode.textHex)
    val primaryColor = Color(themeMode.primaryHex)

    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini AI Code Assistant", color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = textColor)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onExplainCode,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("ai_explain_button")
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Explicar Lógica", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Response Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 320.dp)
                        .background(bgColor, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = if (isLoading) Alignment.Center else Alignment.TopStart
                ) {
                    if (isLoading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Consultando Gemini AI / Motor AST...", color = textColor.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    } else if (response.isBlank()) {
                        Text(
                            "Presiona 'Explicar Lógica' para que Gemini AI analice tu código en ${language.displayName}, examine patrones de diseño y proponga optimizaciones.",
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    } else {
                        Text(
                            text = response,
                            color = textColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TranslatorDialog(
    isOpen: Boolean,
    isLoading: Boolean,
    currentLanguage: ProgrammingLanguage,
    targetLanguage: ProgrammingLanguage,
    translatedCode: String,
    unitTests: String,
    themeMode: IdeThemeMode,
    onSelectTargetLanguage: (ProgrammingLanguage) -> Unit,
    onTranslate: (ProgrammingLanguage) -> Unit,
    onApplyAsNewFile: () -> Unit,
    onClose: () -> Unit
) {
    if (!isOpen) return

    val surfaceColor = Color(themeMode.surfaceHex)
    val bgColor = Color(themeMode.bgHex)
    val textColor = Color(themeMode.textHex)
    val primaryColor = Color(themeMode.primaryHex)

    var activeTab by remember { mutableStateOf(0) } // 0: Código traducido, 1: Tests unitarios

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
                    Icon(Icons.Default.Translate, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Traductor Universal de Proyectos", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Migración de contexto + Tests automáticos", color = textColor.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = textColor)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target Language selector
                Text("Lenguaje Destino:", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ProgrammingLanguage.values().filter { it != currentLanguage }.forEach { lang ->
                        val isSelected = lang == targetLanguage
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) primaryColor else surfaceColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, if (isSelected) primaryColor else textColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .clickable {
                                    onSelectTargetLanguage(lang)
                                    onTranslate(lang)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = lang.displayName,
                                color = if (isSelected) Color.White else textColor,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs: Translated Code vs Tests
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (activeTab == 0) primaryColor.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { activeTab = 0 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Código ${targetLanguage.displayName}", color = if (activeTab == 0) primaryColor else textColor.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (activeTab == 1) primaryColor.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { activeTab = 1 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tests (${targetLanguage.testFramework})", color = if (activeTab == 1) primaryColor else textColor.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Content Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(bgColor, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = if (isLoading) Alignment.Center else Alignment.TopStart
                ) {
                    if (isLoading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Generando sintaxis y pruebas...", color = textColor.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    } else {
                        val textToShow = if (activeTab == 0) translatedCode else unitTests
                        Text(
                            text = textToShow.ifEmpty { "Presiona un lenguaje arriba para iniciar la traducción." },
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onApplyAsNewFile,
                    enabled = translatedCode.isNotBlank() && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("apply_translation_button")
                ) {
                    Icon(Icons.Default.DataObject, contentDescription = null, Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Crear Copia en Proyecto con Tests", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

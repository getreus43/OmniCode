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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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

@Composable
fun SettingsAndAccessibilityDialog(
    isOpen: Boolean,
    currentTheme: IdeThemeMode,
    fontSizeSp: Float,
    isHighAccessibility: Boolean,
    onSelectTheme: (IdeThemeMode) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onToggleHighAccessibility: () -> Unit,
    onClose: () -> Unit
) {
    if (!isOpen) return

    val surfaceColor = Color(currentTheme.surfaceHex)
    val bgColor = Color(currentTheme.bgHex)
    val textColor = Color(currentTheme.textHex)
    val primaryColor = Color(currentTheme.primaryHex)

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
                    Icon(Icons.Default.Settings, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configuración y Accesibilidad", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = textColor)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Themes
                Text("Tema Visual del IDE:", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IdeThemeMode.values().forEach { theme ->
                        val isSelected = theme == currentTheme
                        Box(
                            modifier = Modifier
                                .background(Color(theme.bgHex), RoundedCornerShape(8.dp))
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) primaryColor else Color(theme.surfaceHex),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectTheme(theme) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = theme.title,
                                    color = Color(theme.textHex),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(theme.primaryHex), RoundedCornerShape(2.dp)))
                                    Box(modifier = Modifier.size(8.dp).background(Color(theme.accentHex), RoundedCornerShape(2.dp)))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Font Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tamaño de Fuente del Editor:", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${fontSizeSp.toInt()} sp", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Slider(
                    value = fontSizeSp,
                    onValueChange = onFontSizeChange,
                    valueRange = 12f..24f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = primaryColor,
                        activeTrackColor = primaryColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("font_size_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // High Accessibility Mode Toggle
                Card(
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Accessibility, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Modo de Alta Accesibilidad", color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                "Maximiza el contraste, bordes gruesos y tamaños de interacción para TalkBack y baja visión.",
                                color = textColor.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = isHighAccessibility,
                            onCheckedChange = { onToggleHighAccessibility() },
                            colors = SwitchDefaults.colors(checkedThumbColor = primaryColor),
                            modifier = Modifier.testTag("high_accessibility_toggle")
                        )
                    }
                }
            }
        }
    }
}

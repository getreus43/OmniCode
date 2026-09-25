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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
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
import com.example.model.BlockCategory
import com.example.model.BlockNode
import com.example.model.BlockType
import com.example.model.IdeThemeMode
import com.example.model.ProgrammingLanguage

@Composable
fun VisualBlockCanvas(
    blocks: List<BlockNode>,
    onAddBlock: (BlockType) -> Unit,
    onRemoveBlock: (String) -> Unit,
    onUpdateParams: (id: String, p1: String, p2: String, p3: String) -> Unit,
    language: ProgrammingLanguage,
    themeMode: IdeThemeMode,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(themeMode.bgHex)
    val surfaceColor = Color(themeMode.surfaceHex)
    val textColor = Color(themeMode.textHex)
    val primaryColor = Color(themeMode.primaryHex)

    var selectedCategory by remember { mutableStateOf(BlockCategory.VARIABLE) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Block Category Palette Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceColor)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BlockCategory.values().forEach { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) Color(cat.colorHex) else Color(cat.colorHex).copy(alpha = 0.15f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat.label,
                        color = if (isSelected) Color.White else Color(cat.colorHex),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Available Blocks in category to add
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceColor.copy(alpha = 0.5f))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val available = BlockType.values().filter { it.category == selectedCategory }
            available.forEach { type ->
                Button(
                    onClick = { onAddBlock(type) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(type.category.colorHex)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(34.dp).testTag("add_block_${type.name}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(type.defaultTitle, fontSize = 11.sp, color = Color.White)
                }
            }
        }

        // Block Sequence Canvas
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (blocks.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🧩 Lienzo de Bloques Vacío", color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Selecciona una categoría arriba y presiona un bloque para comenzar a construir lógica visual.",
                                color = textColor.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            itemsIndexed(blocks, key = { _, block -> block.id }) { index, block ->
                BlockItemCard(
                    block = block,
                    index = index,
                    language = language,
                    themeMode = themeMode,
                    onRemove = { onRemoveBlock(block.id) },
                    onUpdate = { p1, p2, p3 -> onUpdateParams(block.id, p1, p2, p3) }
                )
            }
        }
    }
}

@Composable
private fun BlockItemCard(
    block: BlockNode,
    index: Int,
    language: ProgrammingLanguage,
    themeMode: IdeThemeMode,
    onRemove: () -> Unit,
    onUpdate: (String, String, String) -> Unit
) {
    val blockColor = Color(block.type.category.colorHex)
    val surfaceColor = Color(themeMode.surfaceHex)
    val textColor = Color(themeMode.textHex)

    var p1 by remember(block.param1) { mutableStateOf(block.param1) }
    var p2 by remember(block.param2) { mutableStateOf(block.param2) }
    var p3 by remember(block.param3) { mutableStateOf(block.param3) }

    Card(
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, blockColor.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(blockColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${index + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = block.type.defaultTitle,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar bloque",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Parameter Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = p1,
                    onValueChange = {
                        p1 = it
                        onUpdate(it, p2, p3)
                    },
                    label = { Text(block.type.defaultParam1.ifEmpty { "Parámetro 1" }, fontSize = 10.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (block.type.defaultParam2.isNotEmpty()) {
                    OutlinedTextField(
                        value = p2,
                        onValueChange = {
                            p2 = it
                            onUpdate(p1, it, p3)
                        },
                        label = { Text(block.type.defaultParam2, fontSize = 10.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Real-time Code Translation Snippet Preview
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = Color(themeMode.bgHex),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚙️ ${block.toCode(language)}",
                    color = blockColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

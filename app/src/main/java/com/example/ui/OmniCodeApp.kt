package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DebugState
import com.example.ui.components.AiAssistantDialog
import com.example.ui.components.CodeEditorView
import com.example.ui.components.DebuggerPanel
import com.example.ui.components.GitVcsDialog
import com.example.ui.components.IntegrationsAndSecurityDialog
import com.example.ui.components.ProjectDrawerContent
import com.example.ui.components.SettingsAndAccessibilityDialog
import com.example.ui.components.TranslatorDialog
import com.example.ui.components.VisualBlockCanvas
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniCodeApp(viewModel: OmniCodeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val debugSession by viewModel.debugSession.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val theme = uiState.themeMode
    val bgColor = Color(theme.bgHex)
    val surfaceColor = Color(theme.surfaceHex)
    val textColor = Color(theme.textHex)
    val primaryColor = Color(theme.primaryHex)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = surfaceColor) {
                ProjectDrawerContent(
                    currentProject = uiState.currentProject,
                    projects = uiState.allProjects,
                    files = uiState.files,
                    activeFile = uiState.activeFile,
                    themeMode = theme,
                    onSelectProject = {
                        viewModel.selectProject(it)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onSelectFile = {
                        viewModel.selectFile(it)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onCreateProject = { name, lang, enc ->
                        viewModel.createNewProject(name, lang, enc)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onCreateFile = { name ->
                        viewModel.createNewFileInCurrentProject(name)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(bgColor)
            ) {
                // Top App Bar
                Surface(
                    color = surfaceColor,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { coroutineScope.launch { drawerState.open() } },
                                modifier = Modifier.size(36.dp).testTag("drawer_menu_button")
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú de Proyectos", tint = textColor)
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Project Name and Language Badge
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = uiState.currentProject?.name ?: "OmniCode Studio",
                                        color = textColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    if (uiState.currentProject?.isEncrypted == true) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Lock, contentDescription = "Cifrado", tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = uiState.activeFile?.name ?: "main",
                                        color = primaryColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // Branch indicator button
                                    Row(
                                        modifier = Modifier
                                            .background(bgColor, RoundedCornerShape(4.dp))
                                            .clickable { viewModel.setGitSheetOpen(true) }
                                            .padding(horizontal = 4.dp, vertical = 1.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.AltRoute, contentDescription = null, tint = textColor.copy(alpha = 0.6f), modifier = Modifier.size(9.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(uiState.activeBranch, color = textColor.copy(alpha = 0.6f), fontSize = 9.sp)
                                    }
                                }
                            }

                            // Top Actions (Run, Debug, AI, Translate, Security, Settings)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                // Run button
                                IconButton(
                                    onClick = { viewModel.runOrDebugCode(simulateError = false) },
                                    modifier = Modifier.size(32.dp).testTag("run_code_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Ejecutar", tint = Color(0xFF10B981))
                                }

                                // Debug button
                                IconButton(
                                    onClick = { viewModel.runOrDebugCode(simulateError = false) },
                                    modifier = Modifier.size(32.dp).testTag("debug_code_button")
                                ) {
                                    Icon(Icons.Default.BugReport, contentDescription = "Depurar", tint = Color(0xFFFBBF24))
                                }

                                // AI Assistant
                                IconButton(
                                    onClick = { viewModel.requestAiExplain() },
                                    modifier = Modifier.size(32.dp).testTag("ai_assistant_button")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "Asistente Gemini AI", tint = primaryColor)
                                }

                                // Language Translator
                                IconButton(
                                    onClick = { viewModel.setTranslateOpen(true) },
                                    modifier = Modifier.size(32.dp).testTag("translator_button")
                                ) {
                                    Icon(Icons.Default.Translate, contentDescription = "Traductor Multi-Lenguaje", tint = Color(0xFFEC4899))
                                }

                                // Security & Integrations
                                IconButton(
                                    onClick = { viewModel.setIntegrationsOpen(true) },
                                    modifier = Modifier.size(32.dp).testTag("integrations_button")
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = "Seguridad E2E", tint = Color(0xFF06B6D4))
                                }

                                // Settings & Accessibility
                                IconButton(
                                    onClick = { viewModel.setSettingsOpen(true) },
                                    modifier = Modifier.size(32.dp).testTag("settings_button")
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = textColor)
                                }
                            }
                        }

                        // Mode Selector: [ Código | Bloques | Híbrido Split ]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgColor)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Modo:", color = textColor.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)

                            val modes = listOf(
                                Pair(EditorMode.HYBRID, "Híbrido (Split)"),
                                Pair(EditorMode.CODE, "Solo Código"),
                                Pair(EditorMode.BLOCKS, "Solo Bloques")
                            )

                            modes.forEach { (mode, label) ->
                                val isSelected = uiState.editorMode == mode
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) primaryColor else surfaceColor,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { viewModel.setEditorMode(mode) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else textColor,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Search toggle icon
                            IconButton(
                                onClick = { viewModel.setSearchOpen(!uiState.isSearchOpen) },
                                modifier = Modifier.size(28.dp).testTag("toggle_search_button")
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = textColor.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Workspace Main Area (Code / Blocks / Hybrid)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (uiState.editorMode) {
                        EditorMode.CODE -> {
                            CodeEditorView(
                                code = uiState.activeCode,
                                onCodeChange = { viewModel.updateCode(it) },
                                language = uiState.activeLanguage,
                                breakpoints = uiState.breakpoints,
                                currentDebugLine = debugSession.currentLine,
                                collaborators = uiState.collaborators,
                                themeMode = theme,
                                fontSizeSp = uiState.fontSizeSp,
                                isSearchOpen = uiState.isSearchOpen,
                                searchQuery = uiState.searchQuery,
                                replaceQuery = uiState.replaceQuery,
                                onSearchChange = { viewModel.updateSearchQuery(it) },
                                onReplaceChange = { viewModel.updateReplaceQuery(it) },
                                onExecuteReplace = { viewModel.executeReplaceAll() },
                                onCloseSearch = { viewModel.setSearchOpen(false) },
                                onToggleBreakpoint = { viewModel.toggleBreakpoint(it) },
                                onUndo = { viewModel.undo() },
                                onRedo = { viewModel.redo() }
                            )
                        }
                        EditorMode.BLOCKS -> {
                            VisualBlockCanvas(
                                blocks = uiState.activeBlocks,
                                onAddBlock = { viewModel.addBlock(it) },
                                onRemoveBlock = { viewModel.removeBlock(it) },
                                onUpdateParams = { id, p1, p2, p3 -> viewModel.updateBlockParams(id, p1, p2, p3) },
                                language = uiState.activeLanguage,
                                themeMode = theme
                            )
                        }
                        EditorMode.HYBRID -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Upper half: Visual Block Canvas
                                VisualBlockCanvas(
                                    blocks = uiState.activeBlocks,
                                    onAddBlock = { viewModel.addBlock(it) },
                                    onRemoveBlock = { viewModel.removeBlock(it) },
                                    onUpdateParams = { id, p1, p2, p3 -> viewModel.updateBlockParams(id, p1, p2, p3) },
                                    language = uiState.activeLanguage,
                                    themeMode = theme,
                                    modifier = Modifier.weight(0.48f)
                                )

                                Spacer(modifier = Modifier.height(2.dp).background(primaryColor.copy(alpha = 0.3f)).fillMaxWidth())

                                // Lower half: Code Editor View
                                CodeEditorView(
                                    code = uiState.activeCode,
                                    onCodeChange = { viewModel.updateCode(it) },
                                    language = uiState.activeLanguage,
                                    breakpoints = uiState.breakpoints,
                                    currentDebugLine = debugSession.currentLine,
                                    collaborators = uiState.collaborators,
                                    themeMode = theme,
                                    fontSizeSp = uiState.fontSizeSp,
                                    isSearchOpen = uiState.isSearchOpen,
                                    searchQuery = uiState.searchQuery,
                                    replaceQuery = uiState.replaceQuery,
                                    onSearchChange = { viewModel.updateSearchQuery(it) },
                                    onReplaceChange = { viewModel.updateReplaceQuery(it) },
                                    onExecuteReplace = { viewModel.executeReplaceAll() },
                                    onCloseSearch = { viewModel.setSearchOpen(false) },
                                    onToggleBreakpoint = { viewModel.toggleBreakpoint(it) },
                                    onUndo = { viewModel.undo() },
                                    onRedo = { viewModel.redo() },
                                    modifier = Modifier.weight(0.52f)
                                )
                            }
                        }
                    }
                }

                // Debugger & Terminal Panel (Collapsible)
                if (uiState.isTerminalVisible) {
                    DebuggerPanel(
                        session = debugSession,
                        breakpoints = uiState.breakpoints,
                        themeMode = theme,
                        onStepOver = { viewModel.stepOverDebugger() },
                        onContinue = { viewModel.continueDebugger() },
                        onStop = { viewModel.stopDebugger() },
                        onClose = { viewModel.toggleTerminal() },
                        onApplyFix = { fix ->
                            viewModel.updateCode(uiState.activeCode + "\n\n# Fix automático aplicado:\n$fix")
                        }
                    )
                }

                // Bottom Status Bar
                Surface(
                    color = surfaceColor,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Terminal toggle icon
                        IconButton(
                            onClick = { viewModel.toggleTerminal() },
                            modifier = Modifier.size(24.dp).testTag("bottom_terminal_toggle")
                        ) {
                            Icon(
                                Icons.Default.Terminal,
                                contentDescription = "Terminal",
                                tint = if (uiState.isTerminalVisible) primaryColor else textColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Active collaborators presence avatars
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy((-4).dp)
                        ) {
                            uiState.collaborators.forEach { collab ->
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(Color(collab.avatarColor), CircleShape)
                                        .border(1.dp, surfaceColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(collab.name.take(1), color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                        Text("2 Colaboradores", color = textColor.copy(alpha = 0.5f), fontSize = 10.sp)

                        Spacer(modifier = Modifier.weight(1f))

                        // Language badge
                        Box(
                            modifier = Modifier
                                .background(Color(uiState.activeLanguage.badgeColorHex), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(uiState.activeLanguage.displayName, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Sync Status
                        Row(
                            modifier = Modifier.clickable { viewModel.syncWithCloud() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("E2E Sync", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    AiAssistantDialog(
        isOpen = uiState.isAiSheetOpen,
        isLoading = uiState.aiLoading,
        response = uiState.aiResponseText,
        language = uiState.activeLanguage,
        themeMode = theme,
        onExplainCode = { viewModel.requestAiExplain() },
        onClose = { viewModel.setAiSheetOpen(false) }
    )

    TranslatorDialog(
        isOpen = uiState.isTranslateOpen,
        isLoading = uiState.aiLoading,
        currentLanguage = uiState.activeLanguage,
        targetLanguage = uiState.translationTargetLanguage,
        translatedCode = uiState.aiResponseText,
        unitTests = uiState.aiGeneratedTests,
        themeMode = theme,
        onSelectTargetLanguage = { viewModel.translateProject(it) },
        onTranslate = { viewModel.translateProject(it) },
        onApplyAsNewFile = { viewModel.applyTranslationAsNewFile() },
        onClose = { viewModel.setTranslateOpen(false) }
    )

    GitVcsDialog(
        isOpen = uiState.isGitSheetOpen,
        commits = uiState.gitCommits,
        activeBranch = uiState.activeBranch,
        activeDiff = uiState.activeGitDiff,
        themeMode = theme,
        onCommit = { viewModel.commitChanges(it) },
        onSwitchBranch = { viewModel.switchBranch(it) },
        onInspectDiff = { viewModel.showDiffForCommit(it) },
        onCloseDiff = { viewModel.clearActiveGitDiff() },
        onClose = { viewModel.setGitSheetOpen(false) }
    )

    IntegrationsAndSecurityDialog(
        isOpen = uiState.isIntegrationsOpen,
        project = uiState.currentProject,
        integrations = uiState.integrations,
        themeMode = theme,
        onToggleEncryption = { viewModel.toggleProjectEncryption() },
        onSyncCloud = { viewModel.syncWithCloud() },
        onClose = { viewModel.setIntegrationsOpen(false) }
    )

    SettingsAndAccessibilityDialog(
        isOpen = uiState.isSettingsOpen,
        currentTheme = theme,
        fontSizeSp = uiState.fontSizeSp,
        isHighAccessibility = uiState.isHighAccessibility,
        onSelectTheme = { viewModel.setTheme(it) },
        onFontSizeChange = { viewModel.setFontSize(it) },
        onToggleHighAccessibility = { viewModel.toggleHighAccessibility() },
        onClose = { viewModel.setSettingsOpen(false) }
    )
}

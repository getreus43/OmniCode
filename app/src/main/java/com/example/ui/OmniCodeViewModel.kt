package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiCodeService
import com.example.ai.LocalAstEngine
import com.example.debugger.InteractiveDebugger
import com.example.model.BlockNode
import com.example.model.BlockType
import com.example.model.Collaborator
import com.example.model.GitCommitEntity
import com.example.model.GitDiff
import com.example.model.IdeThemeMode
import com.example.model.ProgrammingLanguage
import com.example.model.ProjectEntity
import com.example.model.SourceFileEntity
import com.example.model.ThirdPartyIntegration
import com.example.repository.OmniCodeRepository
import com.example.vcs.VcsEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

enum class EditorMode {
    CODE,
    BLOCKS,
    HYBRID
}

data class UiState(
    val currentProject: ProjectEntity? = null,
    val allProjects: List<ProjectEntity> = emptyList(),
    val files: List<SourceFileEntity> = emptyList(),
    val activeFile: SourceFileEntity? = null,
    val activeCode: String = "",
    val activeBlocks: List<BlockNode> = emptyList(),
    val activeLanguage: ProgrammingLanguage = ProgrammingLanguage.PYTHON,
    val editorMode: EditorMode = EditorMode.HYBRID,
    val breakpoints: Set<Int> = emptySet(),
    val themeMode: IdeThemeMode = IdeThemeMode.CYBERPUNK,
    val fontSizeSp: Float = 14f,
    val isHighAccessibility: Boolean = false,
    val isTerminalVisible: Boolean = false,
    val isDrawerOpen: Boolean = false,
    val isGitSheetOpen: Boolean = false,
    val isAiSheetOpen: Boolean = false,
    val isIntegrationsOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isTranslateOpen: Boolean = false,
    val isSearchOpen: Boolean = false,
    val searchQuery: String = "",
    val replaceQuery: String = "",
    val aiLoading: Boolean = false,
    val aiResponseText: String = "",
    val aiGeneratedTests: String = "",
    val translationTargetLanguage: ProgrammingLanguage = ProgrammingLanguage.KOTLIN,
    val gitCommits: List<GitCommitEntity> = emptyList(),
    val activeGitDiff: GitDiff? = null,
    val activeBranch: String = "main",
    val collaborators: List<Collaborator> = emptyList(),
    val integrations: List<ThirdPartyIntegration> = emptyList(),
    val statusMessage: String? = null
)

class OmniCodeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OmniCodeRepository(application)
    private val geminiService = GeminiCodeService()
    private val debugger = InteractiveDebugger()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val debugSession = debugger.session

    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()

    init {
        setupIntegrationsList()
        setupCollaborators()
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
        }
        viewModelScope.launch {
            repository.allProjects.collectLatest { projects ->
                _uiState.value = _uiState.value.copy(allProjects = projects)
                if (_uiState.value.currentProject == null && projects.isNotEmpty()) {
                    selectProject(projects.first().id)
                }
            }
        }
    }

    private fun setupIntegrationsList() {
        val defaultIntegrations = listOf(
            ThirdPartyIntegration(
                id = "github",
                name = "GitHub",
                description = "Sincronización remota, Pull Requests y GitHub Actions.",
                isConnected = true,
                lastSyncTime = "Hace 5 minutos",
                accountInfo = "github.com/developer",
                iconName = "github"
            ),
            ThirdPartyIntegration(
                id = "gdrive",
                name = "Google Drive",
                description = "Respaldo automático de proyectos y exportación en la nube.",
                isConnected = true,
                lastSyncTime = "Hace 1 hora",
                accountInfo = "drive.google.com",
                iconName = "gdrive"
            ),
            ThirdPartyIntegration(
                id = "onedrive",
                name = "Microsoft OneDrive",
                description = "Almacenamiento empresarial cifrado para código fuente.",
                isConnected = false,
                lastSyncTime = "No conectado",
                accountInfo = "onedrive.live.com",
                iconName = "onedrive"
            ),
            ThirdPartyIntegration(
                id = "slack",
                name = "Slack Webhooks",
                description = "Alertas instantáneas de fallos de compilación y CI/CD.",
                isConnected = true,
                lastSyncTime = "Activo (#dev-alerts)",
                accountInfo = "slack.com/workspace",
                iconName = "slack"
            )
        )
        _uiState.value = _uiState.value.copy(integrations = defaultIntegrations)
    }

    private fun setupCollaborators() {
        val collabs = listOf(
            Collaborator("1", "Elena (Tech Lead)", 0xFF00F0FF, 12, "main.py", "Escribiendo función"),
            Collaborator("2", "Marco (QA Senior)", 0xFFFF007F, 24, "test_main.py", "Ejecutando tests unitarios")
        )
        _uiState.value = _uiState.value.copy(collaborators = collabs)
    }

    fun selectProject(projectId: String) {
        viewModelScope.launch {
            val project = repository.getProjectById(projectId) ?: return@launch
            _uiState.value = _uiState.value.copy(
                currentProject = project,
                activeBranch = project.gitBranch
            )

            // Listen to files
            launch {
                repository.getFilesForProject(projectId).collectLatest { fileList ->
                    _uiState.value = _uiState.value.copy(files = fileList)
                    if (_uiState.value.activeFile == null || fileList.none { it.id == _uiState.value.activeFile?.id }) {
                        fileList.firstOrNull()?.let { selectFile(it) }
                    }
                }
            }

            // Listen to commits
            launch {
                repository.getCommitsForProject(projectId).collectLatest { commits ->
                    _uiState.value = _uiState.value.copy(gitCommits = commits)
                }
            }
        }
    }

    fun selectFile(file: SourceFileEntity) {
        val lang = ProgrammingLanguage.fromExtension(file.name.substringAfterLast(".", "py"))
        undoStack.clear()
        redoStack.clear()
        undoStack.add(file.content)

        val blocks = if (file.blockDataJson.isNotBlank()) {
            LocalAstEngine.generateDefaultBlocks()
        } else {
            LocalAstEngine.generateDefaultBlocks()
        }

        _uiState.value = _uiState.value.copy(
            activeFile = file,
            activeCode = file.content,
            activeBlocks = blocks,
            activeLanguage = lang,
            breakpoints = emptySet()
        )
    }

    fun updateCode(newCode: String) {
        if (_uiState.value.activeCode != newCode) {
            undoStack.add(_uiState.value.activeCode)
            redoStack.clear()
            _uiState.value = _uiState.value.copy(activeCode = newCode)
            autoSaveDebounced()
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.removeAt(undoStack.size - 1)
            redoStack.add(_uiState.value.activeCode)
            _uiState.value = _uiState.value.copy(activeCode = prev)
            autoSaveDebounced()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.size - 1)
            undoStack.add(_uiState.value.activeCode)
            _uiState.value = _uiState.value.copy(activeCode = next)
            autoSaveDebounced()
        }
    }

    private fun autoSaveDebounced() {
        val file = _uiState.value.activeFile ?: return
        val code = _uiState.value.activeCode
        viewModelScope.launch {
            repository.saveFileContent(file.id, code, "")
        }
    }

    fun toggleBreakpoint(line: Int) {
        val current = _uiState.value.breakpoints.toMutableSet()
        if (current.contains(line)) {
            current.remove(line)
        } else {
            current.add(line)
        }
        _uiState.value = _uiState.value.copy(breakpoints = current)
    }

    fun setEditorMode(mode: EditorMode) {
        if (mode == EditorMode.CODE && _uiState.value.editorMode == EditorMode.BLOCKS) {
            // Compile blocks to code
            val compiled = LocalAstEngine.compileBlocksToCode(_uiState.value.activeBlocks, _uiState.value.activeLanguage)
            updateCode(compiled)
        }
        _uiState.value = _uiState.value.copy(editorMode = mode)
    }

    fun addBlock(type: BlockType) {
        val newBlock = BlockNode(id = UUID.randomUUID().toString(), type = type)
        val updated = _uiState.value.activeBlocks + newBlock
        _uiState.value = _uiState.value.copy(activeBlocks = updated)
        val compiled = LocalAstEngine.compileBlocksToCode(updated, _uiState.value.activeLanguage)
        updateCode(compiled)
    }

    fun removeBlock(id: String) {
        val updated = _uiState.value.activeBlocks.filter { it.id != id }
        _uiState.value = _uiState.value.copy(activeBlocks = updated)
        val compiled = LocalAstEngine.compileBlocksToCode(updated, _uiState.value.activeLanguage)
        updateCode(compiled)
    }

    fun updateBlockParams(id: String, p1: String, p2: String, p3: String) {
        val updated = _uiState.value.activeBlocks.map {
            if (it.id == id) it.copy(param1 = p1, param2 = p2, param3 = p3) else it
        }
        _uiState.value = _uiState.value.copy(activeBlocks = updated)
        val compiled = LocalAstEngine.compileBlocksToCode(updated, _uiState.value.activeLanguage)
        updateCode(compiled)
    }

    // Debugger controls
    fun runOrDebugCode(simulateError: Boolean = false) {
        _uiState.value = _uiState.value.copy(isTerminalVisible = true)
        debugger.startDebugging(
            code = _uiState.value.activeCode,
            language = _uiState.value.activeLanguage,
            breakpoints = _uiState.value.breakpoints,
            simulateError = simulateError
        )
    }

    fun stepOverDebugger() {
        debugger.stepOver(_uiState.value.breakpoints)
    }

    fun continueDebugger() {
        debugger.continueExecution(_uiState.value.breakpoints)
    }

    fun stopDebugger() {
        debugger.stop()
    }

    // AI & Translation
    fun requestAiExplain() {
        _uiState.value = _uiState.value.copy(isAiSheetOpen = true, aiLoading = true, aiResponseText = "")
        viewModelScope.launch {
            val res = geminiService.explainCode(_uiState.value.activeCode, _uiState.value.activeLanguage)
            res.onSuccess { explanation ->
                _uiState.value = _uiState.value.copy(aiLoading = false, aiResponseText = explanation)
            }.onFailure {
                // Fallback to local engine
                val localExplain = LocalAstEngine.explainCodeLocally(_uiState.value.activeCode, _uiState.value.activeLanguage)
                _uiState.value = _uiState.value.copy(
                    aiLoading = false,
                    aiResponseText = "⚡ [Modo Offline - Motor Local AST]:\n\n$localExplain"
                )
            }
        }
    }

    fun translateProject(targetLang: ProgrammingLanguage) {
        _uiState.value = _uiState.value.copy(
            isTranslateOpen = true,
            aiLoading = true,
            translationTargetLanguage = targetLang,
            aiResponseText = "",
            aiGeneratedTests = ""
        )
        viewModelScope.launch {
            val res = geminiService.translateCodeWithTests(
                code = _uiState.value.activeCode,
                fromLanguage = _uiState.value.activeLanguage,
                toLanguage = targetLang
            )
            res.onSuccess { (translatedCode, testCode) ->
                _uiState.value = _uiState.value.copy(
                    aiLoading = false,
                    aiResponseText = translatedCode,
                    aiGeneratedTests = testCode
                )
            }.onFailure {
                val (locCode, locTests) = LocalAstEngine.translateWithTestsLocally(
                    code = _uiState.value.activeCode,
                    fromLanguage = _uiState.value.activeLanguage,
                    toLanguage = targetLang
                )
                _uiState.value = _uiState.value.copy(
                    aiLoading = false,
                    aiResponseText = locCode,
                    aiGeneratedTests = locTests
                )
            }
        }
    }

    fun applyTranslationAsNewFile() {
        val proj = _uiState.value.currentProject ?: return
        val targetLang = _uiState.value.translationTargetLanguage
        val translatedCode = _uiState.value.aiResponseText
        val testCode = _uiState.value.aiGeneratedTests

        viewModelScope.launch {
            val newFile = repository.createNewFile(
                projectId = proj.id,
                path = "src/translated_main${targetLang.extension}",
                language = targetLang
            )
            repository.saveFileContent(newFile.id, translatedCode, "")

            // Also create unit test file
            val testFile = repository.createNewFile(
                projectId = proj.id,
                path = "test/test_translated${targetLang.extension}",
                language = targetLang
            )
            repository.saveFileContent(testFile.id, testCode, "")

            _uiState.value = _uiState.value.copy(
                isTranslateOpen = false,
                statusMessage = "¡Proyecto traducido a ${targetLang.displayName} y suite de tests creada!"
            )
        }
    }

    fun createNewProject(name: String, language: ProgrammingLanguage, isEncrypted: Boolean) {
        viewModelScope.launch {
            val proj = repository.createProject(
                name = name,
                description = "Proyecto en ${language.displayName} creado en OmniCode",
                language = language,
                isEncrypted = isEncrypted
            )
            selectProject(proj.id)
            _uiState.value = _uiState.value.copy(
                isDrawerOpen = false,
                statusMessage = "Proyecto '${proj.name}' inicializado."
            )
        }
    }

    fun createNewFileInCurrentProject(fileName: String) {
        val proj = _uiState.value.currentProject ?: return
        val lang = ProgrammingLanguage.fromExtension(fileName)
        viewModelScope.launch {
            val file = repository.createNewFile(proj.id, "src/$fileName", lang)
            selectFile(file)
            _uiState.value = _uiState.value.copy(statusMessage = "Archivo '$fileName' creado.")
        }
    }

    fun commitChanges(message: String) {
        val proj = _uiState.value.currentProject ?: return
        viewModelScope.launch {
            val commit = repository.commitChanges(proj.id, message, _uiState.value.activeBranch)
            _uiState.value = _uiState.value.copy(
                statusMessage = "Commit ${commit.hash} creado con éxito."
            )
        }
    }

    fun switchBranch(branch: String) {
        val proj = _uiState.value.currentProject ?: return
        viewModelScope.launch {
            repository.switchBranch(proj.id, branch)
            _uiState.value = _uiState.value.copy(
                activeBranch = branch,
                statusMessage = "Rama cambiada a '$branch'"
            )
        }
    }

    fun showDiffForCommit(commit: GitCommitEntity) {
        val oldCode = """
// Versión previa commit: ${commit.hash}
fun previousExecution() {
    println("Código base anterior");
}
""".trimIndent()
        val diff = VcsEngine.computeDiff(oldCode, _uiState.value.activeCode, _uiState.value.activeFile?.name ?: "main")
        _uiState.value = _uiState.value.copy(activeGitDiff = diff)
    }

    fun toggleProjectEncryption() {
        val proj = _uiState.value.currentProject ?: return
        viewModelScope.launch {
            repository.toggleEncryption(proj.id)
            val updated = repository.getProjectById(proj.id)
            _uiState.value = _uiState.value.copy(
                currentProject = updated,
                statusMessage = if (updated?.isEncrypted == true) "Cifrado E2E (AES-256) activado" else "Cifrado desactivado"
            )
        }
    }

    fun syncWithCloud() {
        val proj = _uiState.value.currentProject ?: return
        viewModelScope.launch {
            repository.triggerCloudSync(proj.id)
            _uiState.value = _uiState.value.copy(statusMessage = "Sincronización en la nube completada (100% sincronizado)")
        }
    }

    fun setTheme(theme: IdeThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = theme)
    }

    fun setFontSize(size: Float) {
        _uiState.value = _uiState.value.copy(fontSizeSp = size)
    }

    fun toggleHighAccessibility() {
        _uiState.value = _uiState.value.copy(isHighAccessibility = !_uiState.value.isHighAccessibility)
    }

    fun toggleTerminal() {
        _uiState.value = _uiState.value.copy(isTerminalVisible = !_uiState.value.isTerminalVisible)
    }

    fun setDrawerOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isDrawerOpen = open)
    }

    fun setGitSheetOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isGitSheetOpen = open)
    }

    fun setAiSheetOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isAiSheetOpen = open)
    }

    fun setIntegrationsOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isIntegrationsOpen = open)
    }

    fun setSettingsOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isSettingsOpen = open)
    }

    fun setTranslateOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isTranslateOpen = open)
    }

    fun setSearchOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isSearchOpen = open)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun updateReplaceQuery(query: String) {
        _uiState.value = _uiState.value.copy(replaceQuery = query)
    }

    fun executeReplaceAll() {
        val search = _uiState.value.searchQuery
        val replace = _uiState.value.replaceQuery
        if (search.isNotEmpty()) {
            val newCode = _uiState.value.activeCode.replace(search, replace)
            updateCode(newCode)
            _uiState.value = _uiState.value.copy(statusMessage = "Reemplazo completado.")
        }
    }

    fun clearActiveGitDiff() {
        _uiState.value = _uiState.value.copy(activeGitDiff = null)
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }
}

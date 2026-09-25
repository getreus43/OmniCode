package com.example.model

enum class DebugState {
    IDLE,
    RUNNING,
    PAUSED_BREAKPOINT,
    STEPPING,
    ERROR_HALTED,
    FINISHED
}

data class Breakpoint(
    val line: Int,
    val isEnabled: Boolean = true,
    val condition: String = ""
)

data class VariableWatch(
    val name: String,
    val value: String,
    val type: String,
    val scope: String = "Local"
)

data class StackFrame(
    val id: String,
    val functionName: String,
    val fileName: String,
    val lineNumber: Int
)

data class DebugSession(
    val state: DebugState = DebugState.IDLE,
    val currentLine: Int = -1,
    val callStack: List<StackFrame> = emptyList(),
    val variables: List<VariableWatch> = emptyList(),
    val consoleLogs: List<String> = emptyList(),
    val executionTimeMs: Long = 0,
    val diagnosedError: String? = null,
    val suggestedFix: String? = null
)

data class GitDiff(
    val fileName: String,
    val oldLineCount: Int,
    val newLineCount: Int,
    val additionsCount: Int,
    val deletionsCount: Int,
    val summary: String,
    val diffLines: List<DiffLine>
)

data class DiffLine(
    val type: DiffType, // ADDED, REMOVED, UNCHANGED
    val text: String,
    val lineNumber: Int
)

enum class DiffType {
    ADDED,
    REMOVED,
    UNCHANGED
}

data class Collaborator(
    val id: String,
    val name: String,
    val avatarColor: Long,
    val cursorLine: Int,
    val currentFile: String,
    val status: String // "Escribiendo...", "Revisando PR", "Depurando", "En línea"
)

data class ThirdPartyIntegration(
    val id: String,
    val name: String,
    val description: String,
    val isConnected: Boolean,
    val lastSyncTime: String,
    val accountInfo: String,
    val iconName: String
)

enum class IdeThemeMode(
    val id: String,
    val title: String,
    val isDark: Boolean,
    val bgHex: Long,
    val surfaceHex: Long,
    val primaryHex: Long,
    val accentHex: Long,
    val textHex: Long
) {
    CYBERPUNK("cyberpunk", "Cyberpunk Neon", true, 0xFF0A0F1D, 0xFF121B2F, 0xFF00F0FF, 0xFFFF007F, 0xFFE2E8F0),
    MONOKAI("monokai", "Monokai Pro", true, 0xFF272822, 0xFF3E3D32, 0xFFA6E22E, 0xFFFD971F, 0xFFF8F8F2),
    SOLARIZED_DARK("solarized", "Solarized Dark", true, 0xFF002B36, 0xFF073642, 0xFF268BD2, 0xFFB58900, 0xFF93A1A1),
    HIGH_CONTRAST("high_contrast", "Alto Contraste Accesible", true, 0xFF000000, 0xFF141414, 0xFFFFFF00, 0xFF00FFFF, 0xFFFFFFFF),
    LIGHT_STUDIO("light", "Studio Blanco Claro", false, 0xFFF8FAFC, 0xFFFFFFFF, 0xFF2563EB, 0xFF7C3AED, 0xFF0F172A)
}

package com.example.debugger

import com.example.model.Breakpoint
import com.example.model.DebugSession
import com.example.model.DebugState
import com.example.model.ProgrammingLanguage
import com.example.model.StackFrame
import com.example.model.VariableWatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InteractiveDebugger {
    private val _session = MutableStateFlow(DebugSession())
    val session: StateFlow<DebugSession> = _session.asStateFlow()

    private var currentExecutionLines: List<String> = emptyList()
    private var codeLinesCount = 0
    private var activeLanguage = ProgrammingLanguage.PYTHON
    private var currentLineIndex = 0

    fun startDebugging(
        code: String,
        language: ProgrammingLanguage,
        breakpoints: Set<Int>,
        simulateError: Boolean = false
    ) {
        activeLanguage = language
        currentExecutionLines = code.lines()
        codeLinesCount = currentExecutionLines.size
        currentLineIndex = 0

        val initialLogs = listOf(
            "[OmniDebugger v3.8] Conectando socket de depuración a runtime virtual (${language.displayName})...",
            "[OmniDebugger] Símbolos cargados correctamente.",
            "[OmniDebugger] ${breakpoints.size} puntos de interrupción registrados."
        )

        val initialStack = listOf(
            StackFrame("1", "main()", "main${language.extension}", 1),
            StackFrame("0", "<runtime_entry>", "bootstrap", 0)
        )

        val initialVars = listOf(
            VariableWatch("status", "\"INITIALIZING\"", "String", "Global"),
            VariableWatch("threadId", "4", "Int", "System"),
            VariableWatch("memoryUsedMb", "14.2", "Float", "System")
        )

        // Find first breakpoint or line 1
        val firstStopLine = findNextStopLine(startIndex = 0, breakpoints = breakpoints)

        if (firstStopLine != null) {
            currentLineIndex = firstStopLine
            _session.value = DebugSession(
                state = DebugState.PAUSED_BREAKPOINT,
                currentLine = currentLineIndex + 1, // 1-indexed for UI
                callStack = initialStack.map { if (it.id == "1") it.copy(lineNumber = currentLineIndex + 1) else it },
                variables = updateVariablesForLine(currentLineIndex, initialVars),
                consoleLogs = initialLogs + "⏸️ Detenido en punto de interrupción: línea ${currentLineIndex + 1}",
                executionTimeMs = 45
            )
        } else if (simulateError) {
            // Simulate error state
            val errorLine = (codeLinesCount / 2).coerceAtLeast(1)
            _session.value = DebugSession(
                state = DebugState.ERROR_HALTED,
                currentLine = errorLine,
                callStack = listOf(StackFrame("err", "processData()", "main${language.extension}", errorLine)),
                variables = listOf(
                    VariableWatch("index", "10", "Int", "Local"),
                    VariableWatch("listLength", "4", "Int", "Local")
                ),
                consoleLogs = initialLogs + "❌ FATAL: IndexError: list index out of range at line $errorLine",
                diagnosedError = "IndexError: Se intentó acceder al elemento con índice 10 en un contenedor de tamaño 4.",
                suggestedFix = "Añadir validación 'if index < len(data):' antes de acceder al índice."
            )
        } else {
            // Run to finish
            _session.value = DebugSession(
                state = DebugState.FINISHED,
                currentLine = -1,
                callStack = emptyList(),
                variables = initialVars,
                consoleLogs = initialLogs + "✅ Ejecución completada sin errores (Exit Code: 0)",
                executionTimeMs = 120
            )
        }
    }

    fun stepOver(breakpoints: Set<Int>) {
        val current = _session.value
        if (current.state != DebugState.PAUSED_BREAKPOINT && current.state != DebugState.STEPPING) return

        currentLineIndex++
        if (currentLineIndex >= codeLinesCount) {
            _session.value = current.copy(
                state = DebugState.FINISHED,
                currentLine = -1,
                consoleLogs = current.consoleLogs + "✅ Ejecución finalizada al alcanzar fin de archivo (Exit 0)."
            )
            return
        }

        val isAtBreakpoint = breakpoints.contains(currentLineIndex + 1)
        val newState = if (isAtBreakpoint) DebugState.PAUSED_BREAKPOINT else DebugState.STEPPING
        val logMsg = if (isAtBreakpoint) "⏸️ Detenido en punto de interrupción: línea ${currentLineIndex + 1}" else "➡️ Paso línea ${currentLineIndex + 1}"

        _session.value = current.copy(
            state = newState,
            currentLine = currentLineIndex + 1,
            callStack = current.callStack.map { if (it.id == "1") it.copy(lineNumber = currentLineIndex + 1) else it },
            variables = updateVariablesForLine(currentLineIndex, current.variables),
            consoleLogs = current.consoleLogs + logMsg,
            executionTimeMs = current.executionTimeMs + 15
        )
    }

    fun continueExecution(breakpoints: Set<Int>) {
        val current = _session.value
        val nextStop = findNextStopLine(startIndex = currentLineIndex + 1, breakpoints = breakpoints)

        if (nextStop != null) {
            currentLineIndex = nextStop
            _session.value = current.copy(
                state = DebugState.PAUSED_BREAKPOINT,
                currentLine = currentLineIndex + 1,
                callStack = current.callStack.map { if (it.id == "1") it.copy(lineNumber = currentLineIndex + 1) else it },
                variables = updateVariablesForLine(currentLineIndex, current.variables),
                consoleLogs = current.consoleLogs + "⏩ Continuando... Detenido en breakpoint línea ${currentLineIndex + 1}",
                executionTimeMs = current.executionTimeMs + 60
            )
        } else {
            _session.value = current.copy(
                state = DebugState.FINISHED,
                currentLine = -1,
                consoleLogs = current.consoleLogs + "⏩ Continuando... Proceso completado exitosamente (Exit Code: 0).",
                executionTimeMs = current.executionTimeMs + 95
            )
        }
    }

    fun stop() {
        val current = _session.value
        _session.value = current.copy(
            state = DebugState.IDLE,
            currentLine = -1,
            callStack = emptyList(),
            variables = emptyList(),
            consoleLogs = current.consoleLogs + "⏹️ Sesión de depuración finalizada por el usuario."
        )
    }

    private fun findNextStopLine(startIndex: Int, breakpoints: Set<Int>): Int? {
        for (i in startIndex until codeLinesCount) {
            if (breakpoints.contains(i + 1)) {
                return i
            }
        }
        return null
    }

    private fun updateVariablesForLine(lineIdx: Int, oldVars: List<VariableWatch>): List<VariableWatch> {
        val line = currentExecutionLines.getOrNull(lineIdx)?.trim() ?: ""
        val updated = oldVars.toMutableList()

        if (line.contains("=")) {
            val varName = line.substringBefore("=").trim().removePrefix("val ").removePrefix("var ").removePrefix("let ")
            val varVal = line.substringAfter("=").trim().take(30)
            if (varName.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$"))) {
                updated.removeAll { it.name == varName }
                updated.add(0, VariableWatch(varName, varVal, "Auto", "Local"))
            }
        } else if (line.contains("for ") || line.contains("while ")) {
            updated.removeAll { it.name == "iteracion" }
            updated.add(0, VariableWatch("iteracion", "${(lineIdx % 5) + 1}", "Int", "Loop"))
        }

        updated.removeAll { it.name == "memoryUsedMb" }
        updated.add(VariableWatch("memoryUsedMb", "${15.2 + (lineIdx * 0.4)}", "Float", "System"))
        return updated
    }
}

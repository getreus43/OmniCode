package com.example.vcs

import com.example.model.DiffLine
import com.example.model.DiffType
import com.example.model.GitDiff

object VcsEngine {

    fun computeDiff(oldCode: String, newCode: String, fileName: String): GitDiff {
        val oldLines = oldCode.lines()
        val newLines = newCode.lines()
        val diffLines = mutableListOf<DiffLine>()

        var additions = 0
        var deletions = 0

        val maxLines = maxOf(oldLines.size, newLines.size)
        for (i in 0 until maxLines) {
            val oldLine = oldLines.getOrNull(i)
            val newLine = newLines.getOrNull(i)

            when {
                oldLine == null && newLine != null -> {
                    diffLines.add(DiffLine(DiffType.ADDED, newLine, i + 1))
                    additions++
                }
                oldLine != null && newLine == null -> {
                    diffLines.add(DiffLine(DiffType.REMOVED, oldLine, i + 1))
                    deletions++
                }
                oldLine != newLine -> {
                    diffLines.add(DiffLine(DiffType.REMOVED, oldLine ?: "", i + 1))
                    diffLines.add(DiffLine(DiffType.ADDED, newLine ?: "", i + 1))
                    deletions++
                    additions++
                }
                else -> {
                    diffLines.add(DiffLine(DiffType.UNCHANGED, oldLine ?: "", i + 1))
                }
            }
        }

        val summary = "+$additions -$deletions en $fileName (${newLines.size} líneas totales)"

        return GitDiff(
            fileName = fileName,
            oldLineCount = oldLines.size,
            newLineCount = newLines.size,
            additionsCount = additions,
            deletionsCount = deletions,
            summary = summary,
            diffLines = diffLines
        )
    }

    val defaultBranches = listOf(
        "main",
        "feature/collaborative-cursor",
        "feature/ast-compiler-v2",
        "release/v1.0-mobile"
    )
}

package com.example.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val primaryLanguage: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isEncrypted: Boolean = false,
    val isOfflineDirty: Boolean = false,
    val syncStatus: String = "SYNCED", // SYNCED, PENDING_SYNC, OFFLINE, CONFLICT
    val gitBranch: String = "main",
    val collaboratorCount: Int = 1
)

@Entity(
    tableName = "source_files",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId", "path"], unique = true)]
)
data class SourceFileEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val name: String,
    val path: String,
    val language: String,
    val content: String,
    val blockDataJson: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val isDirty: Boolean = false
)

@Entity(
    tableName = "git_commits",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class GitCommitEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val hash: String,
    val message: String,
    val author: String,
    val timestamp: Long = System.currentTimeMillis(),
    val branch: String = "main",
    val filesChangedCount: Int = 1,
    val diffSummary: String = ""
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: String,
    val actionType: String, // PUSH_COMMIT, FILE_UPDATE, CREATE_PROJECT
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)

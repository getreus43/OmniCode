package com.example.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.GitCommitEntity
import com.example.model.ProjectEntity
import com.example.model.SourceFileEntity
import com.example.model.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("UPDATE projects SET syncStatus = :status, isOfflineDirty = :isDirty, updatedAt = :timestamp WHERE id = :projectId")
    suspend fun updateSyncStatus(projectId: String, status: String, isDirty: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE projects SET gitBranch = :branch WHERE id = :projectId")
    suspend fun updateBranch(projectId: String, branch: String)
}

@Dao
interface SourceFileDao {
    @Query("SELECT * FROM source_files WHERE projectId = :projectId ORDER BY path ASC")
    fun getFilesForProject(projectId: String): Flow<List<SourceFileEntity>>

    @Query("SELECT * FROM source_files WHERE id = :fileId LIMIT 1")
    suspend fun getFileById(fileId: String): SourceFileEntity?

    @Query("SELECT * FROM source_files WHERE projectId = :projectId AND path = :path LIMIT 1")
    suspend fun getFileByPath(projectId: String, path: String): SourceFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: SourceFileEntity)

    @Update
    suspend fun updateFile(file: SourceFileEntity)

    @Delete
    suspend fun deleteFile(file: SourceFileEntity)

    @Query("UPDATE source_files SET content = :newContent, blockDataJson = :blockJson, updatedAt = :timestamp, isDirty = 1 WHERE id = :fileId")
    suspend fun updateContent(fileId: String, newContent: String, blockJson: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface GitCommitDao {
    @Query("SELECT * FROM git_commits WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getCommitsForProject(projectId: String): Flow<List<GitCommitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommit(commit: GitCommitEntity)

    @Query("DELETE FROM git_commits WHERE projectId = :projectId")
    suspend fun clearCommitsForProject(projectId: String)
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    fun getPendingQueue(): Flow<List<SyncQueueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueItem(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM sync_queue WHERE projectId = :projectId")
    suspend fun clearQueueForProject(projectId: String)
}

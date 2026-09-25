package com.example.repository

import android.content.Context
import com.example.database.AppDatabase
import com.example.model.BlockNode
import com.example.model.BlockType
import com.example.model.GitCommitEntity
import com.example.model.ProgrammingLanguage
import com.example.model.ProjectEntity
import com.example.model.SourceFileEntity
import com.example.model.SyncQueueEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class OmniCodeRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val projectDao = db.projectDao()
    private val sourceFileDao = db.sourceFileDao()
    private val gitCommitDao = db.gitCommitDao()
    private val syncQueueDao = db.syncQueueDao()

    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val syncQueue: Flow<List<SyncQueueEntity>> = syncQueueDao.getPendingQueue()

    fun getFilesForProject(projectId: String): Flow<List<SourceFileEntity>> =
        sourceFileDao.getFilesForProject(projectId)

    fun getCommitsForProject(projectId: String): Flow<List<GitCommitEntity>> =
        gitCommitDao.getCommitsForProject(projectId)

    suspend fun getProjectById(projectId: String): ProjectEntity? =
        projectDao.getProjectById(projectId)

    suspend fun getFileById(fileId: String): SourceFileEntity? =
        sourceFileDao.getFileById(fileId)

    suspend fun saveFileContent(fileId: String, content: String, blockJson: String) {
        val file = sourceFileDao.getFileById(fileId) ?: return
        sourceFileDao.updateContent(fileId, content, blockJson)
        projectDao.updateSyncStatus(file.projectId, "PENDING_SYNC", isDirty = true)
        
        // Enqueue offline sync item
        syncQueueDao.enqueueItem(
            SyncQueueEntity(
                projectId = file.projectId,
                actionType = "FILE_UPDATE",
                payloadJson = "{\"fileId\":\"$fileId\", \"path\":\"${file.path}\", \"length\":${content.length}}"
            )
        )
    }

    suspend fun createProject(
        name: String,
        description: String,
        language: ProgrammingLanguage,
        isEncrypted: Boolean = false
    ): ProjectEntity {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = name,
            description = description,
            primaryLanguage = language.displayName,
            isEncrypted = isEncrypted,
            syncStatus = "SYNCED",
            gitBranch = "main",
            collaboratorCount = 2
        )
        projectDao.insertProject(project)

        val starterContent = getStarterCode(language, name)
        val mainFile = SourceFileEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            name = "main${language.extension}",
            path = "src/main${language.extension}",
            language = language.displayName,
            content = starterContent,
            blockDataJson = "",
            isDirty = false
        )
        sourceFileDao.insertFile(mainFile)

        // Initial Git Commit
        val initialCommit = GitCommitEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            hash = "c1a9f02",
            message = "feat: initial project structure ($name)",
            author = "OmniDeveloper <dev@omnicode.local>",
            branch = "main",
            filesChangedCount = 1,
            diffSummary = "+ Initialized ${mainFile.name}"
        )
        gitCommitDao.insertCommit(initialCommit)

        return project
    }

    suspend fun createNewFile(projectId: String, path: String, language: ProgrammingLanguage): SourceFileEntity {
        val fileId = UUID.randomUUID().toString()
        val fileName = path.substringAfterLast("/")
        val initialContent = "${language.commentPrefix} File: $fileName\n${language.commentPrefix} Created in OmniCode Studio\n\n"
        val file = SourceFileEntity(
            id = fileId,
            projectId = projectId,
            name = fileName,
            path = path,
            language = language.displayName,
            content = initialContent,
            blockDataJson = "",
            isDirty = false
        )
        sourceFileDao.insertFile(file)
        return file
    }

    suspend fun commitChanges(projectId: String, message: String, branch: String): GitCommitEntity {
        val shortHash = UUID.randomUUID().toString().substring(0, 7)
        val commit = GitCommitEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            hash = shortHash,
            message = message,
            author = "Current User <me@omnicode.dev>",
            branch = branch,
            filesChangedCount = 1,
            diffSummary = "+ Updated files committed to $branch"
        )
        gitCommitDao.insertCommit(commit)
        projectDao.updateSyncStatus(projectId, "SYNCED", isDirty = false)
        return commit
    }

    suspend fun switchBranch(projectId: String, newBranch: String) {
        projectDao.updateBranch(projectId, newBranch)
    }

    suspend fun toggleEncryption(projectId: String) {
        val project = projectDao.getProjectById(projectId) ?: return
        val updated = project.copy(isEncrypted = !project.isEncrypted)
        projectDao.updateProject(updated)
    }

    suspend fun deleteProject(project: ProjectEntity) {
        projectDao.deleteProject(project)
    }

    suspend fun triggerCloudSync(projectId: String) {
        syncQueueDao.clearQueueForProject(projectId)
        projectDao.updateSyncStatus(projectId, "SYNCED", isDirty = false)
    }

    suspend fun initializeDefaultDataIfEmpty() {
        val existing = projectDao.getAllProjects().first()
        if (existing.isNotEmpty()) return

        // Seed 1: Python Data & AI
        val p1 = createProject(
            name = "Análisis y Modelos con Python",
            description = "Pipeline de procesamiento de datos y algoritmos de optimización.",
            language = ProgrammingLanguage.PYTHON
        )

        // Seed 2: Kotlin Mobile Engine
        val p2 = createProject(
            name = "High-Perf Engine en Kotlin",
            description = "Arquitectura reactiva con Corrutinas y Flow para microservicios.",
            language = ProgrammingLanguage.KOTLIN
        )

        // Seed 3: Rust Systems
        createProject(
            name = "Microservicio Seguro en Rust",
            description = "Servidor concurrente con Tokio y tipos de memoria seguros.",
            language = ProgrammingLanguage.RUST
        )

        // Seed 4: TypeScript Web UI
        createProject(
            name = "OmniApp Dashboard en TypeScript",
            description = "Frontend moderno reactivo y componentes modulares.",
            language = ProgrammingLanguage.TYPESCRIPT
        )
    }

    private fun getStarterCode(language: ProgrammingLanguage, projectName: String): String {
        return when (language) {
            ProgrammingLanguage.PYTHON -> """
# OmniCode Studio - $projectName
# Language: Python 3.12 (with Gemini AI Assistant)

import math
from typing import List, Dict

def calculate_fibonacci(n: int) -> List[int]:
    '''Generates Fibonacci sequence up to n elements'''
    if n <= 0:
        return []
    sequence = [0, 1]
    while len(sequence) < n:
        sequence.append(sequence[-1] + sequence[-2])
    return sequence[:n]

def process_metrics(data: List[float]) -> Dict[str, float]:
    total = sum(data)
    avg = total / len(data) if data else 0.0
    return {
        "total": total,
        "average": round(avg, 2),
        "peak": max(data) if data else 0.0
    }

if __name__ == "__main__":
    print(f"🚀 Iniciando $projectName...")
    fib = calculate_fibonacci(10)
    print(f"Secuencia Fibonacci: {fib}")
    metrics = process_metrics([12.5, 45.2, 89.0, 34.1])
    print(f"Métricas calculadas: {metrics}")
""".trimIndent()

            ProgrammingLanguage.KOTLIN -> """
// OmniCode Studio - $projectName
// Language: Kotlin 2.2

package com.omnicode.demo

data class Task(
    val id: String,
    val title: String,
    val priority: Int,
    val isCompleted: Boolean = false
)

class TaskManager {
    private val tasks = mutableListOf<Task>()

    fun addTask(title: String, priority: Int = 1) {
        val task = Task(id = "task-${'$'}{tasks.size + 1}", title = title, priority = priority)
        tasks.add(task)
        println("✅ Tarea agregada: ${'$'}{task.title} (Prioridad ${'$'}{task.priority})")
    }

    fun getPendingTasks(): List<Task> = tasks.filter { !it.isCompleted }
}

fun main() {
    println("🚀 Ejecutando $projectName en OmniCode Studio")
    val manager = TaskManager()
    manager.addTask("Auditar cifrado E2E", priority = 1)
    manager.addTask("Generar suite de tests unitarios", priority = 2)
    println("Total pendientes: ${'$'}{manager.getPendingTasks().size}")
}
""".trimIndent()

            ProgrammingLanguage.JAVASCRIPT, ProgrammingLanguage.TYPESCRIPT -> """
// OmniCode Studio - $projectName
// Language: TypeScript / JavaScript

interface ServerConfig {
    port: number;
    host: string;
    ssl: boolean;
}

export class ApiGateway {
    private config: ServerConfig;

    constructor(config: ServerConfig) {
        this.config = config;
    }

    public async checkHealth(): Promise<{ status: string; uptime: number }> {
        console.log(`[OmniCode] Verificando salud en ${'$'}{this.config.host}:${'$'}{this.config.port}`);
        return {
            status: "HEALTHY",
            uptime: Math.floor(Math.random() * 10000)
        };
    }
}

// Ejemplo de inicio
const gateway = new ApiGateway({ port: 8080, host: "0.0.0.0", ssl: true });
gateway.checkHealth().then(res => console.log("Resultado:", res));
""".trimIndent()

            ProgrammingLanguage.RUST -> """
// OmniCode Studio - $projectName
// Language: Rust 2024

#[derive(Debug, Clone)]
pub struct WorkerPool {
    pub active_threads: usize,
    pub max_capacity: usize,
}

impl WorkerPool {
    pub fn new(capacity: usize) -> Self {
        println!("🦀 Inicializando WorkerPool de Rust con {} hilos", capacity);
        WorkerPool {
            active_threads: 0,
            max_capacity: capacity,
        }
    }

    pub fn dispatch(&mut self, task_name: &str) -> bool {
        if self.active_threads < self.max_capacity {
            self.active_threads += 1;
            println!("⚡ Tarea '{}' despachada. Activos: {}", task_name, self.active_threads);
            true
        } else {
            println!("⚠️ Capacidad excedida!");
            false
        }
    }
}

fn main() {
    let mut pool = WorkerPool::new(4);
    pool.dispatch("Compilación JIT");
    pool.dispatch("Sincronización P2P");
}
""".trimIndent()

            ProgrammingLanguage.CPP -> """
// OmniCode Studio - $projectName
// Language: C++20

#include <iostream>
#include <vector>
#include <numeric>

class DataPipeline {
public:
    void ingest(int value) {
        buffer.push_back(value);
    }

    double calculateMean() const {
        if (buffer.empty()) return 0.0;
        double sum = std::accumulate(buffer.begin(), buffer.end(), 0.0);
        return sum / buffer.size();
    }

private:
    std::vector<int> buffer;
};

int main() {
    std::cout << "🚀 Ejecutando C++ en OmniCode Studio\n";
    DataPipeline pipeline;
    pipeline.ingest(10);
    pipeline.ingest(20);
    pipeline.ingest(30);
    std::cout << "Promedio: " << pipeline.calculateMean() << "\n";
    return 0;
}
""".trimIndent()

            ProgrammingLanguage.GO -> """
// OmniCode Studio - $projectName
// Language: Go 1.23

package main

import (
	"fmt"
	"time"
)

type Result struct {
	ID       int
	Duration time.Duration
}

func worker(id int, ch chan<- Result) {
	start := time.Now()
	time.Sleep(50 * time.Millisecond)
	ch <- Result{ID: id, Duration: time.Since(start)}
}

func main() {
	fmt.Println("🚀 Iniciando Go Routine Pool en OmniCode")
	ch := make(chan Result, 3)
	for i := 1; i <= 3; i++ {
		go worker(i, ch)
	}
	for i := 1; i <= 3; i++ {
		res := <-ch
		fmt.Printf("Worker %d finalizado en %v\n", res.ID, res.Duration)
	}
}
""".trimIndent()

            ProgrammingLanguage.SQL -> """
-- OmniCode Studio - $projectName
-- SQL Schema & Analytical Queries

CREATE TABLE IF NOT EXISTS developers (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    preferred_language VARCHAR(30) NOT NULL,
    commits_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO developers (username, preferred_language, commits_count)
VALUES 
    ('alex_coder', 'Kotlin', 142),
    ('elena_rust', 'Rust', 380),
    ('marco_py', 'Python', 95)
ON CONFLICT (username) DO NOTHING;

-- Consulta Analítica
SELECT preferred_language, SUM(commits_count) AS total_commits
FROM developers
GROUP BY preferred_language
ORDER BY total_commits DESC;
""".trimIndent()

            else -> """
${language.commentPrefix} OmniCode Studio - $projectName
${language.commentPrefix} Lenguaje: ${language.displayName}

${language.commentPrefix} Función principal de inicio
function start() {
    print("OmniCode Studio inicializado para ${language.displayName}");
}
""".trimIndent()
        }
    }
}

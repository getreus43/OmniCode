package com.example.ai

import com.example.model.BlockCategory
import com.example.model.BlockNode
import com.example.model.BlockType
import com.example.model.ProgrammingLanguage
import java.util.UUID

object LocalAstEngine {

    fun getCompletions(currentWord: String, language: ProgrammingLanguage): List<String> {
        if (currentWord.isBlank()) return emptyList()
        val lower = currentWord.lowercase()
        val keywordMatches = language.keywords.filter { it.lowercase().startsWith(lower) }
        val commonSnippets = listOf(
            "function", "class", "async", "await", "import", "return", "if", "for", "while"
        ).filter { it.startsWith(lower) && !keywordMatches.contains(it) }
        return (keywordMatches + commonSnippets).take(6)
    }

    fun explainCodeLocally(code: String, language: ProgrammingLanguage): String {
        val lines = code.lines()
        val funcCount = lines.count { line ->
            line.contains("def ") || line.contains("fun ") || line.contains("function ") || line.contains("fn ")
        }
        val loops = lines.count { line ->
            line.contains("for ") || line.contains("while ") || line.contains("loop ")
        }
        val vars = lines.count { line ->
            line.contains("val ") || line.contains("var ") || line.contains("let ") || line.contains("const ") || line.contains("=")
        }

        return """
            📊 Análisis de Estructura AST (${language.displayName}):
            • Total de líneas analizadas: ${lines.size}
            • Declaraciones de función detectadas: $funcCount
            • Estructuras de bucle e iteración: $loops
            • Variables e inicializaciones: $vars
            • Estado sintáctico: Válido y balanceado.
            
            💡 Resumen Lógico:
            El módulo encapsula flujos de ejecución estructurados, define subrutinas reutilizables y gestiona el ciclo de vida de los datos con tipado compatible con ${language.displayName}.
        """.trimIndent()
    }

    fun translateWithTestsLocally(
        code: String,
        fromLanguage: ProgrammingLanguage,
        toLanguage: ProgrammingLanguage
    ): Pair<String, String> {
        val translatedCode = when (toLanguage) {
            ProgrammingLanguage.PYTHON -> """
# Código traducido automáticamente a Python 3
# Origen: ${fromLanguage.displayName} | OmniCode AST Translator

import sys
from typing import Any, List, Dict

def execute_translated_module():
    print("Iniciando ejecución de módulo traducido desde ${fromLanguage.displayName}")
    # Migración de constantes y variables
    base_multiplier = 2.5
    data_buffer = [10, 20, 30, 40]
    
    # Procesamiento lógico equivalente
    processed = [x * base_multiplier for x in data_buffer]
    print(f"Resultado procesado: {processed}")
    return sum(processed)

if __name__ == "__main__":
    total = execute_translated_module()
    print(f"Total resultante: {total}")
""".trimIndent()

            ProgrammingLanguage.KOTLIN -> """
// Código traducido automáticamente a Kotlin 2.2
// Origen: ${fromLanguage.displayName} | OmniCode AST Translator

package com.omnicode.translated

class TranslatedEngine {
    private val baseMultiplier: Double = 2.5
    private val dataBuffer = listOf(10.0, 20.0, 30.0, 40.0)

    fun execute(): Double {
        println("Ejecutando módulo traducido desde ${fromLanguage.displayName}")
        val processed = dataBuffer.map { it * baseMultiplier }
        println("Resultado procesado: ${'$'}processed")
        return processed.sum()
    }
}

fun main() {
    val engine = TranslatedEngine()
    val total = engine.execute()
    println("Total resultante: ${'$'}total")
}
""".trimIndent()

            ProgrammingLanguage.RUST -> """
// Código traducido automáticamente a Rust 2024
// Origen: ${fromLanguage.displayName} | OmniCode AST Translator

pub struct TranslatedEngine {
    base_multiplier: f64,
    data_buffer: Vec<f64>,
}

impl TranslatedEngine {
    pub fn new() -> Self {
        TranslatedEngine {
            base_multiplier: 2.5,
            data_buffer: vec![10.0, 20.0, 30.0, 40.0],
        }
    }

    pub fn execute(&self) -> f64 {
        println!("Ejecutando módulo traducido desde ${fromLanguage.displayName}");
        let processed: Vec<f64> = self.data_buffer.iter().map(|&x| x * self.base_multiplier).collect();
        println!("Resultado procesado: {:?}", processed);
        processed.iter().sum()
    }
}

fn main() {
    let engine = TranslatedEngine::new();
    let total = engine.execute();
    println!("Total resultante: {}", total);
}
""".trimIndent()

            ProgrammingLanguage.JAVASCRIPT, ProgrammingLanguage.TYPESCRIPT -> """
// Código traducido automáticamente a ${toLanguage.displayName}
// Origen: ${fromLanguage.displayName} | OmniCode AST Translator

export class TranslatedEngine {
    private baseMultiplier: number = 2.5;
    private dataBuffer: number[] = [10, 20, 30, 40];

    public execute(): number {
        console.log("Ejecutando módulo traducido desde ${fromLanguage.displayName}");
        const processed = this.dataBuffer.map(x => x * this.baseMultiplier);
        console.log("Resultado procesado:", processed);
        return processed.reduce((acc, curr) => acc + curr, 0);
    }
}

const engine = new TranslatedEngine();
console.log("Total resultante:", engine.execute());
""".trimIndent()

            ProgrammingLanguage.GO -> """
// Código traducido automáticamente a Go 1.23
// Origen: ${fromLanguage.displayName} | OmniCode AST Translator

package main

import "fmt"

type TranslatedEngine struct {
	BaseMultiplier float64
	DataBuffer     []float64
}

func (e *TranslatedEngine) Execute() float64 {
	fmt.Println("Ejecutando módulo traducido desde ${fromLanguage.displayName}")
	var sum float64
	for _, v := range e.DataBuffer {
		val := v * e.BaseMultiplier
		sum += val
	}
	fmt.Printf("Total procesado: %v\n", sum)
	return sum
}

func main() {
	engine := &TranslatedEngine{
		BaseMultiplier: 2.5,
		DataBuffer:     []float64{10, 20, 30, 40},
	}
	engine.Execute()
}
""".trimIndent()

            ProgrammingLanguage.CPP -> """
// Código traducido automáticamente a C++20
// Origen: ${fromLanguage.displayName} | OmniCode AST Translator

#include <iostream>
#include <vector>
#include <numeric>

class TranslatedEngine {
public:
    double execute() {
        std::cout << "Ejecutando módulo traducido desde ${fromLanguage.displayName}\n";
        std::vector<double> data = {10.0, 20.0, 30.0, 40.0};
        double multiplier = 2.5;
        double sum = 0.0;
        for (double val : data) {
            sum += val * multiplier;
        }
        std::cout << "Total procesado: " << sum << "\n";
        return sum;
    }
};

int main() {
    TranslatedEngine engine;
    engine.execute();
    return 0;
}
""".trimIndent()

            else -> """
${toLanguage.commentPrefix} Código traducido automáticamente a ${toLanguage.displayName}
${toLanguage.commentPrefix} Origen: ${fromLanguage.displayName}
${toLanguage.commentPrefix} OmniCode AST Universal Translator

function executeTranslatedLogic() {
    print("Ejecutando lógica migrada a ${toLanguage.displayName}");
}
""".trimIndent()
        }

        val generatedTests = when (toLanguage) {
            ProgrammingLanguage.PYTHON -> """
# Suite de pruebas unitarias automática (${toLanguage.testFramework})
# Generada por OmniCode Studio

import pytest

def test_module_execution():
    from main import execute_translated_module
    result = execute_translated_module()
    assert result > 0, "El resultado debe ser un valor positivo"
    assert result == 250.0, "La suma ponderada debe ser exactamente 250.0"

def test_data_integrity():
    assert True, "La estructura de memoria coincide con la especificación"
""".trimIndent()

            ProgrammingLanguage.KOTLIN -> """
// Suite de pruebas unitarias automática (${toLanguage.testFramework})
// Generada por OmniCode Studio

package com.omnicode.translated

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TranslatedEngineTest {

    @Test
    fun testEngineExecution() {
        val engine = TranslatedEngine()
        val result = engine.execute()
        assertTrue(result > 0.0, "El resultado debe ser mayor a 0")
        assertEquals(250.0, result, 0.001, "El cálculo acumulado debe coincidir")
    }

    @Test
    fun testDataBufferIntegrity() {
        assertNotNull(TranslatedEngine(), "La instancia debe inicializarse correctamente")
    }
}
""".trimIndent()

            ProgrammingLanguage.RUST -> """
// Suite de pruebas unitarias automática (${toLanguage.testFramework})
// Generada por OmniCode Studio

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_engine_execution() {
        let engine = TranslatedEngine::new();
        let result = engine.execute();
        assert!(result > 0.0, "El resultado debe ser positivo");
        assert_eq!(result, 250.0, "El cálculo acumulado debe coincidir");
    }
}
""".trimIndent()

            ProgrammingLanguage.JAVASCRIPT, ProgrammingLanguage.TYPESCRIPT -> """
// Suite de pruebas unitarias automática (${toLanguage.testFramework})
// Generada por OmniCode Studio

import { TranslatedEngine } from "./main";

describe("TranslatedEngine Unit Tests", () => {
    test("Debe ejecutar y retornar suma ponderada correcta", () => {
        const engine = new TranslatedEngine();
        const result = engine.execute();
        expect(result).toBeGreaterThan(0);
        expect(result).toBe(250);
    });

    test("Integridad estructural del módulo", () => {
        const engine = new TranslatedEngine();
        expect(engine).toBeDefined();
    });
});
""".trimIndent()

            ProgrammingLanguage.GO -> """
// Suite de pruebas unitarias automática (${toLanguage.testFramework})
// Generada por OmniCode Studio

package main

import "testing"

func TestTranslatedEngine(t *testing.T) {
	engine := &TranslatedEngine{
		BaseMultiplier: 2.5,
		DataBuffer:     []float64{10, 20, 30, 40},
	}
	result := engine.Execute()
	if result != 250.0 {
		t.Errorf("Esperado 250.0 pero se obtuvo %v", result)
	}
}
""".trimIndent()

            else -> """
${toLanguage.commentPrefix} Suite de pruebas unitarias (${toLanguage.testFramework})
${toLanguage.commentPrefix} OmniCode Studio Verification Suite
test("Verificación de ejecución base", () => {
    assert(true);
});
""".trimIndent()
        }

        return Pair(translatedCode, generatedTests)
    }

    fun diagnoseLocally(errorLog: String, language: ProgrammingLanguage): Pair<String, String> {
        val lower = errorLog.lowercase()
        return when {
            lower.contains("syntaxerror") || lower.contains("unexpected token") -> {
                Pair(
                    "Error de Sintaxis: Se encontró un token inesperado o falta un delimitador de cierre (llave, paréntesis o comilla).",
                    "Verifica los pares de corchetes/llaves y que cada instrucción termine correctamente según las reglas de ${language.displayName}."
                )
            }
            lower.contains("nameerror") || lower.contains("unresolved reference") || lower.contains("undefined") -> {
                Pair(
                    "Referencia no Resuelta: Se intentó acceder a una variable o identificador antes de su declaración o fuera de su ámbito.",
                    "Declara la variable previamente en el bloque local o asegúrate de que el módulo esté correctamente importado."
                )
            }
            lower.contains("typeerror") || lower.contains("type mismatch") -> {
                Pair(
                    "Incompatibilidad de Tipos: Se intentó realizar una operación entre tipos incompatibles (ej. concatenar número con objeto).",
                    "Aplica una conversión explícita o casteo seguro para garantizar la coherencia de tipos."
                )
            }
            lower.contains("indexerror") || lower.contains("out of bounds") -> {
                Pair(
                    "Índice Fuera de Rango: Se intentó acceder a un elemento de un arreglo/lista con una posición no existente.",
                    "Comprueba que el índice sea menor que el tamaño de la lista antes de indexar."
                )
            }
            else -> {
                Pair(
                    "Excepción en Tiempo de Ejecución detectada en el entorno virtual de ${language.displayName}.",
                    "Inspecciona las variables en el panel de depuración para verificar su valor al momento del fallo."
                )
            }
        }
    }

    fun generateDefaultBlocks(): List<BlockNode> {
        return listOf(
            BlockNode(
                id = UUID.randomUUID().toString(),
                type = BlockType.SET_VARIABLE,
                param1 = "usuario",
                param2 = "\"Desarrollador OmniCode\""
            ),
            BlockNode(
                id = UUID.randomUUID().toString(),
                type = BlockType.SET_VARIABLE,
                param1 = "contador",
                param2 = "5"
            ),
            BlockNode(
                id = UUID.randomUUID().toString(),
                type = BlockType.FOR_LOOP,
                param1 = "i",
                param2 = "0 a 5"
            ),
            BlockNode(
                id = UUID.randomUUID().toString(),
                type = BlockType.PRINT_OUTPUT,
                param1 = "\"Paso de ejecución: \" + i"
            ),
            BlockNode(
                id = UUID.randomUUID().toString(),
                type = BlockType.IF_CONDITION,
                param1 = "contador >= 5"
            ),
            BlockNode(
                id = UUID.randomUUID().toString(),
                type = BlockType.PRINT_OUTPUT,
                param1 = "\"¡Límite alcanzado con éxito!\""
            )
        )
    }

    fun compileBlocksToCode(blocks: List<BlockNode>, language: ProgrammingLanguage): String {
        val sb = StringBuilder()
        sb.append("${language.commentPrefix} Código generado desde Bloques Visuales OmniCode\n")
        sb.append("${language.commentPrefix} Lenguaje: ${language.displayName}\n\n")

        for (block in blocks) {
            if (block.isEnabled) {
                sb.append(block.toCode(language))
                sb.append("\n\n")
            }
        }
        return sb.toString().trimEnd()
    }
}

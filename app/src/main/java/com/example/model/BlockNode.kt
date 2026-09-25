package com.example.model

enum class BlockCategory(val label: String, val colorHex: Long) {
    VARIABLE("Variables", 0xFF10B981),      // Emerald Green
    CONTROL_FLOW("Control de Flujo", 0xFF3B82F6), // Blue
    LOOP("Bucles", 0xFF8B5CF6),              // Purple
    FUNCTION("Funciones", 0xFFEC4899),       // Pink
    MATH_LOGIC("Matemática y Lógica", 0xFFF59E0B), // Amber
    IO_CONSOLE("Entrada / Salida", 0xFF06B6D4),  // Cyan
    API_NETWORK("Red y APIs", 0xFF6366F1)     // Indigo
}

enum class BlockType(
    val category: BlockCategory,
    val defaultTitle: String,
    val defaultParam1: String,
    val defaultParam2: String,
    val defaultParam3: String
) {
    SET_VARIABLE(BlockCategory.VARIABLE, "Definir Variable", "nombre_var", "valor", "tipo"),
    IF_CONDITION(BlockCategory.CONTROL_FLOW, "Si Condición (If)", "x > 0", "verdadero", ""),
    IF_ELSE(BlockCategory.CONTROL_FLOW, "Si / Si No (If-Else)", "condicion", "bloque_si", "bloque_no"),
    WHILE_LOOP(BlockCategory.LOOP, "Mientras (While)", "contador < 10", "cuerpo", ""),
    FOR_LOOP(BlockCategory.LOOP, "Repetir (For)", "i", "0 a 10", "paso 1"),
    DEFINE_FUNCTION(BlockCategory.FUNCTION, "Declarar Función", "calcularTotal", "item, cantidad", "tipo_retorno"),
    CALL_FUNCTION(BlockCategory.FUNCTION, "Llamar Función", "calcularTotal", "precio, 3", "resultado"),
    PRINT_OUTPUT(BlockCategory.IO_CONSOLE, "Imprimir en Consola", "\"¡Hola OmniCode!\"", "", ""),
    MATH_OPERATION(BlockCategory.MATH_LOGIC, "Operación Matemática", "resultado", "a + b", ""),
    FETCH_API(BlockCategory.API_NETWORK, "Petición HTTP / API", "https://api.github.com/status", "GET", "respuesta"),
    RETURN_VALUE(BlockCategory.FUNCTION, "Retornar Valor", "resultado", "", "")
}

data class BlockNode(
    val id: String,
    val type: BlockType,
    val param1: String = type.defaultParam1,
    val param2: String = type.defaultParam2,
    val param3: String = type.defaultParam3,
    val isEnabled: Boolean = true,
    val note: String = ""
) {
    fun toCode(language: ProgrammingLanguage): String {
        return when (type) {
            BlockType.SET_VARIABLE -> when (language) {
                ProgrammingLanguage.PYTHON -> "$param1 = $param2"
                ProgrammingLanguage.JAVASCRIPT, ProgrammingLanguage.TYPESCRIPT -> "let $param1 = $param2;"
                ProgrammingLanguage.KOTLIN -> "val $param1 = $param2"
                ProgrammingLanguage.RUST -> "let mut $param1 = $param2;"
                ProgrammingLanguage.GO -> "$param1 := $param2"
                ProgrammingLanguage.CPP -> "auto $param1 = $param2;"
                ProgrammingLanguage.JAVA -> "var $param1 = $param2;"
                ProgrammingLanguage.SWIFT -> "var $param1 = $param2"
                ProgrammingLanguage.PHP -> "$$param1 = $param2;"
                ProgrammingLanguage.RUBY -> "$param1 = $param2"
                ProgrammingLanguage.DART -> "var $param1 = $param2;"
                ProgrammingLanguage.SQL -> "-- Variable simulada: SET @$param1 = $param2;"
                ProgrammingLanguage.HTML_CSS -> "<meta name=\"$param1\" content=\"$param2\">"
            }
            BlockType.IF_CONDITION -> when (language) {
                ProgrammingLanguage.PYTHON -> "if $param1:\n    # Ejecutar lógica cuando cumple\n    pass"
                ProgrammingLanguage.RUST, ProgrammingLanguage.GO -> "if $param1 {\n    // Ejecutar lógica cuando cumple\n}"
                ProgrammingLanguage.RUBY -> "if $param1\n  # Ejecutar lógica\nend"
                else -> "if ($param1) {\n    // Ejecutar lógica cuando cumple\n}"
            }
            BlockType.IF_ELSE -> when (language) {
                ProgrammingLanguage.PYTHON -> "if $param1:\n    # Rama verdadera\n    pass\nelse:\n    # Rama alternativa\n    pass"
                ProgrammingLanguage.RUBY -> "if $param1\n  # Rama verdadera\nelse\n  # Rama alternativa\nend"
                else -> "if ($param1) {\n    // Rama verdadera\n} else {\n    // Rama alternativa\n}"
            }
            BlockType.WHILE_LOOP -> when (language) {
                ProgrammingLanguage.PYTHON -> "while $param1:\n    # Iteración activa\n    pass"
                ProgrammingLanguage.RUBY -> "while $param1\n  # Iteración activa\nend"
                else -> "while ($param1) {\n    // Iteración activa\n}"
            }
            BlockType.FOR_LOOP -> when (language) {
                ProgrammingLanguage.PYTHON -> "for $param1 in range(10):\n    # Iterar rango\n    pass"
                ProgrammingLanguage.KOTLIN -> "for ($param1 in 0 until 10) {\n    // Iterar rango\n}"
                ProgrammingLanguage.RUST -> "for $param1 in 0..10 {\n    // Iterar rango\n}"
                ProgrammingLanguage.GO -> "for $param1 := 0; $param1 < 10; $param1++ {\n    // Iterar rango\n}"
                else -> "for (let $param1 = 0; $param1 < 10; $param1++) {\n    // Iterar rango\n}"
            }
            BlockType.DEFINE_FUNCTION -> when (language) {
                ProgrammingLanguage.PYTHON -> "def $param1($param2):\n    \"\"\"Docstring explicativo\"\"\"\n    return None"
                ProgrammingLanguage.KOTLIN -> "fun $param1($param2) {\n    // Implementación\n}"
                ProgrammingLanguage.RUST -> "fn $param1($param2) {\n    // Implementación\n}"
                ProgrammingLanguage.GO -> "func $param1($param2) {\n    // Implementación\n}"
                ProgrammingLanguage.CPP -> "void $param1($param2) {\n    // Implementación\n}"
                ProgrammingLanguage.SWIFT -> "func $param1($param2) {\n    // Implementación\n}"
                ProgrammingLanguage.RUBY -> "def $param1($param2)\n  # Implementación\nend"
                else -> "function $param1($param2) {\n    // Implementación\n}"
            }
            BlockType.CALL_FUNCTION -> when (language) {
                ProgrammingLanguage.PYTHON, ProgrammingLanguage.RUBY -> "$param1($param2)"
                else -> "$param1($param2);"
            }
            BlockType.PRINT_OUTPUT -> when (language) {
                ProgrammingLanguage.PYTHON -> "print($param1)"
                ProgrammingLanguage.KOTLIN, ProgrammingLanguage.JAVA -> "println($param1)"
                ProgrammingLanguage.JAVASCRIPT, ProgrammingLanguage.TYPESCRIPT -> "console.log($param1);"
                ProgrammingLanguage.RUST -> "println!(\"{}\", $param1);"
                ProgrammingLanguage.CPP -> "std::cout << $param1 << std::endl;"
                ProgrammingLanguage.GO -> "fmt.Println($param1)"
                ProgrammingLanguage.SWIFT -> "print($param1)"
                ProgrammingLanguage.PHP -> "echo $param1 . PHP_EOL;"
                ProgrammingLanguage.RUBY -> "puts $param1"
                ProgrammingLanguage.DART -> "print($param1);"
                ProgrammingLanguage.SQL -> "SELECT $param1;"
                ProgrammingLanguage.HTML_CSS -> "<p>$param1</p>"
            }
            BlockType.MATH_OPERATION -> when (language) {
                ProgrammingLanguage.PYTHON -> "$param1 = $param2"
                ProgrammingLanguage.KOTLIN -> "val $param1 = $param2"
                ProgrammingLanguage.RUST -> "let $param1 = $param2;"
                else -> "const $param1 = $param2;"
            }
            BlockType.FETCH_API -> when (language) {
                ProgrammingLanguage.PYTHON -> "response = requests.get(\"$param1\").json()"
                ProgrammingLanguage.JAVASCRIPT, ProgrammingLanguage.TYPESCRIPT -> "const response = await fetch(\"$param1\");\nconst data = await response.json();"
                ProgrammingLanguage.KOTLIN -> "val response = httpClient.get(\"$param1\")"
                ProgrammingLanguage.RUST -> "let response = reqwest::get(\"$param1\").await?.json::<Value>().await?;"
                ProgrammingLanguage.GO -> "resp, err := http.Get(\"$param1\")"
                else -> "// HTTP $param2 to $param1"
            }
            BlockType.RETURN_VALUE -> when (language) {
                ProgrammingLanguage.PYTHON, ProgrammingLanguage.RUBY -> "return $param1"
                else -> "return $param1;"
            }
        }
    }
}

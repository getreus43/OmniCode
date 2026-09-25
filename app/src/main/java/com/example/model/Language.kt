package com.example.model

enum class ProgrammingLanguage(
    val displayName: String,
    val extension: String,
    val keywords: List<String>,
    val commentPrefix: String,
    val testFramework: String,
    val badgeColorHex: Long
) {
    PYTHON(
        displayName = "Python",
        extension = ".py",
        keywords = listOf("def", "import", "class", "return", "if", "elif", "else", "for", "while", "in", "try", "except", "with", "as", "lambda", "True", "False", "None"),
        commentPrefix = "#",
        testFramework = "pytest",
        badgeColorHex = 0xFF3572A5
    ),
    KOTLIN(
        displayName = "Kotlin",
        extension = ".kt",
        keywords = listOf("fun", "val", "var", "class", "interface", "package", "import", "return", "if", "else", "when", "for", "while", "null", "true", "false", "data", "sealed", "suspend"),
        commentPrefix = "//",
        testFramework = "JUnit 5",
        badgeColorHex = 0xFFA97BFF
    ),
    JAVASCRIPT(
        displayName = "JavaScript",
        extension = ".js",
        keywords = listOf("function", "const", "let", "var", "return", "if", "else", "for", "while", "switch", "case", "async", "await", "import", "export", "class", "true", "false", "null"),
        commentPrefix = "//",
        testFramework = "Jest",
        badgeColorHex = 0xFFF1E05A
    ),
    TYPESCRIPT(
        displayName = "TypeScript",
        extension = ".ts",
        keywords = listOf("interface", "type", "function", "const", "let", "return", "if", "else", "for", "while", "async", "await", "import", "export", "class", "implements", "extends"),
        commentPrefix = "//",
        testFramework = "Vitest / Jest",
        badgeColorHex = 0xFF3178C6
    ),
    RUST(
        displayName = "Rust",
        extension = ".rs",
        keywords = listOf("fn", "let", "mut", "struct", "enum", "impl", "trait", "pub", "match", "if", "else", "loop", "while", "for", "return", "use", "mod", "true", "false"),
        commentPrefix = "//",
        testFramework = "Cargo Test",
        badgeColorHex = 0xFFDEA584
    ),
    CPP(
        displayName = "C++",
        extension = ".cpp",
        keywords = listOf("int", "float", "double", "char", "void", "class", "struct", "if", "else", "for", "while", "return", "include", "public", "private", "auto", "template", "namespace"),
        commentPrefix = "//",
        testFramework = "GoogleTest",
        badgeColorHex = 0xFFF34B7D
    ),
    GO(
        displayName = "Go",
        extension = ".go",
        keywords = listOf("func", "package", "import", "var", "const", "type", "struct", "interface", "return", "if", "else", "for", "range", "go", "chan", "select", "defer"),
        commentPrefix = "//",
        testFramework = "Go Test",
        badgeColorHex = 0xFF00ADD8
    ),
    JAVA(
        displayName = "Java",
        extension = ".java",
        keywords = listOf("public", "private", "protected", "class", "interface", "void", "static", "final", "return", "if", "else", "for", "while", "new", "import", "package"),
        commentPrefix = "//",
        testFramework = "JUnit 5",
        badgeColorHex = 0xFFB07219
    ),
    SWIFT(
        displayName = "Swift",
        extension = ".swift",
        keywords = listOf("func", "let", "var", "struct", "class", "guard", "if", "else", "for", "in", "while", "return", "import", "enum", "extension", "protocol"),
        commentPrefix = "//",
        testFramework = "XCTest",
        badgeColorHex = 0xFFF05138
    ),
    PHP(
        displayName = "PHP",
        extension = ".php",
        keywords = listOf("function", "class", "public", "private", "echo", "return", "if", "else", "foreach", "while", "namespace", "use", "new"),
        commentPrefix = "//",
        testFramework = "PHPUnit",
        badgeColorHex = 0xFF4F5D95
    ),
    RUBY(
        displayName = "Ruby",
        extension = ".rb",
        keywords = listOf("def", "end", "class", "module", "if", "elsif", "else", "while", "for", "return", "require", "puts", "true", "false", "nil"),
        commentPrefix = "#",
        testFramework = "RSpec",
        badgeColorHex = 0xFF701516
    ),
    DART(
        displayName = "Dart",
        extension = ".dart",
        keywords = listOf("void", "var", "final", "const", "class", "function", "return", "if", "else", "for", "while", "async", "await", "import"),
        commentPrefix = "//",
        testFramework = "Dart Test",
        badgeColorHex = 0xFF00B4AB
    ),
    SQL(
        displayName = "SQL",
        extension = ".sql",
        keywords = listOf("SELECT", "FROM", "WHERE", "INSERT", "INTO", "UPDATE", "DELETE", "JOIN", "INNER", "LEFT", "RIGHT", "GROUP", "BY", "ORDER", "HAVING", "CREATE", "TABLE", "DROP"),
        commentPrefix = "--",
        testFramework = "pgTAP",
        badgeColorHex = 0xFFE38C00
    ),
    HTML_CSS(
        displayName = "HTML / Web",
        extension = ".html",
        keywords = listOf("div", "span", "p", "h1", "h2", "table", "button", "input", "form", "style", "body", "head", "html", "class", "id", "script"),
        commentPrefix = "<!--",
        testFramework = "Cypress / Playwright",
        badgeColorHex = 0xFFE34C26
    );

    companion object {
        fun fromExtension(ext: String): ProgrammingLanguage {
            val cleanExt = if (ext.startsWith(".")) ext else ".$ext"
            return values().firstOrNull { it.extension.equals(cleanExt, ignoreCase = true) } ?: PYTHON
        }
    }
}

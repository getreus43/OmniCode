# OmniCode Studio: IDE Universal Visual y de Código

OmniCode Studio es un Entorno de Desarrollo Integrado (IDE) móvil de última generación diseñado para programar en múltiples lenguajes modernos. Combina un editor de código profesional con una interfaz de bloques visuales interactivos y arrastrables, asistencia inteligente mediante Gemini AI (con respaldo local offline), depuración paso a paso con inspección de memoria, control de versiones tipo Git, cifrado de datos de extremo a extremo (E2E) y traducción automática de código entre lenguajes con generación de pruebas unitarias.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Decisiones confirmadas por el usuario:**
> 1. **Nombre y marca**: `OmniCode Studio: IDE universal visual y de código`.
> 2. **Modo de edición predeterminado**: Modo híbrido con selector rápido y bidireccional entre vista de código tradicional y lienzo visual de bloques lógicos.
> 3. **Motor de IA y traducción**: Integración nativa con **Gemini AI** (`gemini-3.1-pro-preview` / `gemini-3.5-flash`) mediante API REST con clientes robustos, combinada con un motor heurístico y plantillas locales de respaldo para funcionamiento 100% offline.

---

## 1. Overview & Core Concept

### ¿Qué hace OmniCode Studio?
- **Soporte Multi-Lenguaje Universal**: Plantillas de sintaxis, resaltado, formateo y ejecución conceptual para Python, Kotlin, JavaScript, TypeScript, Rust, C++, Go, Java, Swift, PHP, Ruby, Dart, HTML/CSS y SQL.
- **Editor Híbrido Dual**:
  - *Vista de Código*: Numeración de líneas, paleta de colores para tokens, guías de indentación, minimapa interactivo, búsqueda/reemplazo y autocompletado asistido.
  - *Vista de Bloques Visuales*: Nodos visuales arrastrables y configurables (Variables, Control de Flujo, Bucles, Funciones, Llamadas de Red/API, Operaciones Matemáticas y Salida de Consola) que sincronizan su código en tiempo real con el editor de texto.
- **Asistente de Inteligencia Artificial (Gemini AI + Motor Local)**:
  - Explicación de código línea por línea y sugerencias de optimización.
  - Generación de código a partir de instrucciones en lenguaje natural.
  - Traductor de contexto inter-lenguajes: Convierte proyectos enteros (ej. de Python a Kotlin o Rust) preservando la lógica y creando automáticamente una suite de pruebas unitarias verificable.
- **Depurador Avanzado & Consola Interactiva**:
  - Configuración de puntos de interrupción (*breakpoints*), inspección de pila de llamadas (*call stack*), panel de variables locales en memoria y avance paso a paso (*Step Over*, *Step Into*, *Continue*).
  - Terminal integrada con salida simulada y diagnósticos automáticos de fallos.
- **Control de Versiones (VCS Integrado)**:
  - Repositorio local estilo Git con registro de commits, visor de diferencias (*visual diff*), gestión de ramas (*branches*) y resolución de conflictos.
- **Seguridad & Privacidad de Extremo a Extremo**:
  - Almacenamiento local cifrado con Room y claves AES-GCM; telemetría opcional y modo de aislamiento seguro (*Air-gap mode*).
- **Colaboración y Ecosistema de Integraciones**:
  - Conexión configurada para GitHub (Push/Pull de commits), Google Drive, OneDrive y notificaciones para Slack, además de exportación en ZIP y archivos de código fuente limpios.
- **Offline-First Robusto**:
  - Persistencia de proyectos, archivos y revisiones en base de datos Room local, con cola de sincronización en segundo plano al recuperar conectividad.

---

## 2. User Experience & Visual Design

### Flujo de Usuario Principal
1. **Pantalla de Inicio / Dashboard de Proyectos**:
   - Acceso rápido a proyectos recientes, selector de plantillas por lenguaje (Python, Kotlin, Web, Rust, C++, etc.), métricas de sincronización y estado de seguridad/cifrado.
   - Creación de nuevo proyecto o importación mediante Git/ZIP.
2. **Espacio de Trabajo / Editor Principal**:
   - Barra superior con selector de archivos abiertos en pestañas, botón de ejecución (*Run*), botón de depuración (*Debug*), menú de traducción y conmutador rápido **[Código | Bloques | Híbrido Split]**.
   - Barra lateral retráctil (*Explorer*) con árbol de carpetas, gestor de versiones Git, paleta de bloques lógicos e integraciones.
   - Panel inferior colapsable con Consola/Terminal, Inspector de Variables de Depuración y Asistente Gemini AI.
3. **Módulo Visual de Bloques**:
   - Lienzo intuitivo con tarjetas de nodos conectables con colores temáticos por categoría (Azul para Control, Verde para Variables, Púrpura para Funciones, Naranja para Entrada/Salida).
   - Generación instantánea de código en el panel adyacente conforme se modifican o añaden bloques.
4. **Traductor de Lenguajes & Suite de Pruebas**:
   - Modal contextual de traducción: Selección de lenguaje de destino, análisis de compatibilidad semántica, vista previa de código traducido y generación automática de tests unitarios (JUnit, PyTest, Jest, Rust Cargo tests).
5. **Configuración de Accesibilidad y Temas**:
   - Selector entre 5 temas visuales: *Cyberpunk Dark*, *Monokai Pro*, *Solarized Night*, *High Contrast Accessible* y *Light Studio*.
   - Controles de tamaño tipográfico, espaciado entre líneas, modo de alto contraste y soporte TalkBack.

### Identidad Visual y Paleta de Colores
- **Fondo Base**: `#0D1117` (Deep Dark IDE) / `#161B22` (Superficie de paneles).
- **Acentos Primarios**: `#58A6FF` (Cyan Eléctrico para sintaxis y acciones primarias), `#7EE787` (Verde Neón para estados exitosos y variables).
- **Acentos Secundarios**: `#D2A8FF` (Púrpura para palabras clave e IA), `#FFA657` (Ámbar para advertencias y flujo de control).
- **Superficie de Bloques**: Tarjetas elevadas con bordes sutiles de 1.5dp, esquinas redondeadas de 12dp e íconos identificadores Material 3.

---

## 3. Key Product Decisions & Trade-Offs

### 1. Dualidad Editor de Texto + Bloques Visuales
- *Enfoque*: Implementar un parser/generador de nodos bidireccional ligero que traduce estructuras de bloques a código estándar y viceversa, permitiendo a principiantes construir lógica sin errores sintácticos y a desarrolladores avanzados inspeccionar o editar el código fuente directamente.
- *Beneficio*: Cumple el requisito de interfaz para expertos y principiantes sin sacrificar la portabilidad del código.

### 2. IA Híbrida: Gemini Cloud REST API + Motor Local de Respaldo
- *Enfoque*: El cliente consulta Gemini AI (`gemini-3.5-flash` para respuestas rápidas de autocompletado y `gemini-3.1-pro-preview` para refactorización profunda y traducción de proyectos). Si no hay conexión o no hay API Key configurada, el motor local recurre a generadores de plantillas AST y heurísticas semánticas integradas.
- *Beneficio*: Cero tiempo de inactividad; experiencia funcional en aviones, entornos sin red o modo confidencial de alta privacidad.

### 3. Persistencia Local con Room y Cifrado
- *Enfoque*: Base de datos Room con entidades para `Project`, `SourceFile`, `GitCommit`, `SyncQueueItem` y `BlockNode`. Los contenidos de código pueden almacenarse cifrados con clave local mediante AES-GCM.
- *Beneficio*: Rendimiento instantáneo de lectura/escritura, respuesta offline inmediata y garantía de privacidad.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        OmniCode Studio UI                              │
├────────────────────────────────┬───────────────────────────────────────┤
│  TopAppBar & Action Toolbar    │  Language Selector & Mode Switch      │
├────────────────────────────────┴───────────────────────────────────────┤
│ ┌───────────────────────┐ ┌──────────────────────────────────────────┐ │
│ │ Project Tree Explorer │ │ Workspace:                               │ │
│ │ & Git VCS Sidebar     │ │   • Text Code Editor (Syntax Highlight)  │ │
│ │                       │ │   • Visual Block Canvas (Drag & Drop)    │ │
│ └───────────────────────┘ └──────────────────────────────────────────┘ │
├────────────────────────────────────────────────────────────────────────┤
│ Bottom Dock: Console Output | Debugger Watch | Gemini AI Assistant    │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        OmniCode ViewModel                              │
├───────────────────┬───────────────────┬────────────────────────────────┤
│ CodeEditorState   │ BlockCanvasState  │ DebuggerEngine & VCSManager    │
└───────────────────┴───────────────────┴────────────────────────────────┘
                                    │
          ┌─────────────────────────┼────────────────────────┐
          ▼                         ▼                        ▼
┌──────────────────┐      ┌──────────────────┐     ┌─────────────────────┐
│  GeminiApiClient │      │ Local AST Engine │     │ Room Database (AES) │
│  (Cloud AI REST) │      │ (Offline Engine) │     │ Projects / Commits  │
└──────────────────┘      └──────────────────┘     └─────────────────────┘
```

### Entidades de Datos Principales
- **`ProjectEntity`**: ID, nombre, lenguaje principal, fecha de modificación, estado de sincronización, hash de cifrado.
- **`SourceFileEntity`**: ID del proyecto, ruta relativa, contenido en texto, representación en nodos de bloques JSON, lenguaje.
- **`GitCommitEntity`**: Hash del commit, mensaje, autor, marca de tiempo, árbol de diferencias (*diff patch*).
- **`DebugSession`**: Puntos de interrupción (*breakpoints*), variables inspeccionadas, estado de ejecución (Pausado, En ejecución, Error).
- **`CloudSyncConfig`**: Configuraciones de enlace con GitHub, Google Drive, OneDrive y webhooks de Slack.

package com.example.ai

import com.example.BuildConfig
import com.example.model.ProgrammingLanguage
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @property:Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @property:Json(name = "parts") val parts: List<GeminiPart>,
    @property:Json(name = "role") val role: String? = "user"
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @property:Json(name = "contents") val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @property:Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @property:Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        })
        .build()

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }
}

class GeminiCodeService {

    suspend fun askGemini(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(IllegalStateException("No API key configured"))
        }

        try {
            val req = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt))
                    )
                )
            )
            val response = GeminiClient.api.generateContent(apiKey, req)
            val output = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Sin respuesta del modelo."
            Result.success(output)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun explainCode(code: String, language: ProgrammingLanguage): Result<String> {
        val prompt = """
            Eres el asistente senior de código para OmniCode Studio.
            Explica detalladamente la lógica, complejidad y posibles mejoras del siguiente código en ${language.displayName}:
            
            ```${language.displayName.lowercase()}
            $code
            ```
            
            Responde en español de forma clara, con viñetas para fácil lectura móvil.
        """.trimIndent()
        return askGemini(prompt)
    }

    suspend fun translateCodeWithTests(
        code: String,
        fromLanguage: ProgrammingLanguage,
        toLanguage: ProgrammingLanguage
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val prompt = """
            Eres un compilador y traductor de código universal para OmniCode Studio.
            Traduce el siguiente código de ${fromLanguage.displayName} a ${toLanguage.displayName}.
            Garantiza:
            1. Equivalencia lógica exacta y modismos modernos del lenguaje destino.
            2. Genera una suite completa de pruebas unitarias usando ${toLanguage.testFramework}.
            
            Formato de salida OBLIGATORIO:
            ===TRANSLATED_CODE===
            (código traducido aquí sin markdown)
            ===UNIT_TESTS===
            (código de tests unitarios aquí sin markdown)
            
            Código original:
            ```
            $code
            ```
        """.trimIndent()

        val result = askGemini(prompt)
        result.map { rawText ->
            val codePart = rawText.substringAfter("===TRANSLATED_CODE===").substringBefore("===UNIT_TESTS===").trim()
            val testsPart = rawText.substringAfter("===UNIT_TESTS===").trim()
            if (codePart.isNotBlank() && testsPart.isNotBlank()) {
                Pair(codePart, testsPart)
            } else {
                Pair(rawText, "// Tests generados:\n// ${toLanguage.testFramework}")
            }
        }
    }

    suspend fun diagnoseError(code: String, errorLog: String, language: ProgrammingLanguage): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val prompt = """
            Depurador de OmniCode Studio:
            Analiza el siguiente error ocurrido al ejecutar código en ${language.displayName}.
            Código:
            ```
            $code
            ```
            Error / Stacktrace:
            $errorLog
            
            Formato de respuesta:
            ===DIAGNOSIS===
            (Explicación concisa de la causa raíz en español)
            ===SUGGESTED_FIX===
            (Código corregido o paso exacto para solucionarlo)
        """.trimIndent()

        val result = askGemini(prompt)
        result.map { rawText ->
            val diagnosis = rawText.substringAfter("===DIAGNOSIS===").substringBefore("===SUGGESTED_FIX===").trim()
            val fix = rawText.substringAfter("===SUGGESTED_FIX===").trim()
            Pair(diagnosis.ifBlank { rawText }, fix.ifBlank { "Verificar tipos y límites de memoria." })
        }
    }
}

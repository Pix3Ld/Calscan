package com.example.caloriecounter.domain

import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.example.caloriecounter.data.InputType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

interface MealAnalyzer {
    suspend fun analyze(input: AnalysisInput): AnalysisResult
}

data class AnalysisInput(
    val type: InputType,
    val text: String?,
    val imageBytes: ByteArray?,
    val note: String? = null
)

data class AnalysisResult(
    val title: String,
    val caloriesKcal: Int,
    val proteinGrams: Float,
    val carbohydratesGrams: Float,
    val fatGrams: Float,
    val fiberGrams: Float,
    val vitaminsJson: String
)

class GemmaMealAnalyzer(
    private val modelPath: String,
    private val cacheDirectory: String
) : MealAnalyzer {
    override suspend fun analyze(input: AnalysisInput): AnalysisResult {
        require(input.type == InputType.PHOTO) { "Gemma meal analysis currently requires a photo" }
        val imageBytes = requireNotNull(input.imageBytes) { "Meal photo is missing" }
        require(imageBytes.isNotEmpty()) { "Meal photo is empty" }
        check(File(modelPath).isFile) {
            "Gemma model is not installed. Place gemma-4-E4B-it.litertlm in the app models directory."
        }

        val prompt = buildString {
            append("Analyze this meal photo and estimate its nutrition. ")
            input.note?.takeIf { it.isNotBlank() }?.let { append("User context: ${it.trim()}. ") }
            append(
                "Return only a JSON object with string title, integer calories_kcal, " +
                    "numeric protein_grams, carbohydrates_grams, fat_grams, fiber_grams, " +
                    "and vitamins as an array of strings. Use non-negative estimates in grams."
            )
        }

        val rawResult = Engine(
            EngineConfig(
                modelPath = modelPath,
                backend = Backend.CPU(),
                visionBackend = Backend.CPU(),
                cacheDir = cacheDirectory
            )
        ).use { engine ->
            engine.initialize()
            engine.createConversation().use { conversation ->
                conversation.sendMessage(
                    Contents.of(Content.ImageBytes(imageBytes), Content.Text(prompt))
                ).contents.contents
                    .filterIsInstance<Content.Text>()
                    .joinToString(separator = "") { it.text }
            }
        }
        return MealAnalysisResultParser.parse(rawResult)
    }
}

internal object MealAnalysisResultParser {
    fun parse(rawResult: String): AnalysisResult {
        val jsonString = extractJson(rawResult)
        val json = JSONObject(jsonString)

        val title = json.optString("title", "").trim().ifEmpty {
            json.optString("name", "").trim().ifEmpty { "Posiłek" }
        }

        val caloriesRaw = json.opt("calories_kcal") ?: json.opt("calories") ?: json.opt("kcal")
        val caloriesNum = when (caloriesRaw) {
            is Number -> caloriesRaw.toDouble()
            is String -> caloriesRaw.toDoubleOrNull()
            else -> null
        } ?: error("calories_kcal must be a number")
        val caloriesKcal = Math.round(caloriesNum).toInt().coerceAtLeast(0)

        fun nutrient(vararg names: String): Float {
            var raw: Any? = null
            for (name in names) {
                if (json.has(name)) {
                    raw = json.get(name)
                    break
                }
            }
            val num: Float = when (raw) {
                is Number -> raw.toFloat()
                is String -> raw.toFloatOrNull() ?: 0f
                else -> 0f
            }
            require(num.isFinite() && num >= 0f) { "${names.first()} must be a non-negative number" }
            return num
        }

        val vitaminsRaw = json.opt("vitamins")
        val vitaminNames = JSONArray()
        when (vitaminsRaw) {
            is JSONArray -> {
                for (index in 0 until vitaminsRaw.length()) {
                    val vitamin = vitaminsRaw.optString(index).trim()
                    if (vitamin.isNotEmpty()) {
                        vitaminNames.put(vitamin)
                    }
                }
            }
            is String -> {
                vitaminsRaw.split(",", ";").map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                    vitaminNames.put(it)
                }
            }
        }

        return AnalysisResult(
            title = title,
            caloriesKcal = caloriesKcal,
            proteinGrams = nutrient("protein_grams", "protein"),
            carbohydratesGrams = nutrient("carbohydrates_grams", "carbohydrates", "carbs"),
            fatGrams = nutrient("fat_grams", "fat"),
            fiberGrams = nutrient("fiber_grams", "fiber"),
            vitaminsJson = vitaminNames.toString()
        )
    }

    private fun extractJson(rawResult: String): String {
        val firstBrace = rawResult.indexOf('{')
        val lastBrace = rawResult.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace > firstBrace) {
            return rawResult.substring(firstBrace, lastBrace + 1)
        }
        throw IllegalArgumentException("Could not find JSON object in model result: $rawResult")
    }
}

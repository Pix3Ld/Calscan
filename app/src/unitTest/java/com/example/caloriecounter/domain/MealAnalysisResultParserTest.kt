package com.example.caloriecounter.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class MealAnalysisResultParserTest {
    @Test
    fun parsesNutritionJson() {
        val result = MealAnalysisResultParser.parse(
            """{"title":"Tomato salad","calories_kcal":120,"protein_grams":3.5,"carbohydrates_grams":12,"fat_grams":6,"fiber_grams":4,"vitamins":["C","K"]}"""
        )

        assertEquals("Tomato salad", result.title)
        assertEquals(120, result.caloriesKcal)
        assertEquals(3.5f, result.proteinGrams)
        assertEquals("[\"C\",\"K\"]", result.vitaminsJson)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeNutrients() {
        MealAnalysisResultParser.parse(
            """{"title":"Meal","calories_kcal":120,"protein_grams":-1,"carbohydrates_grams":12,"fat_grams":6,"fiber_grams":4,"vitamins":[]}"""
        )
    }
}
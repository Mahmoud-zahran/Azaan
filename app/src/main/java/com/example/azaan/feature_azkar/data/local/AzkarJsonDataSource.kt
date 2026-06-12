package com.example.azaan.feature_azkar.data.local

import android.content.Context
import com.example.azaan.feature_azkar.domain.model.Zekr
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class ZekrJson(
    val id: Int,
    val title: String,
    val text: String,
    val repeat: Int,
    val reference: String = ""
)

@Singleton
class AzkarJsonDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val zekrList: List<Zekr> by lazy {
        val allZekrs = mutableListOf<Zekr>()
        val categories = listOf("morning", "evening")
        for (category in categories) {
            val json = loadJsonFromAssets("azkar/$category.json")
            val items: List<ZekrJson> = gson.fromJson(json, object : TypeToken<List<ZekrJson>>() {}.type)
            allZekrs.addAll(items.map { it.toZekr(category) })
        }
        allZekrs
    }

    fun getAllZekrs(): List<Zekr> = zekrList

    fun getZekrsByCategory(category: String): List<Zekr> =
        zekrList.filter { it.category == category }

    fun getAllCategories(): List<String> = listOf("morning", "evening")

    fun getCategoryDisplayName(category: String): String = when (category) {
        "morning" -> "أذكار الصباح"
        "evening" -> "أذكار المساء"
        else -> category
    }

    private fun loadJsonFromAssets(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }
}

private fun ZekrJson.toZekr(category: String) = Zekr(
    id = id,
    title = title,
    text = text,
    repeat = repeat,
    reference = reference,
    category = category
)

package com.nordisapps.nordisradiojournal.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

class TranslationRepository(private val context: Context) {
    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-3.5-flash")

    private val cacheKeyPref = stringPreferencesKey("translation_cache")

    private fun hashKey(texts: List<String>, targetLanguage: String): String {
        val raw = "$targetLanguage:${texts.joinToString("|")}"
        val digest = MessageDigest.getInstance("MD5").digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private suspend fun readCache(): JSONObject {
        val prefs = context.dataStore.data.first()
        val raw = prefs[cacheKeyPref] ?: "{}"
        return try {
            JSONObject(raw)
        } catch (e: Exception) {
            JSONObject()
        }
    }

    private suspend fun writeCacheEntry(key: String, translated: List<String>) {
        val current = readCache()
        current.put(key, JSONArray(translated))
        context.dataStore.edit { prefs ->
            prefs[cacheKeyPref] = current.toString()
        }
    }

    suspend fun translateBatch(texts: List<String>, targetLanguage: String): List<String> {
        if (texts.all { it.isBlank() } || targetLanguage == "en") return texts

        val key = hashKey(texts, targetLanguage)

        val cache = readCache()
        if (cache.has(key)) {
            val cachedArray = cache.getJSONArray(key)
            return (0 until cachedArray.length()).map { cachedArray.getString(it) }
        }

        return try {
            val jsonArray = JSONArray(texts)
            val prompt = "Translate each string in this JSON array to $targetLanguage. " +
                    "Keep the same tone, short and natural. " +
                    "Return ONLY a valid JSON array of translated strings, same order, same length, no extra text:\n\n$jsonArray"
            val response = model.generateContent(prompt)
            val rawText = response.text?.trim().orEmpty()
                .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val resultArray = JSONArray(rawText)
            val translated = (0 until resultArray.length()).map { resultArray.getString(it) }

            if (translated.size == texts.size) {
                writeCacheEntry(key, translated)
                translated
            } else {
                texts
            }
        } catch (e: Exception) {
            Log.w("TranslationRepository", "Batch translation failed", e)
            texts
        }
    }
}
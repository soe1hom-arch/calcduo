package com.calculator.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val updatedAt: Long
)

object NotesStore {
    private const val PREFS_NAME = "calcduo_prefs"
    private const val KEY_NOTES = "calcduo_notes_list"
    private const val KEY_LEGACY = "calcduo_notes"

    fun load(context: Context): MutableList<Note> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_NOTES, null)
        val notes = mutableListOf<Note>()
        if (!raw.isNullOrEmpty()) {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                notes.add(
                    Note(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        title = o.optString("title", ""),
                        content = o.optString("content", ""),
                        updatedAt = o.optLong("updatedAt", 0L)
                    )
                )
            }
        }
        if (notes.isEmpty()) {
            migrateLegacy(context, prefs, notes)
        }
        return notes
    }

    fun save(context: Context, notes: List<Note>) {
        val arr = JSONArray()
        notes.forEach { n ->
            arr.put(
                JSONObject()
                    .put("id", n.id)
                    .put("title", n.title)
                    .put("content", n.content)
                    .put("updatedAt", n.updatedAt)
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_NOTES, arr.toString())
        }
    }

    private fun migrateLegacy(context: Context, prefs: SharedPreferences, notes: MutableList<Note>) {
        val legacy = prefs.getString(KEY_LEGACY, null)
        if (legacy.isNullOrEmpty()) return
        val title = legacy.lineSequence().firstOrNull { it.isNotBlank() }?.trim()?.take(60) ?: ""
        notes.add(Note(UUID.randomUUID().toString(), title, legacy, System.currentTimeMillis()))
        prefs.edit { remove(KEY_LEGACY) }
        save(context, notes)
    }
}

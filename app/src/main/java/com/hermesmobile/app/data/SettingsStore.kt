package com.hermesmobile.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

data class ChatMessage(val role: String, val content: String)

data class Conversation(
    val id: String,
    val title: String,
    val messages: List<ChatMessage>,
    val updatedAt: Long,
)

/** Preferensi + penyimpanan obrolan lokal (SharedPreferences, JSON). */
class SettingsStore(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("hermes_mobile", Context.MODE_PRIVATE)

    var baseUrl by mutableStateOf(prefs.getString("baseUrl", "http://192.168.1.10:8642") ?: "http://192.168.1.10:8642")
        private set
    var apiKey by mutableStateOf(prefs.getString("apiKey", "") ?: "")
        private set
    var profile by mutableStateOf(prefs.getString("profile", "") ?: "")
        private set
    var streaming by mutableStateOf(prefs.getBoolean("streaming", true))
        private set
    /** 0 = sistem, 1 = terang, 2 = gelap */
    var darkMode by mutableStateOf(prefs.getInt("darkMode", 0))
        private set
    var accent by mutableStateOf(prefs.getString("accent", "Dynamic") ?: "Dynamic")
        private set
    var activeId by mutableStateOf(prefs.getString("activeId", "") ?: "")
        private set

    var conversations: List<Conversation> by mutableStateOf(loadConversations())
        private set

    fun putBaseUrl(v: String) { prefs.edit().putString("baseUrl", v).apply(); baseUrl = v }
    fun putApiKey(v: String) { prefs.edit().putString("apiKey", v).apply(); apiKey = v }
    fun putProfile(v: String) { prefs.edit().putString("profile", v).apply(); profile = v }
    fun putStreaming(v: Boolean) { prefs.edit().putBoolean("streaming", v).apply(); streaming = v }
    fun putDarkMode(v: Int) { prefs.edit().putInt("darkMode", v).apply(); darkMode = v }
    fun putAccent(v: String) { prefs.edit().putString("accent", v).apply(); accent = v }
    fun setActive(v: String) { prefs.edit().putString("activeId", v).apply(); activeId = v }

    fun loadConversations(): List<Conversation> {
        val raw = prefs.getString("conversations", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val msgs = JSONArray(o.optString("messages", "[]"))
                val list = (0 until msgs.length()).map { j ->
                    val m = msgs.getJSONObject(j)
                    ChatMessage(m.getString("role"), m.getString("content"))
                }
                Conversation(
                    id = o.getString("id"),
                    title = o.optString("title", "Obrolan baru"),
                    messages = list,
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    fun replaceConversations(list: List<Conversation>, persist: Boolean = true) {
        conversations = list
        if (persist) persistConversations()
    }

    fun persistConversations() {
        val arr = JSONArray()
        conversations.forEach { c ->
            val msgs = JSONArray()
            c.messages.forEach { m ->
                msgs.put(JSONObject().put("role", m.role).put("content", m.content))
            }
            arr.put(
                JSONObject()
                    .put("id", c.id)
                    .put("title", c.title)
                    .put("messages", msgs.toString())
                    .put("updatedAt", c.updatedAt),
            )
        }
        prefs.edit().putString("conversations", arr.toString()).apply()
    }
}

package com.hermesmobile.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed interface StreamEvent {
    data class Delta(val text: String) : StreamEvent
    data class Tool(val name: String) : StreamEvent
    data object Done : StreamEvent
    data class Error(val message: String) : StreamEvent
}

/** Klien untuk API server Hermes (OpenAI-compatible), port 8642. */
class HermesRepository(private val settings: SettingsStore) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)   // streaming SSE bisa lama
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private var currentCall: Call? = null

    private fun endpoint(path: String): String {
        var base = settings.baseUrl.trim().trimEnd('/')
        if (!base.contains("://")) base = "http://$base"
        val p = settings.profile.trim().trim('/')
        return if (p.isEmpty()) "$base/$path" else "$base/p/$p/$path"
    }

    suspend fun stream(
        messages: List<ChatMessage>,
        sessionId: String,
        onEvent: (StreamEvent) -> Unit,
    ) = withContext(Dispatchers.IO) {
        val msgArray = org.json.JSONArray()
        messages.forEach { m ->
            msgArray.put(JSONObject().put("role", m.role).put("content", m.content))
        }
        val payload = JSONObject()
            .put("model", "hermes-agent")
            .put("stream", settings.streaming)
            .put("messages", msgArray)

        val body = payload.toString().toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url(endpoint("v1/chat/completions"))
            .header("Authorization", "Bearer ${settings.apiKey}")
            .header("X-Hermes-Session-Id", sessionId)
            .post(body)
            .build()

        val call = client.newCall(req)
        currentCall = call
        try {
            call.execute().use { resp ->
                if (!resp.isSuccessful) {
                    val detail = resp.body?.string()?.take(280) ?: ""
                    onEvent(StreamEvent.Error("HTTP ${resp.code}: $detail"))
                    return@withContext
                }
                val source = resp.body!!.source()
                var finished = false
                while (true) {
                    val raw = source.readUtf8Line() ?: break
                    val line = raw.trim()
                    if (!line.startsWith("data:")) continue
                    val data = line.removePrefix("data:").trim()
                    if (data.isEmpty()) continue
                    if (data == "[DONE]") {
                        finished = true
                        onEvent(StreamEvent.Done)
                        break
                    }
                    val obj = try { JSONObject(data) } catch (_: Exception) { continue }

                    val kind = obj.optString("type", "")
                    if (kind.contains("tool") || data.contains("hermes.tool.progress")) {
                        val name = obj.optString("tool", obj.optString("name", "alat"))
                        onEvent(StreamEvent.Tool(name))
                        continue
                    }
                    val first = obj.optJSONArray("choices")?.optJSONObject(0)
                    val delta = first?.optJSONObject("delta")?.optString("content") ?: ""
                    val message = first?.optJSONObject("message")?.optString("content") ?: ""
                    val text = delta.ifEmpty { message }
                    if (text.isNotEmpty()) onEvent(StreamEvent.Delta(text))
                    if (!settings.streaming && text.isNotEmpty()) {
                        finished = true
                        onEvent(StreamEvent.Done)
                        break
                    }
                }
                if (!finished) onEvent(StreamEvent.Done)
            }
        } catch (e: Exception) {
            val msg = if (call.isCanceled()) "Dihentikan" else (e.message ?: "Gagal terhubung")
            onEvent(StreamEvent.Error(msg))
        } finally {
            currentCall = null
        }
    }

    suspend fun health(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url(endpoint("health"))
                .header("Authorization", "Bearer ${settings.apiKey}")
                .get()
                .build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) Result.success("Terhubung · HTTP ${resp.code}")
                else Result.failure(Exception("HTTP ${resp.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cancel() { currentCall?.cancel() }
}

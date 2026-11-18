package com.hermesmobile.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/** Stateholder aplikasi: obrolan aktif, streaming, dan aksi. */
class ChatVm(ctx: Context) {
    val settings = SettingsStore(ctx)
    private val repo = HermesRepository(settings)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val active: Conversation?
        get() = settings.conversations.firstOrNull { it.id == settings.activeId }

    var toolStatus by mutableStateOf<String?>(null)
        private set
    var banner by mutableStateOf<String?>(null)
        private set
    var isStreaming by mutableStateOf(false)
        private set

    init {
        if (settings.conversations.isEmpty() || active == null) newChat(open = true)
    }

    fun newChat(open: Boolean = true) {
        val c = Conversation(UUID.randomUUID().toString(), "Obrolan baru", emptyList(), System.currentTimeMillis())
        settings.replaceConversations(listOf(c) + settings.conversations)
        if (open) settings.setActive(c.id)
        banner = null
        toolStatus = null
    }

    fun openChat(id: String) {
        settings.setActive(id)
        banner = null
    }

    fun deleteChat(id: String) {
        val rest = settings.conversations.filterNot { it.id == id }
        settings.replaceConversations(rest)
        if (settings.activeId == id) {
            if (rest.isEmpty()) newChat(open = true) else settings.setActive(rest.first().id)
        }
    }

    fun send(text: String) {
        val prompt = text.trim()
        if (prompt.isEmpty() || isStreaming) return
        if (active == null) newChat(open = true)
        val base = active ?: return

        val updated = base.copy(
            title = if (base.messages.isEmpty()) prompt.take(36) else base.title,
            messages = base.messages + ChatMessage("user", prompt) + ChatMessage("assistant", ""),
            updatedAt = System.currentTimeMillis(),
        )
        replaceActive(updated, persist = false)
        isStreaming = true
        toolStatus = null
        banner = null

        val snapshot = updated.messages.dropLast(1).map { ChatMessage(it.role, it.content) }
        scope.launch {
            withContext(Dispatchers.IO) {
                repo.stream(snapshot, base.id) { ev -> scope.launch { handle(base.id, ev) } }
            }
        }
    }

    fun stop() { repo.cancel() }

    private fun handle(id: String, ev: StreamEvent) {
        when (ev) {
            is StreamEvent.Delta -> {
                val c = settings.conversations.firstOrNull { it.id == id } ?: return
                val list = c.messages.toMutableList()
                if (list.isEmpty() || list.last().role != "assistant") {
                    list.add(ChatMessage("assistant", ""))
                }
                val last = list.last()
                list[list.lastIndex] = last.copy(content = last.content + ev.text)
                replaceActive(c.copy(messages = list, updatedAt = System.currentTimeMillis()), persist = false)
            }
            is StreamEvent.Tool -> toolStatus = "Menjalankan ${ev.name}…"
            is StreamEvent.Done -> {
                isStreaming = false
                toolStatus = null
                settings.persistConversations()
            }
            is StreamEvent.Error -> {
                isStreaming = false
                toolStatus = null
                banner = ev.message
                val c = settings.conversations.firstOrNull { it.id == id } ?: return
                val list = c.messages.toMutableList()
                if (list.isEmpty() || list.last().role != "assistant") {
                    list.add(ChatMessage("assistant", ""))
                }
                val last = list.last()
                list[list.lastIndex] = last.copy(content = last.content + "\n\n⚠️ ${ev.message}")
                replaceActive(c.copy(messages = list), persist = true)
            }
        }
    }

    private fun replaceActive(c: Conversation, persist: Boolean) {
        settings.replaceConversations(
            settings.conversations.map { if (it.id == c.id) c else it },
            persist,
        )
    }

    fun testConnection(onResult: (String) -> Unit) {
        scope.launch {
            val result = repo.health()
            onResult(result.fold({ it }, { "Gagal: ${it.message ?: "tidak terhubung"}" }))
        }
    }
}

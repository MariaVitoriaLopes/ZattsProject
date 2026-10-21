package com.zattsproject.mobile.Network

import com.zattsproject.mobile.Model.Consumo // Import do seu model
import com.google.gson.Gson
import okhttp3.*
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

class SseClient(private val onConsumoRecebido: (Consumo) -> Unit) {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // Mantém a conexão SSE aberta
        .build()

    private var eventSource: EventSource? = null
    private val gson = Gson()

    fun iniciarConexao(urlBackend: String) {
        val request = Request.Builder()
            .url(urlBackend)
            .header("Accept", "text/event-stream")
            .build()

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                println("Conexão SSE aberta com o Backend!")
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    // Corrigido: Passagem explícita de Consumo::class.java
                    val consumo = gson.fromJson(data, Consumo::class.java)
                    if (consumo != null) {
                        onConsumoRecebido(consumo)
                    }
                } catch (e: Exception) {
                    println("Erro ao converter JSON do SSE: ${e.message}")
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                println("Erro na conexão SSE: ${t?.message}")
            }

            override fun onClosed(eventSource: EventSource) {
                println("Conexão SSE fechada.")
            }
        }

        eventSource = EventSources.createFactory(client).newEventSource(request, listener)
    }

    fun fecharConexao() {
        eventSource?.cancel()
    }
}

package com.zattsproject.mobile.Network

import android.util.Log
import com.zattsproject.mobile.Model.Consumo
import com.google.gson.Gson
import okhttp3.*
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

class SseClient(private val onConsumoRecebido: (Consumo) -> Unit) {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
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
                Log.d("GRAFICO_SSE", "Conexão SSE aberta com o Backend!")
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                try {
                    // 1. Verifica o JSON original recebido
                    Log.d("GRAFICO_SSE", "JSON RECEBIDO: $data")

                    // 2. Converte para o model Consumo
                    val consumo = gson.fromJson(data, Consumo::class.java)

                    if (consumo != null) {

                        // 3. Verifica os valores convertidos
                        Log.d(
                            "GRAFICO_SSE",
                            """
                            ===== LEITURA SSE =====
                            ID: ${consumo.id}
                            Consumo: ${consumo.consumoKwh} kWh
                            Potência: ${consumo.potenciaWatts} W
                            Corrente: ${consumo.correnteAmperes} A
                            Tensão: ${consumo.voltagem} V
                            Data: ${consumo.dataHora}
                            =======================
                            """.trimIndent()
                        )

                        // 4. Mantém o encaminhamento original
                        onConsumoRecebido(consumo)
                    }

                } catch (e: Exception) {
                    Log.e(
                        "GRAFICO_SSE",
                        "Erro ao converter JSON do SSE",
                        e
                    )
                }
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?
            ) {
                Log.e(
                    "GRAFICO_SSE",
                    "Falha na conexão SSE. HTTP: ${response?.code}",
                    t
                )
            }

            override fun onClosed(eventSource: EventSource) {
                Log.d("GRAFICO_SSE", "Conexão SSE fechada.")
            }
        }

        eventSource = EventSources.createFactory(client)
            .newEventSource(request, listener)
    }

    fun fecharConexao() {
        eventSource?.cancel()
    }
}

package com.zattsproject.mobile

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zattsproject.mobile.Model.Consumo
import com.zattsproject.mobile.Model.DashboardDTO
import com.zattsproject.mobile.Network.RetrofitClient
import com.zattsproject.mobile.Network.SseClient
import com.zattsproject.mobile.View.GraficoConsumoView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.TreeMap

class HomeActivity : AppCompatActivity() {

    private var corAplicada = ""

    private lateinit var txvHorario: TextView
    private lateinit var txvData: TextView

    private lateinit var btnTempoReal: TextView
    private lateinit var btnConsumo: TextView
    private lateinit var btnValorConta: TextView

    private lateinit var txvTituloGrafico: TextView
    private lateinit var txvSubtituloGrafico: TextView

    private lateinit var cardMetaMensal: View
    private lateinit var grafico: GraficoConsumoView

    private var sseClient: SseClient? = null
    private var dashboardDados: DashboardDTO? = null

    private var consumoDiarioAtualKwh = 0.0
    private val tarifaKwhEstimada = 0.85

    private val handler = Handler(Looper.getMainLooper())

    // Histórico de consumo acumulado por minuto.
    private val consumoPorMinuto = TreeMap<Long, Double>()

    // Histórico de consumo acumulado por hora via SSE.
    private val consumoPorHoraSse = TreeMap<Long, Double>()

    private var abaAtual = ABA_TEMPO_REAL

    companion object {
        private const val ABA_TEMPO_REAL = 0
        private const val ABA_CONSUMO = 1
        private const val ABA_CONTA = 2

        private const val INTERVALO_MINUTOS = 5
        private const val LIMITE_HORAS = 10
        private const val LIMITE_MESES = 3

        private const val TAG_GRAFICO = "GRAFICO_CONSUMO"
        private val ZONA_HORARIA = ZoneId.of("America/Sao_Paulo")
    }

    private val formatoHora = SimpleDateFormat(
        "HH:mm",
        Locale("pt", "BR")
    ).apply {
        timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
    }

    private val atualizarRelogio = object : Runnable {
        override fun run() {
            atualizarDataHora()
            handler.postDelayed(this, 1000)
        }
    }

    private val atualizarJanelaMinutos = object : Runnable {
        override fun run() {
            if (abaAtual == ABA_TEMPO_REAL) {
                mostrarGraficoTempoReal()
            }

            agendarAtualizacaoMinuto()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        txvHorario = findViewById(R.id.txv_horario)
        txvData = findViewById(R.id.txv_data)

        btnTempoReal = findViewById(R.id.btn_tempoReal)
        btnConsumo = findViewById(R.id.btn_consumo)
        btnValorConta = findViewById(R.id.btn_valorConta)

        txvTituloGrafico = findViewById(R.id.txv_tituloGrafico)
        txvSubtituloGrafico = findViewById(R.id.txv_subtituloGrafico)

        cardMetaMensal = findViewById(R.id.card_metaMensal)
        grafico = findViewById(R.id.graficoConsumo)

        configurarBotoes()

        carregarDadosDashboardBackend()
        iniciarConexaoSse()
    }

    override fun onResume() {
        super.onResume()

        val corAtual = AparenciaManager.obterCor(this)

        if (corAplicada.isNotEmpty() && corAtual != corAplicada) {
            recreate()
            return
        }

        corAplicada = corAtual
    }

    // =====================================================
    // RELÓGIO
    // =====================================================

    private fun atualizarDataHora() {
        val agora = Date()
        val idioma = Locale("pt", "BR")
        val fusoSP = TimeZone.getTimeZone("America/Sao_Paulo")

        val formatoHoraCompleta = SimpleDateFormat(
            "HH:mm:ss",
            idioma
        ).apply {
            timeZone = fusoSP
        }

        val formatoData = SimpleDateFormat(
            "EEEE, dd 'de' MMMM 'de' yyyy",
            idioma
        ).apply {
            timeZone = fusoSP
        }

        txvHorario.text = formatoHoraCompleta.format(agora)

        val dataFormatada = formatoData.format(agora)

        txvData.text = dataFormatada.replaceFirstChar {
            it.titlecase(idioma)
        }
    }

    // =====================================================
    // CARREGAMENTO DO BACK-END
    // =====================================================

    private fun carregarDadosDashboardBackend() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val usuarioIdLogado = "6ae2afdbe3f4de30aa7d8f38"

                val resposta =
                    RetrofitClient.apiService.obterDadosDashboard(
                        usuarioIdLogado
                    )

                if (resposta.isSuccessful && resposta.body() != null) {

                    val dto = resposta.body()!!

                    Log.d(
                        "GRAFICO_HORAS",
                        "Quantidade de horas recebidas: ${dto.consumoDiarioHoras.size}"
                    )

                    dto.consumoDiarioHoras.forEach {
                        Log.d(
                            "GRAFICO_HORAS",
                            "Hora: ${it.hora} | Consumo: ${it.consumoKwh} kWh"
                        )
                    }

                    withContext(Dispatchers.Main) {

                        dashboardDados = dto

                        consumoDiarioAtualKwh =
                            dto.consumoTotalDiarioKwh

                        sincronizarHistoricoTempoReal(dto)

                        atualizarIndicadoresComDadosReais(dto)

                        when (abaAtual) {
                            ABA_TEMPO_REAL -> mostrarTempoReal()
                            ABA_CONSUMO -> mostrarConsumo()
                            ABA_CONTA -> mostrarValorConta()
                        }
                    }

                } else {
                    Log.e(
                        "HOME_API",
                        "Erro ao carregar dados: ${resposta.code()}"
                    )
                }

            } catch (e: Exception) {
                Log.e(
                    "HOME_API",
                    "Falha de conexão na API: ${e.message}",
                    e
                )
            }
        }
    }

    // =====================================================
    // HISTÓRICO DO GRÁFICO EM TEMPO REAL
    // =====================================================

    private fun obterMinutoAtual(): Long {
        val calendario = Calendar.getInstance(
            TimeZone.getTimeZone("America/Sao_Paulo")
        )

        calendario.set(Calendar.SECOND, 0)
        calendario.set(Calendar.MILLISECOND, 0)

        return calendario.timeInMillis
    }

    private fun converterHoraParaMinuto(hora: String?): Long? {

        if (hora.isNullOrBlank()) return null

        return try {

            val instante = when {

                hora.endsWith("Z", ignoreCase = true) -> {
                    Instant.parse(hora)
                }

                Regex("[+-]\\d{2}:?\\d{2}$")
                    .containsMatchIn(hora) -> {

                    OffsetDateTime.parse(hora).toInstant()
                }

                hora.contains("T") -> {

                    LocalDateTime.parse(
                        hora,
                        DateTimeFormatter.ISO_DATE_TIME
                    ).atZone(ZONA_HORARIA).toInstant()
                }

                else -> {

                    val horario = hora.takeLast(5)
                    val partes = horario.split(":")

                    if (partes.size != 2) return null

                    val horaInt = partes[0].toInt()
                    val minutoInt = partes[1].toInt()

                    if (horaInt !in 0..23 || minutoInt !in 0..59) {
                        return null
                    }

                    var dataHora = LocalDate.now(ZONA_HORARIA)
                        .atTime(horaInt, minutoInt)
                        .atZone(ZONA_HORARIA)

                    if (
                        dataHora.toInstant().toEpochMilli() >
                        obterMinutoAtual() + 60_000L
                    ) {
                        dataHora = dataHora.minusDays(1)
                    }

                    dataHora.toInstant()
                }
            }

            val milissegundos = instante.toEpochMilli()

            Math.floorDiv(
                milissegundos,
                60_000L
            ) * 60_000L

        } catch (e: Exception) {

            Log.e(
                TAG_GRAFICO,
                "Erro ao converter horário: $hora",
                e
            )

            null
        }
    }

    private fun sincronizarHistoricoTempoReal(dto: DashboardDTO) {

        val pontos = dto.consumoTempoReal

        Log.d(
            TAG_GRAFICO,
            "Quantidade de pontos recebidos do dashboard: ${pontos.size}"
        )

        pontos.forEach { ponto ->

            val minuto = converterHoraParaMinuto(ponto.hora)

            if (minuto != null) {

                // Preserva uma leitura SSE já recebida.
                consumoPorMinuto.putIfAbsent(
                    minuto,
                    ponto.consumoKwh
                )

                Log.d(
                    TAG_GRAFICO,
                    "Histórico: ${ponto.hora} = ${ponto.consumoKwh} kWh"
                )

            } else {
                Log.w(
                    TAG_GRAFICO,
                    "Horário inválido recebido do dashboard: ${ponto.hora}"
                )
            }
        }

        limparHistoricoAntigo()

        Log.d(
            TAG_GRAFICO,
            "Histórico após sincronização: $consumoPorMinuto"
        )
    }

    private fun limparHistoricoAntigo() {

        val inicio = obterMinutoAtual() -
                (INTERVALO_MINUTOS - 1) * 60_000L

        consumoPorMinuto.keys
            .filter { it < inicio }
            .forEach {
                consumoPorMinuto.remove(it)
            }
    }

    private fun mostrarGraficoTempoReal() {

        val agora = obterMinutoAtual()

        val valores = mutableListOf<Float>()
        val horarios = mutableListOf<String>()

        // Exibe os cinco minutos, incluindo o atual.
        for (i in INTERVALO_MINUTOS - 1 downTo 0) {

            val minuto = agora - i * 60_000L

            val consumo = consumoPorMinuto[minuto] ?: 0.0

            horarios.add(
                formatoHora.format(Date(minuto))
            )

            valores.add(consumo.toFloat())
        }

        limparHistoricoAntigo()

        Log.d(
            TAG_GRAFICO,
            """
            ===== GRÁFICO EM TEMPO REAL =====
            Horários: $horarios
            Valores: $valores
            Histórico armazenado: $consumoPorMinuto
            =================================
            """.trimIndent()
        )

        grafico.atualizarGraficoLinha(
            valores,
            horarios
        )
    }

    // =====================================================
    // ATUALIZAÇÃO AUTOMÁTICA DA JANELA
    // =====================================================

    private fun agendarAtualizacaoMinuto() {

        handler.removeCallbacks(atualizarJanelaMinutos)

        val agora = System.currentTimeMillis()

        val proximoMinuto =
            60_000L - (agora % 60_000L) + 100L

        handler.postDelayed(
            atualizarJanelaMinutos,
            proximoMinuto
        )
    }

    // =====================================================
    // SSE
    // =====================================================


    private fun iniciarConexaoSse() {

        sseClient = SseClient { consumoRecebido ->
            runOnUiThread {
                processarNovaLeituraSse(consumoRecebido)
            }
        }

        val urlSse = "${BuildConfig.BASE_URL}api/v1/web/dashboard/sse"

        sseClient?.iniciarConexao(urlSse)
    }


    private fun processarNovaLeituraSse(consumo: Consumo) {

        Log.d(
            "GRAFICO_SSE",
            """
    Consumo recebido: ${consumo.consumoKwh}
    Potência: ${consumo.potenciaWatts}
    Corrente: ${consumo.correnteAmperes}
    Tensão: ${consumo.voltagem}
    Data: ${consumo.dataHora}
    """.trimIndent()
        )

        val deltaKwh = consumo.consumoKwh.coerceAtLeast(0.0)

        consumoDiarioAtualKwh += deltaKwh

        // Identifica o minuto da leitura.
        val minuto = converterHoraParaMinuto(consumo.dataHora)
            ?: obterMinutoAtual()

        val agora = obterMinutoAtual()

        val inicioJanela = agora -
                (INTERVALO_MINUTOS - 1) * 60_000L

        // Acumula as leituras recebidas no mesmo minuto.
        consumoPorMinuto[minuto] =
            (consumoPorMinuto[minuto] ?: 0.0) + deltaKwh

        // Identifica a hora da leitura recebida.
        val hora = Math.floorDiv(
            minuto,
            3_600_000L
        ) * 3_600_000L

        // Acumula o consumo recebido pelo SSE naquela hora.
        consumoPorHoraSse[hora] =
            (consumoPorHoraSse[hora] ?: 0.0) + deltaKwh

        Log.d(
            TAG_GRAFICO,
            """
            ===== ACUMULAÇÃO =====
            Minuto convertido: ${Date(minuto)}
            Minuto atual: ${Date(agora)}
            Dentro da janela: ${minuto >= inicioJanela && minuto <= agora}
            Consumo acumulado: ${consumoPorMinuto[minuto]}
            ======================
            """.trimIndent()
        )

        limparHistoricoAntigo()

        val txvConsumoDiario =
            findViewById<TextView>(R.id.txv_consumoDiario)

        val txvCustoDiario =
            findViewById<TextView>(R.id.txv_custoDiario)

        txvConsumoDiario.text = String.format(
            Locale("pt", "BR"),
            "%.4f kWh",
            consumoDiarioAtualKwh
        )

        val custoEstimadoDiario =
            consumoDiarioAtualKwh * tarifaKwhEstimada

        txvCustoDiario.text = String.format(
            Locale("pt", "BR"),
            "R$ %.2f",
            custoEstimadoDiario
        )

        when (abaAtual) {
            ABA_TEMPO_REAL -> mostrarGraficoTempoReal()
            ABA_CONSUMO -> mostrarConsumo()
        }
    }

    // =====================================================
    // INDICADORES
    // =====================================================

    private fun atualizarIndicadoresComDadosReais(dto: DashboardDTO) {

        val consumoMensal = dto.metaConsumoMensalAtualKwh

        val metaMensal =
            if (dto.metaConsumoMensalLimiteKwh > 0)
                dto.metaConsumoMensalLimiteKwh
            else
                250.0

        val porcentagem = dto.percentualMetaAtingida.toInt()

        findViewById<TextView>(R.id.txv_consumoDiario).text =
            String.format(
                Locale("pt", "BR"),
                "%.2f kWh",
                dto.consumoTotalDiarioKwh
            )

        findViewById<TextView>(R.id.txv_custoDiario).text =
            String.format(
                Locale("pt", "BR"),
                "R$ %.2f",
                dto.custoDiarioReais
            )

        findViewById<TextView>(R.id.txv_metaGasto).text =
            String.format(
                Locale("pt", "BR"),
                "%.0f kWh",
                metaMensal
            )

        findViewById<TextView>(R.id.txv_economia).text =
            String.format(
                Locale("pt", "BR"),
                "R$ %.2f",
                dto.valorContaEstimadoMesReais
            )

        findViewById<TextView>(R.id.txv_metaDescricao).text =
            "↗ $porcentagem% da meta atingida"

        findViewById<TextView>(R.id.txv_consumoMensal).text =
            String.format(
                Locale("pt", "BR"),
                "%.1f kWh",
                consumoMensal
            )

        findViewById<TextView>(R.id.txv_metaMensal).text =
            String.format(
                Locale("pt", "BR"),
                "%.0f kWh",
                metaMensal
            )

        val txvStatusMeta =
            findViewById<TextView>(R.id.txv_statusMeta)

        val txvStatusDescricao =
            findViewById<TextView>(R.id.txv_statusDescricao)

        txvStatusMeta.text =
            dto.mensagemStatusMeta ?: "Você está dentro da sua meta!"

        txvStatusDescricao.text =
            "O consumo está em $porcentagem% da sua meta."

        val progressoMeta =
            findViewById<ProgressBar>(R.id.progressoMeta)

        val corSelecionada = AparenciaManager.corHex(this)

        progressoMeta.progressTintList =
            ColorStateList.valueOf(corSelecionada)

        progressoMeta.progress =
            porcentagem.coerceIn(0, 100)
    }

    // =====================================================
    // BOTÕES DOS GRÁFICOS
    // =====================================================

    private fun configurarBotoes() {

        btnTempoReal.setOnClickListener {
            mostrarTempoReal()
        }

        btnConsumo.setOnClickListener {
            mostrarConsumo()
        }

        btnValorConta.setOnClickListener {
            mostrarValorConta()
        }
    }

    private fun selecionarBotao(selecionado: TextView) {

        val botoes = listOf(
            btnTempoReal,
            btnConsumo,
            btnValorConta
        )

        botoes.forEach { botao ->
            botao.background = null
        }

        selecionado.setBackgroundResource(
            R.drawable.bg_tab_selecionada
        )
    }

    // =====================================================
    // GRÁFICO EM TEMPO REAL
    // =====================================================

    private fun mostrarTempoReal() {

        abaAtual = ABA_TEMPO_REAL

        selecionarBotao(btnTempoReal)

        cardMetaMensal.visibility = View.GONE

        txvTituloGrafico.text = "Consumo de energia atual"

        txvSubtituloGrafico.text =
            "Veja o consumo de energia em tempo real"

        mostrarGraficoTempoReal()
    }

    // =====================================================
    // GRÁFICO DE CONSUMO POR HORA
    // =====================================================


    private fun mostrarConsumo() {

        abaAtual = ABA_CONSUMO

        selecionarBotao(btnConsumo)

        cardMetaMensal.visibility = View.VISIBLE

        txvTituloGrafico.text = "Consumo de energia"

        txvSubtituloGrafico.text =
            "Veja o consumo de energia (kWh) por hora"

        // Recupera as últimas 10 horas disponibilizadas pelo Back-end.
        val pontos = dashboardDados
            ?.consumoDiarioHoras
            ?.takeLast(LIMITE_HORAS)
            ?: emptyList()

        val inicioHoraAtual = Math.floorDiv(
            System.currentTimeMillis(),
            3_600_000L
        ) * 3_600_000L

        val calendario = Calendar.getInstance(
            TimeZone.getTimeZone("America/Sao_Paulo")
        )
        val horaAtual = calendario.get(Calendar.HOUR_OF_DAY)

        val valores = pontos.map { ponto ->

            val horaPonto = ponto.hora
                .removeSuffix("h")
                .toIntOrNull()

            val consumoBackend = ponto.consumoKwh

            // Usa o acumulado SSE na hora atual, sem somá-lo ao Back-end.
            // Isso evita contar duas vezes leituras já gravadas no banco.
            val consumoFinal = if (horaPonto == horaAtual) {
                consumoPorHoraSse[inicioHoraAtual] ?: consumoBackend
            } else {
                consumoBackend
            }

            consumoFinal.toFloat()
        }

        val horas = pontos.map { it.hora }

        Log.d(
            "GRAFICO_HORAS",
            "Horários: $horas | Valores enviados: $valores | Histórico SSE: $consumoPorHoraSse"
        )

        grafico.atualizarGrafico(
            valores,
            horas
        )
    }

    // =====================================================
    // GRÁFICO DE ESTIMATIVA DA CONTA
    // =====================================================

    private fun mostrarValorConta() {

        abaAtual = ABA_CONTA

        selecionarBotao(btnValorConta)

        cardMetaMensal.visibility = View.GONE

        txvTituloGrafico.text = "Valor da conta (estimado)"

        txvSubtituloGrafico.text =
            "Comparativo dos últimos meses"

        val pontos = dashboardDados
            ?.valorContaEstimadoReais
            ?.takeLast(LIMITE_MESES)
            ?: emptyList()

        val valores = pontos.map {
            it.valor.toFloat()
        }

        val meses = pontos.map {
            it.mes
        }

        grafico.atualizarGrafico(
            valores,
            meses
        )
    }

    // =====================================================
    // CICLO DE VIDA
    // =====================================================

    override fun onStart() {
        super.onStart()

        handler.post(atualizarRelogio)
        agendarAtualizacaoMinuto()
    }

    override fun onStop() {

        handler.removeCallbacks(atualizarRelogio)
        handler.removeCallbacks(atualizarJanelaMinutos)

        super.onStop()
    }

    override fun onDestroy() {

        sseClient?.fecharConexao()

        super.onDestroy()
    }
}
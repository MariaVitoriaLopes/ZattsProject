package com.zattsproject.mobile

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zattsproject.mobile.Model.DashboardDTO
import com.zattsproject.mobile.Network.RetrofitClient
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

    private var dashboardDados: DashboardDTO? = null

    // Evita requisições sobrepostas quando o polling dispara de 5 em 5 s.
    private var requisicaoEmAndamento = false

    private val handler = Handler(Looper.getMainLooper())

    private val consumoPorMinuto = sortedMapOf<Long, Double>()

    private var abaAtual = ABA_TEMPO_REAL

    companion object {
        private const val ABA_TEMPO_REAL = 0
        private const val ABA_CONSUMO = 1
        private const val ABA_CONTA = 2

        // Mesma janela que o backend envia (10 minutos).
        private const val INTERVALO_MINUTOS = 5
        private const val LIMITE_HORAS = 12

        // Mês atual + 2 anteriores.
        private const val LIMITE_MESES = 3

        // Atualização do dashboard (igual ao web).
        private const val INTERVALO_ATUALIZACAO_MS = 5000L

        private const val TAG_GRAFICO = "GRAFICO_CONSUMO"
        private const val TAG_HOME = "HOME_API"

        private val ZONA_HORARIA =
            ZoneId.of("America/Sao_Paulo")
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

    private val atualizarDashboard = object : Runnable {
        override fun run() {
            carregarDadosDashboardBackend()
            handler.postDelayed(this, INTERVALO_ATUALIZACAO_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        carregarHistoricoConsumoLocal()
        inicializarComponentes()
        configurarBotoes()
        configurarBotaoEditarMeta()

        // O carregamento dos dados é disparado no onStart (polling).
    }

    private fun inicializarComponentes() {

        txvHorario = findViewById(R.id.txv_horario)
        txvData = findViewById(R.id.txv_data)

        btnTempoReal = findViewById(R.id.btn_tempoReal)
        btnConsumo = findViewById(R.id.btn_consumo)
        btnValorConta = findViewById(R.id.btn_valorConta)

        txvTituloGrafico =
            findViewById(R.id.txv_tituloGrafico)

        txvSubtituloGrafico =
            findViewById(R.id.txv_subtituloGrafico)

        cardMetaMensal =
            findViewById(R.id.card_metaMensal)

        grafico =
            findViewById(R.id.graficoConsumo)
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

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            carregarDadosDashboardBackend()
        }
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

        txvData.text = formatoData.format(agora)
            .replaceFirstChar {
                it.titlecase(idioma)
            }
    }

    // =====================================================
    // CARREGAMENTO DO BACK-END
    // =====================================================

    private fun carregarDadosDashboardBackend() {

        if (requisicaoEmAndamento) return

        requisicaoEmAndamento = true

        lifecycleScope.launch(Dispatchers.IO) {

            try {

                val usuarioIdLogado =
                    RetrofitClient.obterUsuarioId()

                Log.d(
                    TAG_HOME,
                    "USUÁRIO DO DASHBOARD: $usuarioIdLogado"
                )

                if (usuarioIdLogado.isNullOrBlank()) {

                    Log.e(
                        TAG_HOME,
                        "ID do usuário não encontrado na sessão."
                    )

                    return@launch
                }

                val resposta =
                    RetrofitClient.apiService.obterDadosDashboard(
                        usuarioIdLogado
                    )

                if (resposta.isSuccessful) {

                    val dto = resposta.body()

                    if (dto == null) {

                        Log.e(
                            TAG_HOME,
                            "Dashboard retornou resposta vazia."
                        )

                        return@launch
                    }

                    withContext(Dispatchers.Main) {

                        dashboardDados = dto

                        atualizarIndicadoresComDadosReais(dto)

                        Log.d(
                            TAG_HOME,
                            "DADOS RECEBIDOS DA API: ${dto.consumoTempoReal}"
                        )

                        when (abaAtual) {
                            ABA_TEMPO_REAL -> mostrarTempoReal()
                            ABA_CONSUMO -> mostrarConsumo()
                            ABA_CONTA -> mostrarValorConta()
                        }
                    }

                } else {

                    Log.e(
                        TAG_HOME,
                        "Erro HTTP ${resposta.code()}: ${resposta.message()}"
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    TAG_HOME,
                    "Falha ao carregar Dashboard.",
                    e
                )

            } finally {

                requisicaoEmAndamento = false
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

    private fun converterHoraParaMinuto(
        hora: String?
    ): Long? {

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

                    // LocalDateTime do Backend representa
                    // o horário local de São Paulo.
                    LocalDateTime.parse(
                        hora,
                        DateTimeFormatter.ISO_DATE_TIME
                    ).atZone(ZONA_HORARIA).toInstant()
                }

                else -> {

                    // Formato "HH:mm" enviado pelo backend.
                    val horario = hora.takeLast(5)
                    val partes = horario.split(":")

                    if (partes.size != 2) return null

                    val horaInt = partes[0].toInt()
                    val minutoInt = partes[1].toInt()

                    if (
                        horaInt !in 0..23 ||
                        minutoInt !in 0..59
                    ) {
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

    private fun sincronizarHistoricoTempoReal(
        dto: DashboardDTO
    ) {

        consumoPorMinuto.clear()

        dto.consumoTempoReal.forEach { ponto ->

            val minuto = converterHoraParaMinuto(ponto.hora)

            if (minuto != null) {
                consumoPorMinuto[minuto] = ponto.consumoKwh
            }
        }

        limparHistoricoAntigo()

        Log.d(
            TAG_GRAFICO,
            "Pontos recebidos: ${dto.consumoTempoReal.size} | " +
                    "Histórico sincronizado: $consumoPorMinuto"
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

        val pontos = dashboardDados
            ?.consumoTempoReal
            ?: emptyList()

        val ultimoPonto = pontos.lastOrNull()

        // Identifica o minuto mais recente recebido da API.
        val minutoReferencia = ultimoPonto
            ?.hora
            ?.let { converterHoraParaMinuto(it) }
            ?: obterMinutoAtual()

        // Recupera medições anteriores que a API disponibilizar.
        // Não sobrescreve valores locais com zeros do histórico.
        pontos.dropLast(1).forEach { ponto ->

            val minuto = converterHoraParaMinuto(ponto.hora)
                ?: return@forEach

            if (ponto.consumoKwh > 0.0) {
                consumoPorMinuto[minuto] = maxOf(
                    consumoPorMinuto[minuto] ?: 0.0,
                    ponto.consumoKwh
                )
            }
        }

        // Atualiza o valor do minuto atual.
        // Como ele é acumulado, substituímos o valor anterior,
        // em vez de somá-lo novamente.
        if (ultimoPonto != null) {

            val consumoRecebido = ultimoPonto.consumoKwh

            consumoPorMinuto[minutoReferencia] = maxOf(
                consumoPorMinuto[minutoReferencia] ?: 0.0,
                consumoRecebido
            )
        }

        // Mantém somente os últimos 5 minutos.
        val inicio = minutoReferencia -
                (INTERVALO_MINUTOS - 1) * 60_000L

        consumoPorMinuto.keys
            .filter {
                it < inicio || it > minutoReferencia
            }
            .forEach {
                consumoPorMinuto.remove(it)
            }

        // Cria os cinco horários consecutivos.
        val minutosExibidos = (0 until INTERVALO_MINUTOS)
            .map { indice ->
                inicio + indice * 60_000L
            }

        // Recupera os consumos armazenados.
        val valores = minutosExibidos.map { minuto ->
            (consumoPorMinuto[minuto] ?: 0.0).toFloat()
        }

        // Formata os horários.
        val horarios = minutosExibidos.map { minuto ->
            formatoHora.format(Date(minuto))
        }

        Log.d(
            TAG_GRAFICO,
            "===== GRÁFICO TEMPO REAL ====="
        )

        Log.d(
            TAG_GRAFICO,
            "Horários: $horarios"
        )

        Log.d(
            TAG_GRAFICO,
            "Consumos: $valores"
        )

        salvarHistoricoConsumoLocal()

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
    // INDICADORES
    // =====================================================

    private fun formatarConsumo(valor: Double): String {

        return String.format(
            Locale("pt", "BR"),
            "%.2f kWh",
            valor
        )
    }

    private fun formatarMoeda(valor: Double): String {

        return String.format(
            Locale("pt", "BR"),
            "R$ %.2f",
            valor
        )
    }

    private fun atualizarIndicadoresComDadosReais(
        dto: DashboardDTO
    ) {

        findViewById<TextView>(
            R.id.txv_consumoDiario
        ).text = formatarConsumo(
            dto.consumoTotalDiarioKwh
        )

        findViewById<TextView>(
            R.id.txv_custoDiario
        ).text = formatarMoeda(
            dto.custoDiarioReais
        )

        findViewById<TextView>(
            R.id.txv_economia
        ).text = formatarMoeda(
            dto.valorContaEstimadoMesReais
        )

        findViewById<TextView>(
            R.id.txv_consumoMensal
        ).text = formatarConsumo(
            dto.metaConsumoMensalAtualKwh
        )

        atualizarStatusMeta(
            dto.metaConsumoMensalAtualKwh,
            dto.metaConsumoMensalLimiteKwh,
            dto.mensagemStatusMeta
        )

        Log.d("CARDS_HOME", "========== DADOS DOS CARDS ==========")

        Log.d("CARDS_HOME", "Consumo diário: ${dto.consumoTotalDiarioKwh}")

        Log.d("CARDS_HOME", "Custo diário: ${dto.custoDiarioReais}")

        Log.d("CARDS_HOME", "Conta estimada: ${dto.valorContaEstimadoMesReais}")

        Log.d("CARDS_HOME", "Meta mensal atual: ${dto.metaConsumoMensalAtualKwh}")

        Log.d("CARDS_HOME", "Limite da meta: ${dto.metaConsumoMensalLimiteKwh}")

        Log.d("CARDS_HOME", "Mensagem da meta: ${dto.mensagemStatusMeta}")

        Log.d("CARDS_HOME", "====================================")
    }

    private fun atualizarStatusMeta(
        consumoMensal: Double,
        limiteMeta: Double,
        mensagem: String? = null
    ) {

        val txvMetaGasto =
            findViewById<TextView>(R.id.txv_metaGasto)

        val txvMetaMensal =
            findViewById<TextView>(R.id.txv_metaMensal)

        val txvMetaDescricao =
            findViewById<TextView>(R.id.txv_metaDescricao)

        val txvStatusMeta =
            findViewById<TextView>(R.id.txv_statusMeta)

        val txvStatusDescricao =
            findViewById<TextView>(R.id.txv_statusDescricao)

        val progressoMeta =
            findViewById<ProgressBar>(R.id.progressoMeta)

        val corSelecionada = AparenciaManager.corHex(this)

        progressoMeta.progressTintList =
            ColorStateList.valueOf(corSelecionada)

        if (limiteMeta <= 0.0) {

            txvMetaGasto.text = "Não definida"
            txvMetaMensal.text = "Não definida"

            txvMetaDescricao.text = "Cadastre uma meta para acompanhar"

            txvStatusMeta.text = "Nenhuma meta cadastrada"

            txvStatusDescricao.text =
                "Cadastre uma meta para acompanhar seu consumo."

            progressoMeta.progress = 0

            return
        }

        val percentual = (
                consumoMensal / limiteMeta * 100.0
                ).toInt()

        txvMetaGasto.text = formatarConsumo(limiteMeta)
        txvMetaMensal.text = formatarConsumo(limiteMeta)

        txvMetaDescricao.text =
            "↗ $percentual% da meta atingida"

        txvStatusMeta.text =
            mensagem ?: "Acompanhamento da sua meta"

        txvStatusDescricao.text =
            "O consumo está em $percentual% da sua meta."

        progressoMeta.progress =
            percentual.coerceIn(0, 100)
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

    private fun selecionarBotao(
        selecionado: TextView
    ) {

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
    // EDITAR META
    // =====================================================

    private fun configurarBotaoEditarMeta() {

        val btnEditar = findViewById<ImageButton>(
            R.id.btnEditarMeta
        )

        btnEditar.setOnClickListener {

            val dto = dashboardDados

            val possuiMeta =
                dto != null &&
                        dto.metaConsumoMensalLimiteKwh > 0.0

            val tipo = "CONSUMO"

            val valor =
                dto?.metaConsumoMensalLimiteKwh ?: 0.0

            val intent = Intent(
                this,
                MetaActivity::class.java
            ).apply {

                putExtra("tipoMeta", tipo)
                putExtra("valorMeta", valor)
                putExtra("possuiMeta", possuiMeta)
            }

            startActivityForResult(intent, 1001)
        }
    }

    // =====================================================
    // GRÁFICO EM TEMPO REAL
    // =====================================================

    private fun mostrarTempoReal() {

        abaAtual = ABA_TEMPO_REAL

        selecionarBotao(btnTempoReal)

        cardMetaMensal.visibility = View.GONE

        txvTituloGrafico.text =
            "Consumo de energia atual"

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

        txvTituloGrafico.text =
            "Consumo de energia"

        txvSubtituloGrafico.text =
            "Veja o consumo de energia (kWh) por hora"

        val pontos = dashboardDados
            ?.consumoDiarioHoras
            ?.takeLast(LIMITE_HORAS)
            ?: emptyList()

        val valores = pontos.map {
            it.consumoKwh.toFloat()
        }

        val horas = pontos.map {
            it.hora
        }

        Log.d(
            "GRAFICO_HORAS",
            "Horários: $horas | Valores: $valores"
        )

        grafico.atualizarGrafico(
            valores,
            horas,
            4
        )
    }

    // =====================================================
    // GRÁFICO DE ESTIMATIVA DA CONTA
    // =====================================================

    private fun mostrarValorConta() {

        abaAtual = ABA_CONTA

        selecionarBotao(btnValorConta)

        cardMetaMensal.visibility = View.GONE

        txvTituloGrafico.text =
            "Valor da conta (estimado)"

        txvSubtituloGrafico.text =
            "Mês atual e dois anteriores"

        // Mês atual + 2 anteriores (o backend envia em ordem cronológica).
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

        Log.d(
            TAG_GRAFICO,
            "Histórico de contas: $meses | Valores: $valores"
        )

        grafico.atualizarGrafico(
            valores,
            meses,
            2
        )
    }

    private val preferenciasHistorico by lazy {
        getSharedPreferences(
            "historico_consumo",
            MODE_PRIVATE
        )
    }

    private fun carregarHistoricoConsumoLocal() {

        consumoPorMinuto.clear()

        val historico = preferenciasHistorico
            .getString("consumo_por_minuto", null)
            ?: return

        historico.split(";").forEach { registro ->

            val partes = registro.split(":", limit = 2)

            if (partes.size == 2) {

                val minuto = partes[0].toLongOrNull()
                val consumo = partes[1].toDoubleOrNull()

                if (minuto != null && consumo != null) {
                    consumoPorMinuto[minuto] = consumo
                }
            }
        }

        // Mantém somente os últimos 10 minutos.
        val agora = obterMinutoAtual()
        val inicio = agora - (9 * 60_000L)

        consumoPorMinuto.keys
            .filter { it < inicio || it > agora }
            .toList()
            .forEach { consumoPorMinuto.remove(it) }

        Log.d(
            "HISTORICO_LOCAL",
            "Histórico recuperado: $consumoPorMinuto"
        )
    }

    private fun salvarHistoricoConsumoLocal() {

        val agora = obterMinutoAtual()
        val inicio = agora - (9 * 60_000L)

        consumoPorMinuto.keys
            .filter { it < inicio || it > agora }
            .toList()
            .forEach { consumoPorMinuto.remove(it) }

        val historico = consumoPorMinuto.entries
            .joinToString(";") {
                "${it.key}:${it.value}"
            }

        preferenciasHistorico.edit()
            .putString("consumo_por_minuto", historico)
            .apply()

        Log.d(
            "HISTORICO_LOCAL",
            "Histórico salvo: $consumoPorMinuto"
        )
    }

    // =====================================================
    // CICLO DE VIDA
    // =====================================================

    override fun onStart() {
        super.onStart()

        handler.post(atualizarRelogio)

        // Consulta imediata e depois a cada 5 s.
        handler.post(atualizarDashboard)

        agendarAtualizacaoMinuto()
    }

    override fun onStop() {

        handler.removeCallbacks(atualizarRelogio)

        handler.removeCallbacks(atualizarDashboard)

        handler.removeCallbacks(atualizarJanelaMinutos)

        super.onStop()
    }
}
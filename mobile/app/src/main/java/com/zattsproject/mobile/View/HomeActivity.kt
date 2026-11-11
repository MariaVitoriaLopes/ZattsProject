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
import java.util.Date
import java.util.Locale

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

    // Clientes e dados da Dashboard
    private var sseClient: SseClient? = null
    private var dashboardDados: DashboardDTO? = null

    // Acumulador em tempo real para os cards
    private var consumoDiarioAtualKwh: Double = 0.0
    private val tarifaKwhEstimada: Double = 0.85 // Tarifa estimada (CPFL Piratininga)

    private val handler = Handler(Looper.getMainLooper())

    private val atualizarRelogio = object : Runnable {
        override fun run() {
            atualizarDataHora()
            handler.postDelayed(this, 1000)
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

    private fun carregarDadosDashboardBackend() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // ID do utilizador logado no sistema
                val usuarioIdLogado = "6ae2afdbe3f4de30aa7d8f38"

                val resposta = RetrofitClient.apiService.obterDadosDashboard(usuarioIdLogado)
                if (resposta.isSuccessful && resposta.body() != null) {
                    val dto = resposta.body()!!
                    dashboardDados = dto
                    consumoDiarioAtualKwh = dto.consumoTotalDiarioKwh

                    withContext(Dispatchers.Main) {
                        atualizarCards(dto)
                        mostrarTempoReal()
                    }
                } else {
                    Log.e("HOME_API", "Erro ao carregar dashboard mobile: ${resposta.code()}")
                }
            } catch (e: Exception) {
                Log.e("HOME_API", "Falha de conexão na dashboard: ${e.message}")
            }
        }
    }

    private fun iniciarConexaoSse() {
        sseClient = SseClient { consumoRecebido ->
            runOnUiThread {
                processarNovaLeituraSse(consumoRecebido)
            }
        }
        val urlSse = "${BuildConfig.BASE_URL}api/v1/web/stream"
        sseClient?.iniciarConexao(urlSse)
    }

    private fun processarNovaLeituraSse(consumo: Consumo) {
        val deltaKwh = if (consumo.consumoKwh > 0) consumo.consumoKwh else 0.0
        consumoDiarioAtualKwh += deltaKwh

        val txvConsumoDiario = findViewById<TextView>(R.id.txv_consumoDiario)
        val txvCustoDiario = findViewById<TextView>(R.id.txv_custoDiario)

        txvConsumoDiario.text = String.format(Locale("pt", "BR"), "%.2f kWh", consumoDiarioAtualKwh)
        val custoEstimadoDiario = consumoDiarioAtualKwh * tarifaKwhEstimada
        txvCustoDiario.text = String.format(Locale("pt", "BR"), "R$ %.2f", custoEstimadoDiario)
    }

    private fun atualizarCards(dto: DashboardDTO) {
        val consumoMensal = dto.metaConsumoMensalAtualKwh
        val metaMensal = if (dto.metaConsumoMensalLimiteKwh > 0) dto.metaConsumoMensalLimiteKwh else 250.0
        val porcentagem = dto.percentualMetaAtingida.toInt()

        // 1. Consumo de Energia Hoje e Custo Diário do Backend
        findViewById<TextView>(R.id.txv_consumoDiario).text =
            String.format(Locale("pt", "BR"), "%.2f kWh", dto.consumoTotalDiarioKwh)

        findViewById<TextView>(R.id.txv_custoDiario).text =
            String.format(Locale("pt", "BR"), "R$ %.2f", dto.custoDiarioReais)

        // 2. Estimativa de Conta de Luz Mensal (Acumulada) vinda do Backend
        findViewById<TextView>(R.id.txv_economia).text =
            String.format(Locale("pt", "BR"), "R$ %.2f", dto.valorContaEstimadoMesReais)

        // 3. Meta de Gasto
        findViewById<TextView>(R.id.txv_metaGasto).text =
            String.format(Locale("pt", "BR"), "%.0f kWh", metaMensal)

        findViewById<TextView>(R.id.txv_metaDescricao).text = "↗ $porcentagem% da meta atingida"

        findViewById<TextView>(R.id.txv_consumoMensal).text =
            String.format(Locale("pt", "BR"), "%.1f kWh", consumoMensal)

        findViewById<TextView>(R.id.txv_metaMensal).text =
            String.format(Locale("pt", "BR"), "%.0f kWh", metaMensal)

        // 4. Mensagens de Status da Meta
        val txvStatusMeta = findViewById<TextView>(R.id.txv_statusMeta)
        val txvStatusDescricao = findViewById<TextView>(R.id.txv_statusDescricao)

        txvStatusMeta.text = dto.mensagemStatusMeta ?: "Você está dentro da sua meta!"
        txvStatusDescricao.text = "O consumo está em $porcentagem% da sua meta estipulada."

        val progressoMeta = findViewById<ProgressBar>(R.id.progressoMeta)
        val corSelecionada = AparenciaManager.corHex(this)

        progressoMeta.progressTintList = ColorStateList.valueOf(corSelecionada)
        progressoMeta.progress = porcentagem.coerceIn(0, 100)
    }

    private fun atualizarDataHora() {
        val agora = Date()
        val idioma = Locale("pt", "BR")
        val fusoSaoPaulo = java.util.TimeZone.getTimeZone("America/Sao_Paulo")

        val formatoHora = SimpleDateFormat("HH:mm:ss", idioma).apply {
            timeZone = fusoSaoPaulo
        }

        val formatoData = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", idioma).apply {
            timeZone = fusoSaoPaulo
        }

        txvHorario.text = formatoHora.format(agora)

        val dataFormatada = formatoData.format(agora)
        txvData.text = dataFormatada.replaceFirstChar { it.titlecase(idioma) }
    }

    private fun configurarBotoes() {
        btnTempoReal.setOnClickListener { mostrarTempoReal() }
        btnConsumo.setOnClickListener { mostrarConsumo() }
        btnValorConta.setOnClickListener { mostrarValorConta() }
    }

    private fun selecionarBotao(selecionado: TextView) {
        val botoes = listOf(btnTempoReal, btnConsumo, btnValorConta)
        botoes.forEach { it.background = null }
        selecionado.setBackgroundResource(R.drawable.bg_tab_selecionada)
    }

    private fun mostrarTempoReal() {
        selecionarBotao(btnTempoReal)
        cardMetaMensal.visibility = View.GONE
        txvTituloGrafico.text = "Consumo de energia atual"
        txvSubtituloGrafico.text = "Veja o consumo de energia em tempo real"

        val pontos = dashboardDados?.consumoTempoReal ?: emptyList()
        val valores = pontos.map { it.consumoKwh.toFloat() }
        val horas = pontos.map { it.hora }

        grafico.atualizarGraficoLinha(valores, horas)
    }

    private fun mostrarConsumo() {
        selecionarBotao(btnConsumo)
        cardMetaMensal.visibility = View.VISIBLE
        txvTituloGrafico.text = "Consumo de energia"
        txvSubtituloGrafico.text = "Veja o consumo de energia (kWh) por hora"

        val pontos = dashboardDados?.consumoDiarioHoras ?: emptyList()
        val valores = pontos.map { it.consumoKwh.toFloat() }
        val horas = pontos.map { it.hora }

        grafico.atualizarGrafico(valores, horas)
    }

    private fun mostrarValorConta() {
        selecionarBotao(btnValorConta)
        cardMetaMensal.visibility = View.GONE
        txvTituloGrafico.text = "Valor da conta (estimado)"
        txvSubtituloGrafico.text = "Comparativo dos últimos meses"

        val pontos = dashboardDados?.valorContaEstimadoReais ?: emptyList()
        val valores = pontos.map { it.valor.toFloat() }
        val meses = pontos.map { it.mes }

        grafico.atualizarGrafico(valores, meses)
    }

    override fun onStart() {
        super.onStart()
        handler.post(atualizarRelogio)
    }

    override fun onStop() {
        handler.removeCallbacks(atualizarRelogio)
        super.onStop()
    }

    override fun onDestroy() {
        sseClient?.fecharConexao()
        super.onDestroy()
    }
}
package com.zattsproject.mobile

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.zattsproject.mobile.Model.ConsumoDiario
import com.zattsproject.mobile.Repository.HistoricoRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import kotlin.math.ceil
import kotlin.math.roundToInt

class HistoryActivity : AppCompatActivity() {

    private enum class TipoHistorico {
        SEMANAL, MENSAL, COMPARATIVO
    }

    private var corAplicada = ""

    private lateinit var txvTituloHistorico: TextView
    private lateinit var tabSemanal: TextView
    private lateinit var tabMensal: TextView
    private lateinit var tabComparativo: TextView
    private lateinit var btnSelecionarPeriodo: MaterialButton
    private lateinit var btnExportarPdf: MaterialButton

    private lateinit var layoutIndicadores: View
    private lateinit var cardComparacaoValores: MaterialCardView
    private lateinit var txvTituloIndicador1: TextView
    private lateinit var txvValorIndicador1: TextView
    private lateinit var txvDescricaoIndicador1: TextView
    private lateinit var txvTituloIndicador2: TextView
    private lateinit var txvValorIndicador2: TextView
    private lateinit var txvDescricaoIndicador2: TextView
    private lateinit var txvTituloComparacao: TextView
    private lateinit var txvRotuloAtual: TextView
    private lateinit var txvValorAtual: TextView
    private lateinit var txvRotuloAnterior: TextView
    private lateinit var txvValorAnterior: TextView

    private lateinit var txvTituloGrafico: TextView
    private lateinit var txvDescricaoGrafico: TextView
    private lateinit var txvLegenda1: TextView
    private lateinit var txvLegenda2: TextView
    private lateinit var corLegenda1: View
    private lateinit var corLegenda2: View
    private lateinit var txvObservacaoGrafico: TextView
    private lateinit var graficoHistorico: GraficoHistoricoView
    private lateinit var indicadorRolagemGrafico: View

    private var tipoAtual = TipoHistorico.SEMANAL
    private val calendario = Calendar.getInstance()

    private val historicoRepository = HistoricoRepository()

    private var historicoDiario: List<ConsumoDiario> = emptyList()

    private val verde = android.graphics.Color.rgb(220, 255, 0)
    private val laranja = android.graphics.Color.rgb(255, 88, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)

        corAplicada = AparenciaManager.obterCor(this)
        setContentView(R.layout.activity_history)

        findViewById<AppNavBar>(R.id.navBar)
            .setSelected(NavTab.HISTORY)

        inicializarComponentes()
        configurarAbas()
        configurarPeriodo()
        configurarExportacao()
        atualizarTela(TipoHistorico.SEMANAL)

        carregarHistorico()
    }

    override fun onResume() {
        super.onResume()

        val corAtual = AparenciaManager.obterCor(this)

        if (corAtual != corAplicada) {
            recreate()
        }
    }

    private fun inicializarComponentes() {
        txvTituloHistorico = findViewById(R.id.txvTituloHistorico)
        tabSemanal = findViewById(R.id.tabSemanal)
        tabMensal = findViewById(R.id.tabMensal)
        tabComparativo = findViewById(R.id.tabComparativo)
        btnSelecionarPeriodo = findViewById(R.id.btnSelecionarPeriodo)
        btnExportarPdf = findViewById(R.id.btnExportarPdf)

        layoutIndicadores = findViewById(R.id.layoutIndicadores)
        cardComparacaoValores = findViewById(R.id.cardComparacaoValores)

        txvTituloIndicador1 = findViewById(R.id.txvTituloIndicador1)
        txvValorIndicador1 = findViewById(R.id.txvValorIndicador1)
        txvDescricaoIndicador1 = findViewById(R.id.txvDescricaoIndicador1)
        txvTituloIndicador2 = findViewById(R.id.txvTituloIndicador2)
        txvValorIndicador2 = findViewById(R.id.txvValorIndicador2)
        txvDescricaoIndicador2 = findViewById(R.id.txvDescricaoIndicador2)

        txvTituloComparacao = findViewById(R.id.txvTituloComparacao)
        txvRotuloAtual = findViewById(R.id.txvRotuloAtual)
        txvValorAtual = findViewById(R.id.txvValorAtual)
        txvRotuloAnterior = findViewById(R.id.txvRotuloAnterior)
        txvValorAnterior = findViewById(R.id.txvValorAnterior)

        txvTituloGrafico = findViewById(R.id.txvTituloGrafico)
        txvDescricaoGrafico = findViewById(R.id.txvDescricaoGrafico)
        txvLegenda1 = findViewById(R.id.txvLegenda1)
        txvLegenda2 = findViewById(R.id.txvLegenda2)
        corLegenda1 = findViewById(R.id.corLegenda1)
        corLegenda2 = findViewById(R.id.corLegenda2)
        txvObservacaoGrafico = findViewById(R.id.txvObservacaoGrafico)
        graficoHistorico = findViewById(R.id.graficoHistorico)
        indicadorRolagemGrafico = findViewById(R.id.indicadorRolagemGrafico)
    }

    private fun configurarAbas() {
        tabSemanal.setOnClickListener { atualizarTela(TipoHistorico.SEMANAL) }
        tabMensal.setOnClickListener { atualizarTela(TipoHistorico.MENSAL) }
        tabComparativo.setOnClickListener { atualizarTela(TipoHistorico.COMPARATIVO) }
    }

    private fun atualizarTela(tipo: TipoHistorico) {
        tipoAtual = tipo
        atualizarAparenciaAbas()

        when (tipo) {
            TipoHistorico.SEMANAL -> mostrarSemanal()
            TipoHistorico.MENSAL -> mostrarMensal()
            TipoHistorico.COMPARATIVO -> mostrarComparativo()
        }
    }

    private fun atualizarAparenciaAbas() {

        val abas = listOf(
            tabSemanal to (tipoAtual == TipoHistorico.SEMANAL),
            tabMensal to (tipoAtual == TipoHistorico.MENSAL),
            tabComparativo to (tipoAtual == TipoHistorico.COMPARATIVO)
        )

        abas.forEach { (aba, selecionada) ->

            if (selecionada) {

                aba.setBackgroundResource(
                    R.drawable.bg_notificacoes_aba_ativa
                )

                aba.setTextColor(
                    com.google.android.material.color.MaterialColors.getColor(
                        aba,
                        com.google.android.material.R.attr.colorOnPrimary
                    )
                )

                aba.elevation = dp(2).toFloat()

            } else {

                aba.setBackgroundResource(0)

                aba.setTextColor(
                    com.google.android.material.color.MaterialColors.getColor(
                        aba,
                        com.google.android.material.R.attr.colorOnSurface
                    )
                )

                aba.elevation = 0f
            }
        }
    }

    private fun mostrarSemanal() {
        val formatoApi = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )
        val formatoDiaMes = SimpleDateFormat(
            "dd/MM",
            Locale.getDefault()
        )
        // Segunda-feira da semana selecionada
        val inicioSemana = calendario.clone() as Calendar
        val diasDesdeSegunda =
            (inicioSemana.get(Calendar.DAY_OF_WEEK) + 5) % 7
        inicioSemana.add(
            Calendar.DAY_OF_MONTH,
            -diasDesdeSegunda
        )
        // Domingo da semana selecionada
        val fimSemana = inicioSemana.clone() as Calendar
        fimSemana.add(Calendar.DAY_OF_MONTH, 6)
        // Semana anterior
        val inicioSemanaAnterior =
            inicioSemana.clone() as Calendar
        inicioSemanaAnterior.add(
            Calendar.DAY_OF_MONTH,
            -7
        )
        val fimSemanaAnterior =
            fimSemana.clone() as Calendar

        fimSemanaAnterior.add(
            Calendar.DAY_OF_MONTH,
            -7
        )
        val inicioAtualApi =
            formatoApi.format(inicioSemana.time)
        val fimAtualApi =
            formatoApi.format(fimSemana.time)
        val inicioAnteriorApi =
            formatoApi.format(inicioSemanaAnterior.time)
        val fimAnteriorApi =
            formatoApi.format(fimSemanaAnterior.time)
        val dadosSemanaAtual =
            historicoDiario.filter { consumo ->
                consumo.dia >= inicioAtualApi &&
                        consumo.dia <= fimAtualApi
            }
        val dadosSemanaAnterior =
            historicoDiario.filter { consumo ->
                consumo.dia >= inicioAnteriorApi &&
                        consumo.dia <= fimAnteriorApi
            }
        val totalKwh =
            dadosSemanaAtual.sumOf { it.kwh }
        val totalReais =
            dadosSemanaAtual.sumOf { it.reais }
        val totalReaisSemanaAnterior =
            dadosSemanaAnterior.sumOf { it.reais }
        val valoresGrafico = (0..6).map { indice ->
            val dia =
                inicioSemana.clone() as Calendar
            dia.add(
                Calendar.DAY_OF_MONTH,
                indice
            )
            val chaveDia =
                formatoApi.format(dia.time)
            historicoDiario
                .filter { it.dia == chaveDia }
                .sumOf { it.kwh }
                .toFloat()
        }
        val nomesDias = listOf(
            "Segunda",
            "Terça",
            "Quarta",
            "Quinta",
            "Sexta",
            "Sábado",
            "Domingo"
        )
        val rotulosGrafico = (0..6).map { indice ->
            val dia =
                inicioSemana.clone() as Calendar
            dia.add(
                Calendar.DAY_OF_MONTH,
                indice
            )
            "${nomesDias[indice]}\n${formatoDiaMes.format(dia.time)}"
        }
        txvTituloHistorico.text =
            "Histórico Semanal"
        btnSelecionarPeriodo.text =
            "${formatoDiaMes.format(inicioSemana.time)} - " +
                    "${formatoDiaMes.format(fimSemana.time)} de " +
                    "${fimSemana.get(Calendar.YEAR)}"
        layoutIndicadores.visibility = View.VISIBLE
        cardComparacaoValores.visibility = View.VISIBLE
        txvTituloIndicador1.text =
            "Consumo de energia"
        txvValorIndicador1.text =
            String.format(
                Locale.getDefault(),
                "%.1f kWh",
                totalKwh
            )
        txvDescricaoIndicador1.text =
            "Total de energia consumida na semana"
        txvTituloIndicador2.text =
            "Custo estimado"
        txvValorIndicador2.text =
            String.format(
                Locale.getDefault(),
                "%.2f Reais",
                totalReais
            )
        txvDescricaoIndicador2.text =
            "Valor estimado na semana com base na tarifa"
        txvTituloComparacao.text =
            "Valor total por semana"
        txvRotuloAtual.text =
            "Semana de ${formatoDiaMes.format(inicioSemana.time)} - " +
                    formatoDiaMes.format(fimSemana.time)
        txvValorAtual.text =
            String.format(
                Locale.getDefault(),
                "R$ %.2f",
                totalReais
            )
        txvRotuloAnterior.text =
            "Semana anterior"
        txvValorAnterior.text =
            String.format(
                Locale.getDefault(),
                "R$ %.2f",
                totalReaisSemanaAnterior
            )
        txvTituloGrafico.text =
            "Consumo de energia semanal"
        txvDescricaoGrafico.text =
            "Veja o consumo de energia (kWh) de cada dia da semana"
        configurarLegenda(
            "Consumo diário",
            null
        )
        graficoHistorico.configurar(
            modo = GraficoHistoricoView.Modo.LINHA_AREA,
            valores = valoresGrafico,
            rotulos = rotulosGrafico,
            corPrincipal = verde
        )
        indicadorRolagemGrafico.visibility =
            View.GONE
        txvObservacaoGrafico.visibility =
            View.GONE
    }

    private fun carregarHistorico() {
        val formatoData = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )
        val hoje = Calendar.getInstance()
        val inicio = Calendar.getInstance().apply {
            set(
                hoje.get(Calendar.YEAR) - 1,
                Calendar.JANUARY,
                1
            )
        }
        val inicioFormatado = formatoData.format(inicio.time)
        val fimFormatado = formatoData.format(hoje.time)
        lifecycleScope.launch {
            try {
                val resposta =
                    historicoRepository.buscarTotaisDiarios(
                        inicio = inicioFormatado,
                        fim = fimFormatado
                    )

                if (resposta.isSuccessful) {
                    historicoDiario = resposta.body().orEmpty()
                    atualizarTela(tipoAtual)
                    Log.d(
                        "ZATTS_HISTORICO",
                        "Histórico carregado: ${historicoDiario.size} dias"
                    )
                    Log.d(
                        "ZATTS_HISTORICO",
                        historicoDiario.toString()
                    )
                } else {
                    Log.e(
                        "ZATTS_HISTORICO",
                        "Erro HTTP: ${resposta.code()}"
                    )
                }
            } catch (e: Exception) {
                Log.e(
                    "ZATTS_HISTORICO",
                    "Erro ao carregar histórico",
                    e
                )
            }
        }
    }

    private fun mostrarMensal() {
        val formatoApi = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )
        val localePtBr =
            Locale.forLanguageTag("pt-BR")
        // Primeiro dia do mês selecionado
        val inicioMes =
            calendario.clone() as Calendar
        inicioMes.set(
            Calendar.DAY_OF_MONTH,
            1
        )
        // Último dia do mês selecionado
        val fimMes =
            calendario.clone() as Calendar
        fimMes.set(
            Calendar.DAY_OF_MONTH,
            fimMes.getActualMaximum(
                Calendar.DAY_OF_MONTH
            )
        )
        // Mês anterior
        val inicioMesAnterior =
            inicioMes.clone() as Calendar
        inicioMesAnterior.add(
            Calendar.MONTH,
            -1
        )
        val fimMesAnterior =
            inicioMesAnterior.clone() as Calendar
        fimMesAnterior.set(
            Calendar.DAY_OF_MONTH,
            fimMesAnterior.getActualMaximum(
                Calendar.DAY_OF_MONTH
            )
        )
        val inicioAtualApi =
            formatoApi.format(inicioMes.time)
        val fimAtualApi =
            formatoApi.format(fimMes.time)
        val inicioAnteriorApi =
            formatoApi.format(inicioMesAnterior.time)
        val fimAnteriorApi =
            formatoApi.format(fimMesAnterior.time)
        // Dados reais do mês atual
        val dadosMesAtual =
            historicoDiario.filter { consumo ->
                consumo.dia >= inicioAtualApi &&
                        consumo.dia <= fimAtualApi
            }
        // Dados reais do mês anterior
        val dadosMesAnterior =
            historicoDiario.filter { consumo ->
                consumo.dia >= inicioAnteriorApi &&
                        consumo.dia <= fimAnteriorApi
            }
        val totalKwh =
            dadosMesAtual.sumOf { it.kwh }
        val totalReais =
            dadosMesAtual.sumOf { it.reais }
        val totalReaisMesAnterior =
            dadosMesAnterior.sumOf { it.reais }
        val quantidadeDias =
            fimMes.get(Calendar.DAY_OF_MONTH)
        val valoresGrafico =
            (1..quantidadeDias).map { diaMes ->
                val dia =
                    inicioMes.clone() as Calendar
                dia.set(
                    Calendar.DAY_OF_MONTH,
                    diaMes
                )
                val chaveDia =
                    formatoApi.format(dia.time)
                historicoDiario
                    .filter { it.dia == chaveDia }
                    .sumOf { it.kwh }
                    .toFloat()
            }
        val rotulosGrafico =
            (1..quantidadeDias).map { diaMes ->
                if (
                    diaMes == 1 ||
                    diaMes % 5 == 0 ||
                    diaMes == quantidadeDias
                ) {
                    diaMes
                        .toString()
                        .padStart(2, '0')
                } else {
                    ""
                }
            }
        val nomeMes =
            SimpleDateFormat(
                "MMMM",
                localePtBr
            ).format(inicioMes.time)
                .replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(localePtBr)
                    } else {
                        it.toString()
                    }
                }
        val nomeMesAnterior =
            SimpleDateFormat(
                "MMMM",
                localePtBr
            ).format(inicioMesAnterior.time)
                .replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(localePtBr)
                    } else {
                        it.toString()
                    }
                }
        txvTituloHistorico.text =
            "Histórico Mensal"
        btnSelecionarPeriodo.text =
            "$nomeMes de ${inicioMes.get(Calendar.YEAR)}"
        layoutIndicadores.visibility =
            View.VISIBLE
        cardComparacaoValores.visibility =
            View.VISIBLE
        // Consumo
        txvTituloIndicador1.text =
            "Consumo de energia"
        txvValorIndicador1.text =
            String.format(
                localePtBr,
                "%.1f kWh",
                totalKwh
            )
        txvDescricaoIndicador1.text =
            "Total de energia consumida no mês"
        // Custo
        txvTituloIndicador2.text =
            "Custo estimado"
        txvValorIndicador2.text =
            String.format(
                localePtBr,
                "%.2f Reais",
                totalReais
            )
        txvDescricaoIndicador2.text =
            "Valor estimado no mês com base na tarifa"
        // Comparação
        txvTituloComparacao.text =
            "Valor total por mês"
        txvRotuloAtual.text =
            "Mês de $nomeMes"
        txvValorAtual.text =
            String.format(
                localePtBr,
                "R$ %.2f",
                totalReais
            )
        txvRotuloAnterior.text =
            "Mês de $nomeMesAnterior"
        txvValorAnterior.text =
            String.format(
                localePtBr,
                "R$ %.2f",
                totalReaisMesAnterior
            )
        // Gráfico
        txvTituloGrafico.text =
            "Consumo de energia mensal"
        txvDescricaoGrafico.text =
            "Veja o consumo de energia (kWh) de cada dia do mês"
        configurarLegenda(
            "Consumo diário",
            null
        )
        graficoHistorico.configurar(
            modo = GraficoHistoricoView.Modo.BARRAS,
            valores = valoresGrafico,
            rotulos = rotulosGrafico,
            corPrincipal = verde
        )
        indicadorRolagemGrafico.visibility =
            View.VISIBLE
        txvObservacaoGrafico.visibility =
            View.VISIBLE
    }

    private fun mostrarComparativo() {
        val formatoApi = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )
        val localePtBr =
            Locale.forLanguageTag("pt-BR")
        // Mês selecionado
        val inicioMesAtual =
            calendario.clone() as Calendar
        inicioMesAtual.set(
            Calendar.DAY_OF_MONTH,
            1
        )
        val fimMesAtual =
            inicioMesAtual.clone() as Calendar
        fimMesAtual.set(
            Calendar.DAY_OF_MONTH,
            fimMesAtual.getActualMaximum(
                Calendar.DAY_OF_MONTH
            )
        )
        // Mês anterior
        val inicioMesAnterior =
            inicioMesAtual.clone() as Calendar
        inicioMesAnterior.add(
            Calendar.MONTH,
            -1
        )
        val fimMesAnterior =
            inicioMesAnterior.clone() as Calendar
        fimMesAnterior.set(
            Calendar.DAY_OF_MONTH,
            fimMesAnterior.getActualMaximum(
                Calendar.DAY_OF_MONTH
            )
        )
        val inicioAtualApi =
            formatoApi.format(inicioMesAtual.time)
        val fimAtualApi =
            formatoApi.format(fimMesAtual.time)
        val inicioAnteriorApi =
            formatoApi.format(inicioMesAnterior.time)
        val fimAnteriorApi =
            formatoApi.format(fimMesAnterior.time)
        val dadosMesAtual =
            historicoDiario.filter { consumo ->
                consumo.dia >= inicioAtualApi &&
                        consumo.dia <= fimAtualApi
            }
        val dadosMesAnterior =
            historicoDiario.filter { consumo ->
                consumo.dia >= inicioAnteriorApi &&
                        consumo.dia <= fimAnteriorApi
            }
        val totalReaisAtual =
            dadosMesAtual.sumOf { it.reais }
        val totalReaisAnterior =
            dadosMesAnterior.sumOf { it.reais }
        val diasMesAtual =
            fimMesAtual.get(Calendar.DAY_OF_MONTH)
        val diasMesAnterior =
            fimMesAnterior.get(Calendar.DAY_OF_MONTH)
        val quantidadeDias =
            max(
                diasMesAtual,
                diasMesAnterior
            )
        // Consumo diário do mês atual
        val valoresMesAtual =
            (1..quantidadeDias).map { diaMes ->
                if (diaMes <= diasMesAtual) {
                    val dia =
                        inicioMesAtual.clone() as Calendar
                    dia.set(
                        Calendar.DAY_OF_MONTH,
                        diaMes
                    )
                    val chaveDia =
                        formatoApi.format(dia.time)
                    historicoDiario
                        .filter { it.dia == chaveDia }
                        .sumOf { it.kwh }
                        .toFloat()
                } else {
                    0f
                }
            }
        // Consumo diário do mês anterior
        val valoresMesAnterior =
            (1..quantidadeDias).map { diaMes ->
                if (diaMes <= diasMesAnterior) {
                    val dia =
                        inicioMesAnterior.clone() as Calendar
                    dia.set(
                        Calendar.DAY_OF_MONTH,
                        diaMes
                    )
                    val chaveDia =
                        formatoApi.format(dia.time)
                    historicoDiario
                        .filter { it.dia == chaveDia }
                        .sumOf { it.kwh }
                        .toFloat()
                } else {
                    0f
                }
            }
        val rotulosGrafico =
            (1..quantidadeDias).map { diaMes ->
                if (
                    diaMes == 1 ||
                    diaMes % 5 == 0 ||
                    diaMes == quantidadeDias
                ) {
                    diaMes
                        .toString()
                        .padStart(2, '0')
                } else {
                    ""
                }
            }
        val nomeMesAtual =
            SimpleDateFormat(
                "MMMM",
                localePtBr
            ).format(inicioMesAtual.time)
                .replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(localePtBr)
                    } else {
                        it.toString()
                    }
                }
        val nomeMesAnterior =
            SimpleDateFormat(
                "MMMM",
                localePtBr
            ).format(inicioMesAnterior.time)
                .replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(localePtBr)
                    } else {
                        it.toString()
                    }
                }
        val mesCurtoAtual =
            SimpleDateFormat(
                "MMM",
                localePtBr
            ).format(inicioMesAtual.time)
                .removeSuffix(".")
                .replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(localePtBr)
                    } else {
                        it.toString()
                    }
                }
        val mesCurtoAnterior =
            SimpleDateFormat(
                "MMM",
                localePtBr
            ).format(inicioMesAnterior.time)
                .removeSuffix(".")
                .replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(localePtBr)
                    } else {
                        it.toString()
                    }
                }
        txvTituloHistorico.text =
            "Histórico Comparativo"
        val anoAtual =
            inicioMesAtual.get(Calendar.YEAR)
        val anoAnterior =
            inicioMesAnterior.get(Calendar.YEAR)
        btnSelecionarPeriodo.text =
            if (anoAtual == anoAnterior) {
                "$mesCurtoAtual. e $mesCurtoAnterior. de $anoAtual"
            } else {
                "$mesCurtoAtual. de $anoAtual e " +
                        "$mesCurtoAnterior. de $anoAnterior"
            }
        layoutIndicadores.visibility =
            View.GONE
        cardComparacaoValores.visibility =
            View.VISIBLE
        txvTituloComparacao.text =
            "Valor total por mês"
        txvRotuloAtual.text =
            "Mês de $nomeMesAtual"
        txvValorAtual.text =
            String.format(
                localePtBr,
                "R$ %.2f",
                totalReaisAtual
            )
        txvRotuloAnterior.text =
            "Mês de $nomeMesAnterior"
        txvValorAnterior.text =
            String.format(
                localePtBr,
                "R$ %.2f",
                totalReaisAnterior
            )
        txvTituloGrafico.text =
            "Comparativo entre meses"
        txvDescricaoGrafico.text =
            "Veja o consumo de energia (kWh) comparativo entre os meses"
        configurarLegenda(
            "Consumo em $nomeMesAtual",
            "Consumo em $nomeMesAnterior"
        )
        graficoHistorico.configurar(
            modo = GraficoHistoricoView.Modo.COMPARATIVO,
            valores = valoresMesAtual,
            valoresSecundarios = valoresMesAnterior,
            rotulos = rotulosGrafico,
            corPrincipal = verde,
            corSecundaria = laranja
        )
        indicadorRolagemGrafico.visibility =
            View.VISIBLE
        txvObservacaoGrafico.visibility =
            View.VISIBLE
    }

    private fun configurarLegenda(principal: String, secundaria: String?) {
        txvLegenda1.text = principal
        corLegenda1.setBackgroundColor(verde)

        val temSegundaLegenda = secundaria != null
        corLegenda2.visibility = if (temSegundaLegenda) View.VISIBLE else View.GONE
        txvLegenda2.visibility = if (temSegundaLegenda) View.VISIBLE else View.GONE

        if (secundaria != null) {
            txvLegenda2.text = secundaria
            corLegenda2.setBackgroundColor(laranja)
        }
    }

    private fun configurarPeriodo() {
        btnSelecionarPeriodo.setOnClickListener {
            val hoje = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, ano, mes, dia ->
                    calendario.set(ano, mes, dia)
                    atualizarTextoPeriodoSelecionado()
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun atualizarTextoPeriodoSelecionado() {
        val dia = calendario.get(Calendar.DAY_OF_MONTH)
        val mes = calendario.getDisplayName(
            Calendar.MONTH,
            Calendar.SHORT,
            Locale("pt", "BR")
        ) ?: ""
        val ano = calendario.get(Calendar.YEAR)

        btnSelecionarPeriodo.text = when (tipoAtual) {
            TipoHistorico.SEMANAL -> {
                val inicio = (calendario.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                }
                val fim = (inicio.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_MONTH, 6)
                }
                "${inicio.get(Calendar.DAY_OF_MONTH)}/$mes - " +
                        "${fim.get(Calendar.DAY_OF_MONTH)}/$mes de $ano"
            }
            TipoHistorico.MENSAL -> "$mes de $ano"
            TipoHistorico.COMPARATIVO -> "$mes de $ano"
        }
    }

    private fun configurarExportacao() {
        btnExportarPdf.setOnClickListener {
            // EXPORTAÇÃO:
            // Colocar caminho aqui (função ou endpoint responsável por gerar/exportar o PDF).
            // Se a exportação for feita no aplicativo, implementar a geração com PdfDocument.
            Toast.makeText(
                this,
                "Coloque aqui o caminho da exportação de PDF.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()
}

/**
 * Gráfico simples desenhado em Canvas para não depender de uma biblioteca externa.
 * Os dados reais devem ser enviados por configurar() após a integração com a API.
 */
class GraficoHistoricoView @JvmOverloads constructor(
    context: Context,
    attrs: android.util.AttributeSet? = null
) : View(context, attrs) {

    enum class Modo {
        BARRAS, LINHA_AREA, COMPARATIVO
    }

    private val tinta = Paint(Paint.ANTI_ALIAS_FLAG)
    private var modo = Modo.BARRAS
    private var valores = emptyList<Float>()
    private var valoresSecundarios = emptyList<Float>()
    private var rotulos = emptyList<String>()
    private var corPrincipal = android.graphics.Color.rgb(220, 255, 0)
    private var corSecundaria = android.graphics.Color.rgb(255, 88, 0)

    private val corTexto: Int
        get() {
            val valor = android.util.TypedValue()

            return if (
                context.theme.resolveAttribute(
                    com.google.android.material.R.attr.colorOnSurface,
                    valor,
                    true
                )
            ) {
                if (valor.resourceId != 0) {
                    androidx.core.content.ContextCompat.getColor(
                        context,
                        valor.resourceId
                    )
                } else {
                    valor.data
                }
            } else {
                android.graphics.Color.BLACK
            }
        }

    private val corGrade: Int
        get() = android.graphics.Color.LTGRAY

    fun configurar(
        modo: Modo,
        valores: List<Float>,
        rotulos: List<String>,
        corPrincipal: Int,
        valoresSecundarios: List<Float> = emptyList(),
        corSecundaria: Int = android.graphics.Color.rgb(255, 88, 0)
    ) {
        this.modo = modo
        this.valores = valores
        this.rotulos = rotulos
        this.corPrincipal = corPrincipal
        this.valoresSecundarios = valoresSecundarios
        this.corSecundaria = corSecundaria
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (valores.isEmpty()) return

        val esquerda = dp(28f)
        val direita = width - dp(8f)
        val topo = dp(20f)
        val base = height - dp(42f)
        val altura = base - topo
        val largura = direita - esquerda
        val maiorValor = max(
            valores.maxOrNull() ?: 0f,
            valoresSecundarios.maxOrNull() ?: 0f
        )
        val passoEixo = calcularPassoEixo(maiorValor)
        val maximo = if (maiorValor <= 0f) {
            passoEixo
        } else {
            ceil(
                (maiorValor / passoEixo).toDouble()
            ).toFloat() * passoEixo
        }
        val quantidadeIntervalos =
            (maximo / passoEixo)
                .roundToInt()
                .coerceAtLeast(1)
        desenharGrade(
            canvas,
            esquerda,
            direita,
            topo,
            base,
            altura,
            maximo,
            quantidadeIntervalos
        )

        when (modo) {
            Modo.BARRAS -> desenharBarras(canvas, esquerda, direita, topo, base, altura, maximo)
            Modo.LINHA_AREA -> desenharLinha(
                canvas, esquerda, direita, topo, base, altura, maximo,
                valores, corPrincipal, preencher = true
            )
            Modo.COMPARATIVO -> {
                desenharLinha(
                    canvas, esquerda, direita, topo, base, altura, maximo,
                    valores, corPrincipal, preencher = false
                )
                desenharLinha(
                    canvas, esquerda, direita, topo, base, altura, maximo,
                    valoresSecundarios, corSecundaria, preencher = false
                )
            }
        }

        desenharRotulos(canvas, esquerda, direita, base, largura)
    }

    private fun calcularPassoEixo(maiorValor: Float): Float {
        if (maiorValor <= 0f) {
            return 0.2f
        }
        val passoDesejado = maiorValor / 7f
        val passos = listOf(
            0.01f,
            0.02f,
            0.05f,
            0.1f,
            0.2f,
            0.5f,
            1f,
            2f,
            5f,
            10f,
            20f,
            50f,
            100f,
            200f,
            500f,
            1000f
        )
        return passos.firstOrNull {
            it >= passoDesejado
        } ?: 1000f
    }
    private fun desenharGrade(
        canvas: Canvas,
        esquerda: Float,
        direita: Float,
        topo: Float,
        base: Float,
        altura: Float,
        maximo: Float,
        quantidadeIntervalos: Int
    ) {
        tinta.color = corGrade
        tinta.strokeWidth = dp(0.7f)
        tinta.style = Paint.Style.STROKE
        for (i in 0..quantidadeIntervalos) {
            val y =
                base - (
                        altura * i /
                                quantidadeIntervalos.toFloat()
                        )
            canvas.drawLine(
                esquerda,
                y,
                direita,
                y,
                tinta
            )
            val valorEixo =
                maximo * i /
                        quantidadeIntervalos.toFloat()
            val rotuloEixo =
                if (valorEixo == 0f) {
                    "0"
                } else if (valorEixo < 10f) {
                    String.format(
                        Locale.forLanguageTag("pt-BR"),
                        "%.1f",
                        valorEixo
                    )
                } else {
                    String.format(
                        Locale.forLanguageTag("pt-BR"),
                        "%.0f",
                        valorEixo
                    )
                }
            tinta.style = Paint.Style.FILL
            tinta.textSize = dp(11f)
            tinta.color = corTexto
            tinta.textAlign = Paint.Align.RIGHT
            canvas.drawText(
                rotuloEixo,
                esquerda - dp(7f),
                y + dp(4f),
                tinta
            )
            tinta.color = corGrade
            tinta.style = Paint.Style.STROKE
        }
        tinta.style = Paint.Style.FILL
    }

    private fun desenharBarras(
        canvas: Canvas,
        esquerda: Float,
        direita: Float,
        topo: Float,
        base: Float,
        altura: Float,
        maximo: Float
    ) {
        val espacamento = (direita - esquerda) / valores.size
        val larguraBarra = espacamento * 0.62f

        valores.forEachIndexed { indice, valor ->
            val xCentro = esquerda + espacamento * (indice + 0.5f)
            val y = base - (valor / maximo) * altura
            tinta.color = corPrincipal
            tinta.style = Paint.Style.FILL
            canvas.drawRoundRect(
                RectF(
                    xCentro - larguraBarra / 2,
                    y,
                    xCentro + larguraBarra / 2,
                    base
                ),
                dp(8f),
                dp(8f),
                tinta
            )
        }
    }

    private fun desenharLinha(
        canvas: Canvas,
        esquerda: Float,
        direita: Float,
        topo: Float,
        base: Float,
        altura: Float,
        maximo: Float,
        dados: List<Float>,
        cor: Int,
        preencher: Boolean
    ) {
        if (dados.isEmpty()) return

        val espacamento = if (dados.size > 1) {
            (direita - esquerda) / (dados.size - 1)
        } else 0f

        val caminho = Path()
        val pontos = dados.mapIndexed { indice, valor ->
            val x = esquerda + espacamento * indice
            val y = base - (valor / maximo) * altura
            x to y
        }

        pontos.forEachIndexed { indice, ponto ->
            if (indice == 0) caminho.moveTo(ponto.first, ponto.second)
            else caminho.lineTo(ponto.first, ponto.second)
        }

        if (preencher) {
            val area = Path(caminho).apply {
                lineTo(pontos.last().first, base)
                lineTo(pontos.first().first, base)
                close()
            }
            tinta.color = cor
            tinta.alpha = 55
            tinta.style = Paint.Style.FILL
            canvas.drawPath(area, tinta)
            tinta.alpha = 255
        }

        tinta.color = cor
        tinta.strokeWidth = dp(2f)
        tinta.style = Paint.Style.STROKE
        tinta.strokeCap = Paint.Cap.ROUND
        tinta.strokeJoin = Paint.Join.ROUND
        canvas.drawPath(caminho, tinta)

        pontos.forEach { (x, y) ->
            tinta.style = Paint.Style.FILL
            tinta.color = cor
            canvas.drawCircle(x, y, dp(6f), tinta)
            tinta.color = android.graphics.Color.WHITE
            canvas.drawCircle(x, y, dp(3f), tinta)
        }
        tinta.style = Paint.Style.FILL
    }

    private fun desenharRotulos(
        canvas: Canvas,
        esquerda: Float,
        direita: Float,
        base: Float,
        largura: Float
    ) {
        if (rotulos.isEmpty()) return

        tinta.color = corTexto
        tinta.textSize = dp(9f)
        tinta.textAlign = Paint.Align.CENTER
        tinta.style = Paint.Style.FILL

        val espacamento = if (rotulos.size > 1) {
            largura / (rotulos.size - 1)
        } else 0f

        rotulos.forEachIndexed { indice, rotulo ->
            val x = if (rotulos.size > 1) esquerda + espacamento * indice
            else (esquerda + direita) / 2

            val linhas = rotulo.split("\n")
            linhas.forEachIndexed { linha, texto ->
                canvas.drawText(
                    texto,
                    x,
                    base + dp(17f) + linha * dp(12f),
                    tinta
                )
            }
        }
    }

    private fun dp(valor: Float): Float =
        valor * resources.displayMetrics.density
}
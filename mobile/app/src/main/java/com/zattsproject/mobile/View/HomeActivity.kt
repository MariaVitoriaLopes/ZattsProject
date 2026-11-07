
package com.zattsproject.mobile

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zattsproject.mobile.View.GraficoConsumoView
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

        configurarIndicadores()
        configurarBotoes()

        mostrarTempoReal()
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

    private fun atualizarDataHora() {
        val agora = Date()
        val idioma = Locale("pt", "BR")

        val formatoHora = SimpleDateFormat(
            "HH:mm:ss", idioma
        )

        val formatoData = SimpleDateFormat(
            "EEEE, dd 'de' MMMM 'de' yyyy", idioma
        )

        txvHorario.text = formatoHora.format(agora)

        val dataFormatada = formatoData.format(agora)

        txvData.text = dataFormatada.replaceFirstChar {
            it.titlecase(idioma)
        }
    }

    private fun configurarIndicadores() {

        // Dados temporários para desenvolvimento
        val consumoMensal = 90.0
        val metaMensal = 250.0

        val porcentagem = if (metaMensal > 0) {
            ((consumoMensal / metaMensal) * 100).toInt()
        } else {
            0
        }

        findViewById<TextView>(
            R.id.txv_consumoDiario
        ).text = "32,8 kWh"

        findViewById<TextView>(
            R.id.txv_custoDiario
        ).text = "R$ 3,57"

        findViewById<TextView>(
            R.id.txv_metaGasto
        ).text = "250 kWh"

        findViewById<TextView>(
            R.id.txv_economia
        ).text = "R$ 3,45"

        findViewById<TextView>(
            R.id.txv_metaDescricao
        ).text = "↗ $porcentagem% da meta atingida"

        findViewById<TextView>(
            R.id.txv_consumoMensal
        ).text = "90 kWh"

        findViewById<TextView>(
            R.id.txv_metaMensal
        ).text = "250 kWh"

        // Status da meta
        val txvStatusMeta = findViewById<TextView>(
            R.id.txv_statusMeta
        )

        val txvStatusDescricao = findViewById<TextView>(
            R.id.txv_statusDescricao
        )

        if (consumoMensal <= metaMensal) {
            txvStatusMeta.text = "Você está dentro da sua meta!"
            txvStatusDescricao.text =
                "O consumo está em $porcentagem% da sua meta."
        } else {
            txvStatusMeta.text = "Você ultrapassou sua meta!"
            txvStatusDescricao.text =
                "O consumo está em $porcentagem% da sua meta."
        }

        val progressoMeta = findViewById<ProgressBar>(
            R.id.progressoMeta
        )

        val corSelecionada = AparenciaManager.corHex(this)

        progressoMeta.progressTintList =
            ColorStateList.valueOf(corSelecionada)

        progressoMeta.progress = porcentagem.coerceIn(0, 100)
    }

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

    private fun mostrarTempoReal() {

        selecionarBotao(btnTempoReal)

        cardMetaMensal.visibility = View.GONE

        txvTituloGrafico.text = "Consumo de energia atual"

        txvSubtituloGrafico.text =
            "Veja o consumo de energia em tempo real"

        // Dados temporários
        // TODO: Substituir pelas medições da API

        grafico.atualizarGraficoLinha(
            listOf(
                14f, 8f, 16f, 31f, 22f, 15f
            ),
            listOf(
                "15:00", "15:01", "15:02",
                "15:03", "15:04", "15:05"
            )
        )
    }

    private fun mostrarConsumo() {

        selecionarBotao(btnConsumo)

        cardMetaMensal.visibility = View.VISIBLE

        txvTituloGrafico.text = "Consumo de energia"

        txvSubtituloGrafico.text =
            "Veja o consumo de energia (kWh) em tempo real"

        // Dados temporários
        // TODO: Substituir pelos consumos da API

        grafico.atualizarGrafico(
            listOf(
                5f, 7f, 11f, 16f, 23f,
                29f, 19f, 23f, 13f, 15f
            ),
            listOf(
                "5h", "6h", "7h", "8h", "9h",
                "10h", "11h", "12h", "13h", "14h"
            )
        )
    }

    private fun mostrarValorConta() {

        selecionarBotao(btnValorConta)

        cardMetaMensal.visibility = View.GONE

        txvTituloGrafico.text = "Valor da conta (estimado)"

        txvSubtituloGrafico.text =
            "Comparativo dos últimos meses"

        // Dados temporários
        // TODO: Substituir pelos valores estimados da API

        grafico.atualizarGrafico(
            listOf(
                160f, 190f, 140f,
                160f, 110f, 120f
            ),
            listOf(
                "Abr", "Mai", "Jun",
                "Jul", "Ago", "Set"
            )
        )
    }

    override fun onStart() {
        super.onStart()
        handler.post(atualizarRelogio)
    }

    override fun onStop() {
        handler.removeCallbacks(atualizarRelogio)
        super.onStop()
    }
}

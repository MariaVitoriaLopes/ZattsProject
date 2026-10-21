
package com.zattsproject.mobile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.zattsproject.mobile.View.GraficoConsumoView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var txvHorario: TextView
    private lateinit var txvData: TextView

    private lateinit var btnConsumo: TextView
    private lateinit var btnResumo: TextView
    private lateinit var btnValorConta: TextView

    private lateinit var txvTituloGrafico: TextView
    private lateinit var txvSubtituloGrafico: TextView
    private lateinit var grafico: GraficoConsumoView

    private val handler = Handler(Looper.getMainLooper())

    private val atualizarRelogio = object : Runnable {
        override fun run() {
            atualizarDataHora()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        txvHorario = findViewById(R.id.txv_horario)
        txvData = findViewById(R.id.txv_data)

        btnConsumo = findViewById(R.id.btn_consumo)
        btnResumo = findViewById(R.id.btn_resumo)
        btnValorConta = findViewById(R.id.btn_valorConta)

        txvTituloGrafico = findViewById(R.id.txv_tituloGrafico)
        txvSubtituloGrafico = findViewById(R.id.txv_subtituloGrafico)
        grafico = findViewById(R.id.graficoConsumo)

        configurarIndicadores()
        configurarBotoes()

        mostrarConsumo()
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
            ((consumoMensal / metaMensal) * 100)
                .toInt()
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

        findViewById<ProgressBar>(
            R.id.progressoMeta
        ).progress = porcentagem.coerceIn(0, 100)
    }

    private fun configurarBotoes() {
        btnConsumo.setOnClickListener {
            mostrarConsumo()
        }

        btnResumo.setOnClickListener {
            mostrarResumo()
        }

        btnValorConta.setOnClickListener {
            mostrarValorConta()
        }
    }

    private fun selecionarBotao(selecionado: TextView) {
        val botoes = listOf(
            btnConsumo,
            btnResumo,
            btnValorConta
        )

        botoes.forEach { botao ->
            botao.background = null
        }

        selecionado.setBackgroundResource(
            R.drawable.bg_tab_selecionada
        )
    }

    private fun mostrarConsumo() {
        selecionarBotao(btnConsumo)

        txvTituloGrafico.text = "Consumo de energia"

        txvSubtituloGrafico.text =
            "Veja o consumo de energia (kWh) por horário"

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

    private fun mostrarResumo() {
        selecionarBotao(btnResumo)

        txvTituloGrafico.text = "Resumo semanal"

        txvSubtituloGrafico.text =
            "Consumo diário de energia nos últimos 7 dias"

        grafico.atualizarGrafico(
            listOf(
                22f, 27f, 19f, 34f,
                29f, 25f, 33f
            ),
            listOf(
                "Seg", "Ter", "Qua", "Qui",
                "Sex", "Sáb", "Dom"
            )
        )
    }

    private fun mostrarValorConta() {
        selecionarBotao(btnValorConta)

        txvTituloGrafico.text = "Valor aproximado da conta"

        txvSubtituloGrafico.text =
            "Evolução do custo estimado em reais (R$)"

        grafico.atualizarGrafico(
            listOf(
                18f, 35f, 51f, 68f,
                84f, 101f
            ),
            listOf(
                "1", "5", "10", "15",
                "20", "25"
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

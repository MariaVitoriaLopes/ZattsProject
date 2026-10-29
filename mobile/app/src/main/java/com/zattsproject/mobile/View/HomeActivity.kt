
package com.zattsproject.mobile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zattsproject.mobile.View.GraficoConsumoView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import java.text.DateFormatSymbols
import android.content.res.ColorStateList

class HomeActivity : AppCompatActivity() {

    private var corAplicada = ""
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
        AparenciaManager.aplicarCor(this)
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

        val progressoMeta = findViewById<ProgressBar>(R.id.progressoMeta)

        // Pega a cor escolhida pelo usuário nas Preferências
        val corSelecionada = AparenciaManager.corHex(this)

        // Aplica a cor na barra de progresso
        progressoMeta.progressTintList =
            ColorStateList.valueOf(corSelecionada)

        // Atualiza o progresso normalmente
        progressoMeta.progress = porcentagem.coerceIn(0, 100)
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

        txvTituloGrafico.text = "Resumo comparativo"

        txvSubtituloGrafico.text =
            "Veja o gasto em kWh durante os últimos 6 meses"

        val calendario = Calendar.getInstance()
        val idioma = Locale("pt", "BR")

        // Começa cinco meses antes do mês atual.
        calendario.add(Calendar.MONTH, -5)

        val nomesMeses = DateFormatSymbols(idioma).months

        val rotulos = mutableListOf<String>()

        repeat(6) {
            val indiceMes = calendario.get(Calendar.MONTH)

            val nomeMes = nomesMeses[indiceMes]
                .replaceFirstChar { it.titlecase(idioma) }

            rotulos.add(nomeMes)

            calendario.add(Calendar.MONTH, 1)
        }

        // DADOS TEMPORÁRIOS
        // TODO: COLOQUE AQUI O CAMINHO DA API.
        // Buscar no Spring Boot os consumos mensais
        // dos últimos seis meses, incluindo o atual.
        //
        // A API deve devolver os valores na mesma
        // ordem dos meses exibidos no gráfico.

        val consumosMensais = listOf(
            150f,
            35f,
            185f,
            485f,
            305f,
            170f
        )

        grafico.atualizarGraficoLinha(
            consumosMensais,
            rotulos
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

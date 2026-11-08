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

class HistoryActivity : AppCompatActivity() {

    private enum class TipoHistorico {
        SEMANAL, MENSAL, COMPARATIVO
    }

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

    // DADOS DE DEMONSTRAÇÃO:
    // Substituir pelos valores recebidos da API quando o endpoint estiver disponível.
    private val consumoSemanal = listOf(23f, 20f, 19f, 23f, 26f, 29f, 30f)
    private val consumoMensal = listOf(19f, 24f, 16f, 19f, 11f, 12f, 16f)
    private val consumoOutubro = listOf(4f, 7f, 13f, 19f, 24f, 26f, 28f)
    private val consumoSetembro = listOf(4f, 6f, 8f, 14f, 20f, 25f, 28f)

    private val verde = android.graphics.Color.rgb(220, 255, 0)
    private val laranja = android.graphics.Color.rgb(255, 88, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        findViewById<AppNavBar>(R.id.navBar)
            .setSelected(NavTab.HISTORY)

        inicializarComponentes()
        configurarAbas()
        configurarPeriodo()
        configurarExportacao()
        atualizarTela(TipoHistorico.SEMANAL)
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
        txvTituloHistorico.text = "Histórico Semanal"
        btnSelecionarPeriodo.text = "18/Out - 24/Out de 2026"

        layoutIndicadores.visibility = View.VISIBLE
        cardComparacaoValores.visibility = View.VISIBLE

        txvTituloIndicador1.text = "Consumo de energia"
        txvValorIndicador1.text = "68,3 kWh"
        txvDescricaoIndicador1.text = "Total de energia consumida na semana"

        txvTituloIndicador2.text = "Custo estimado"
        txvValorIndicador2.text = "45,00 Reais"
        txvDescricaoIndicador2.text = "Valor estimado na semana com base na tarifa"

        txvTituloComparacao.text = "Valor total por semana"
        txvRotuloAtual.text = "Semana de 18 de Out. - 24 de Out. 2026"
        txvValorAtual.text = "R$ 45,00"
        txvRotuloAnterior.text = "Semana anterior"
        txvValorAnterior.text = "R$ 43,50"

        txvTituloGrafico.text = "Consumo de energia semanal"
        txvDescricaoGrafico.text =
            "Veja o consumo de energia (kWh) de cada dia da semana"

        configurarLegenda("Consumo diário", null)
        graficoHistorico.configurar(
            modo = GraficoHistoricoView.Modo.LINHA_AREA,
            valores = consumoSemanal,
            rotulos = listOf("Segunda\n18/10", "Terça\n19/10", "Quarta\n20/10",
                "Quinta\n21/10", "Sexta\n22/10", "Sábado\n23/10", "Domingo\n24/10"),
            corPrincipal = verde
        )
        indicadorRolagemGrafico.visibility = View.GONE
        txvObservacaoGrafico.visibility = View.GONE
    }

    private fun mostrarMensal() {
        txvTituloHistorico.text = "Histórico Mensal"
        btnSelecionarPeriodo.text = "Outubro de 2026"

        layoutIndicadores.visibility = View.VISIBLE
        cardComparacaoValores.visibility = View.VISIBLE

        txvTituloIndicador1.text = "Consumo de energia"
        txvValorIndicador1.text = "160 kWh"
        txvDescricaoIndicador1.text = "Total de energia consumida no mês"

        txvTituloIndicador2.text = "Custo estimado"
        txvValorIndicador2.text = "138,00 Reais"
        txvDescricaoIndicador2.text = "Valor estimado no mês com base na tarifa"

        txvTituloComparacao.text = "Valor total por mês"
        txvRotuloAtual.text = "Mês de Outubro"
        txvValorAtual.text = "R$ 138,00"
        txvRotuloAnterior.text = "Mês anterior"
        txvValorAnterior.text = "R$ 142,10"

        txvTituloGrafico.text = "Consumo de energia mensal"
        txvDescricaoGrafico.text =
            "Veja o consumo de energia (kWh) de cada semana do mês"

        configurarLegenda("Consumo diário", null)
        graficoHistorico.configurar(
            modo = GraficoHistoricoView.Modo.BARRAS,
            valores = consumoMensal,
            rotulos = listOf("01/10", "02/10", "03/10", "04/10", "05/10", "06/10", "07/10"),
            corPrincipal = verde
        )
        indicadorRolagemGrafico.visibility = View.VISIBLE
        txvObservacaoGrafico.visibility = View.VISIBLE
    }

    private fun mostrarComparativo() {
        txvTituloHistorico.text = "Histórico Comparativo"
        btnSelecionarPeriodo.text = "Out. e Set. de 2026"

        layoutIndicadores.visibility = View.GONE
        cardComparacaoValores.visibility = View.VISIBLE

        // O comparativo utiliza cards de resumo de consumo e custo.
        // Os valores serão alimentados pela API quando o endpoint estiver disponível.
        txvTituloComparacao.text = "Valor total por mês"
        txvRotuloAtual.text = "Mês de Outubro"
        txvValorAtual.text = "R$ 138,00"
        txvRotuloAnterior.text = "Mês anterior"
        txvValorAnterior.text = "R$ 142,10"

        txvTituloGrafico.text = "Comparativo entre meses"
        txvDescricaoGrafico.text =
            "Veja o consumo de energia (kWh) comparativo entre os meses"

        configurarLegenda("Consumo em Outubro", "Consumo em Setembro")
        graficoHistorico.configurar(
            modo = GraficoHistoricoView.Modo.COMPARATIVO,
            valores = consumoOutubro,
            valoresSecundarios = consumoSetembro,
            rotulos = listOf("01/10", "02/10", "03/10", "04/10", "05/10", "06/10", "07/10"),
            corPrincipal = verde,
            corSecundaria = laranja
        )
        indicadorRolagemGrafico.visibility = View.VISIBLE
        txvObservacaoGrafico.visibility = View.VISIBLE
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
        val maximo = max(50f, max(valores.maxOrNull() ?: 0f,
            valoresSecundarios.maxOrNull() ?: 0f) + 5f)

        desenharGrade(canvas, esquerda, direita, topo, base, altura, maximo)

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

    private fun desenharGrade(
        canvas: Canvas,
        esquerda: Float,
        direita: Float,
        topo: Float,
        base: Float,
        altura: Float,
        maximo: Float
    ) {
        tinta.color = corGrade
        tinta.strokeWidth = dp(0.7f)
        tinta.style = Paint.Style.STROKE

        for (i in 0..5) {
            val y = base - (altura * i / 5f)
            canvas.drawLine(esquerda, y, direita, y, tinta)

            tinta.style = Paint.Style.FILL
            tinta.textSize = dp(11f)
            tinta.color = corTexto
            tinta.textAlign = Paint.Align.RIGHT
            canvas.drawText(
                ((maximo * i / 5f).toInt()).toString(),
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
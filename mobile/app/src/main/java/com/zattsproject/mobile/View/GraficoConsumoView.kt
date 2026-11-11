
package com.zattsproject.mobile.View

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Locale
import kotlin.math.max

class GraficoConsumoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val graficoBarras = BarChart(context)
    private val graficoLinha = LineChart(context)

    private val corPrimaria: Int
        get() {
            val valor = TypedValue()

            return if (
                context.theme.resolveAttribute(
                    com.zattsproject.mobile.R.attr.corDestaque,
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
                Color.parseColor("#E6FB1A")
            }
        }

    private val corTexto: Int
        get() {
            val valor = TypedValue()

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
                Color.DKGRAY
            }
        }

    private val corGrade = Color.parseColor("#DDDDDD")

    init {

        addView(
            graficoBarras,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )

        addView(
            graficoLinha,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )

        configurarGrafico(graficoBarras)
        configurarGrafico(graficoLinha)

        graficoLinha.visibility = View.GONE
    }

    // =====================================================
    // CONFIGURAÇÃO GERAL
    // =====================================================

    private fun configurarGrafico(grafico: Chart<*>) {

        grafico.description.isEnabled = false
        grafico.legend.isEnabled = false

        grafico.setNoDataText("Nenhum dado disponível")

        grafico.setBackgroundColor(Color.TRANSPARENT)
        grafico.setTouchEnabled(false)

        grafico.setExtraOffsets(
            8f,
            12f,
            12f,
            12f
        )

        when (grafico) {

            is BarChart -> {
                grafico.setScaleEnabled(false)
                grafico.setPinchZoom(false)
                grafico.setDrawGridBackground(false)
                grafico.setDrawBarShadow(false)
                grafico.setDrawBorders(false)
                grafico.setFitBars(true)
                grafico.axisRight.isEnabled = false
            }

            is LineChart -> {
                grafico.setScaleEnabled(false)
                grafico.setPinchZoom(false)
                grafico.setDrawGridBackground(false)
                grafico.setDrawBorders(false)
                grafico.axisRight.isEnabled = false
            }
        }

        val xAxis = when (grafico) {
            is BarChart -> grafico.xAxis
            is LineChart -> grafico.xAxis
            else -> return
        }

        val axisLeft = when (grafico) {
            is BarChart -> grafico.axisLeft
            is LineChart -> grafico.axisLeft
            else -> return
        }

        xAxis.apply {

            position = XAxis.XAxisPosition.BOTTOM

            setDrawGridLines(false)
            setDrawAxisLine(false)

            textColor = corTexto
            textSize = 9f

            granularity = 1f
            isGranularityEnabled = true

            labelRotationAngle = 0f

            yOffset = 10f

            setAvoidFirstLastClipping(true)
        }

        axisLeft.apply {

            axisMinimum = 0f

            setDrawGridLines(true)

            gridColor = corGrade
            gridLineWidth = 0.7f

            setDrawAxisLine(false)

            textColor = corTexto
            textSize = 9f
        }

        configurarFormatacaoEixoY(axisLeft, 0f)
    }

    // =====================================================
    // GRÁFICO DE BARRAS
    // =====================================================

    fun atualizarGrafico(
        novosValores: List<Float>,
        novosRotulos: List<String>
    ) {

        require(novosValores.size == novosRotulos.size) {
            "Valores e rótulos devem ter o mesmo tamanho"
        }

        graficoLinha.visibility = View.GONE
        graficoBarras.visibility = View.VISIBLE

        if (novosValores.isEmpty()) {
            graficoBarras.clear()
            graficoBarras.invalidate()
            return
        }

        val entradas = novosValores.mapIndexed { indice, valor ->

            BarEntry(
                indice.toFloat(),
                valor.coerceAtLeast(0f)
            )
        }

        val conjunto = BarDataSet(
            entradas,
            "Consumo"
        ).apply {

            color = corPrimaria

            setDrawValues(false)

            highLightAlpha = 0
        }

        graficoBarras.data = BarData(conjunto).apply {
            barWidth = 0.62f
        }

        configurarEixoX(
            graficoBarras.xAxis,
            novosRotulos
        )

        configurarEscala(
            graficoBarras.axisLeft,
            novosValores
        )

        graficoBarras.notifyDataSetChanged()
        graficoBarras.invalidate()
    }

    // =====================================================
    // GRÁFICO DE LINHA - TEMPO REAL
    // =====================================================

    fun atualizarGraficoLinha(
        novosValores: List<Float>,
        novosRotulos: List<String>
    ) {

        require(novosValores.size == novosRotulos.size) {
            "Valores e rótulos devem ter o mesmo tamanho"
        }

        graficoBarras.visibility = View.GONE
        graficoLinha.visibility = View.VISIBLE

        if (novosValores.isEmpty()) {
            graficoLinha.clear()
            graficoLinha.invalidate()
            return
        }

        val entradas = novosValores.mapIndexed { indice, valor ->

            Entry(
                indice.toFloat(),
                valor.coerceAtLeast(0f)
            )
        }

        val conjunto = LineDataSet(
            entradas,
            "Consumo em tempo real"
        ).apply {

            color = corPrimaria

            lineWidth = 2f

            setDrawCircles(true)
            setCircleColor(corPrimaria)

            circleRadius = 4f

            setDrawCircleHole(false)

            setDrawValues(false)

            setDrawFilled(true)

            fillColor = corPrimaria
            fillAlpha = 45

            mode = LineDataSet.Mode.LINEAR

            isHighlightEnabled = false
        }

        graficoLinha.data = LineData(conjunto)

        configurarEixoX(
            graficoLinha.xAxis,
            novosRotulos
        )

        configurarEscala(
            graficoLinha.axisLeft,
            novosValores
        )

        graficoLinha.resetZoom()
        graficoLinha.notifyDataSetChanged()
        graficoLinha.invalidate()

    }

    // =====================================================
    // CONFIGURAÇÃO DO EIXO X
    // =====================================================

    private fun configurarEixoX(
        eixo: XAxis,
        rotulos: List<String>
    ) {

        eixo.apply {

            valueFormatter = IndexAxisValueFormatter(rotulos)

            axisMinimum = 0f

            axisMaximum =
                if (rotulos.size > 1) {
                    (rotulos.size - 1).toFloat()
                } else {
                    1f
                }

            granularity = 1f
            isGranularityEnabled = true

            setLabelCount(rotulos.size, true)

            setAvoidFirstLastClipping(true)

            position = XAxis.XAxisPosition.BOTTOM

            labelRotationAngle = 0f

            textColor = corTexto
            textSize = 9f
        }
    }

    // =====================================================
    // ESCALA DINÂMICA DO EIXO Y
    // =====================================================



    private fun configurarEscala(
        eixo: com.github.mikephil.charting.components.YAxis,
        valores: List<Float>
    ) {
        val maiorValor = valores.maxOrNull()?.coerceAtLeast(0f) ?: 0f

        // Adiciona 5% de espaço acima do maior consumo
        val limiteSuperior = if (maiorValor > 0f) {
            maiorValor * 1.05f
        } else {
            0.00001f
        }

        eixo.axisMinimum = 0f
        eixo.axisMaximum = limiteSuperior

        // Mantém cinco marcações no eixo Y
        eixo.setLabelCount(5, true)

        eixo.granularity = limiteSuperior / 4f
        eixo.isGranularityEnabled = true

        // Formata os valores pequenos sem transformá-los em zero
        val casasDecimais = when {
            maiorValor < 0.001f -> 7
            maiorValor < 0.01f -> 6
            maiorValor < 0.1f -> 5
            maiorValor < 1f -> 3
            else -> 2
        }

        eixo.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(
                value: Float,
                axis: AxisBase?
            ): String {
                return String.format(
                    java.util.Locale("pt", "BR"),
                    "%.${casasDecimais}f",
                    value
                )
            }
        }
    }


    // =====================================================
    // FORMATAÇÃO DOS VALORES
    // =====================================================

    private fun configurarFormatacaoEixoY(
        eixo: YAxis,
        maiorValor: Float
    ) {

        val casasDecimais = when {

            maiorValor > 0f && maiorValor < 0.01f -> 6

            maiorValor < 1f -> 5

            maiorValor < 10f -> 3

            else -> 2
        }

        eixo.valueFormatter = object : ValueFormatter() {

            override fun getAxisLabel(
                value: Float,
                axis: AxisBase?
            ): String {

                return String.format(
                    Locale("pt", "BR"),
                    "%.${casasDecimais}f",
                    value
                )
            }
        }
    }
}

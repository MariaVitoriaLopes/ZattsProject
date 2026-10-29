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
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlin.math.ceil
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

    private fun configurarGrafico(
        grafico: Chart<*>
    ) {
        grafico.description.isEnabled = false
        grafico.legend.isEnabled = false
        grafico.setNoDataText("Nenhum dado disponível")
        grafico.setBackgroundColor(Color.TRANSPARENT)

        grafico.setTouchEnabled(false)

        when (grafico) {
            is BarChart -> {
                grafico.setScaleEnabled(false)
                grafico.setPinchZoom(false)
                grafico.setDrawGridBackground(false)
                grafico.setDrawBarShadow(false)
                grafico.setDrawBorders(false)
            }

            is LineChart -> {
                grafico.setScaleEnabled(false)
                grafico.setPinchZoom(false)
                grafico.setDrawGridBackground(false)
                grafico.setDrawBorders(false)
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

        when (grafico) {
            is BarChart -> grafico.axisRight.isEnabled = false
            is LineChart -> grafico.axisRight.isEnabled = false
        }

        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textColor = corTexto
            textSize = 9f
            granularity = 1f
            labelRotationAngle = 0f
            yOffset = 12f
        }

        axisLeft.apply {
            axisMinimum = 0f
            setDrawGridLines(true)
            gridColor = corGrade
            gridLineWidth = 0.7f
            setDrawAxisLine(false)
            textColor = corTexto
            textSize = 10f

            valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(
                    value: Float,
                    axis: AxisBase?
                ): String {
                    return value.toInt().toString()
                }
            }
        }

        grafico.setExtraOffsets(4f, 12f, 4f, 4f)
    }

    // GRÁFICO DE BARRAS: CONSUMO E VALOR DA CONTA
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
            return
        }

        val entradas = novosValores.mapIndexed { indice, valor ->
            BarEntry(indice.toFloat(), valor.coerceAtLeast(0f))
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

    // GRÁFICO DE LINHA: RESUMO DOS ÚLTIMOS 6 MESES
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
            return
        }

        val entradas = novosValores.mapIndexed { indice, valor ->
            Entry(indice.toFloat(), valor.coerceAtLeast(0f))
        }

        val conjunto = LineDataSet(
            entradas,
            "Consumo mensal"
        ).apply {
            color = corPrimaria
            lineWidth = 1.5f

            setDrawCircles(true)
            setCircleColor(corPrimaria)
            circleRadius = 4.5f
            setDrawCircleHole(false)

            setDrawValues(false)
            setDrawFilled(true)

            fillColor = corPrimaria
            fillAlpha = 55

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

        graficoLinha.notifyDataSetChanged()
        graficoLinha.invalidate()
    }

    private fun configurarEixoX(
        eixo: XAxis,
        rotulos: List<String>
    ) {
        eixo.apply {
            valueFormatter = IndexAxisValueFormatter(rotulos)
            axisMinimum = -0.1f
            axisMaximum = (rotulos.size - 1).toFloat() + 0.1f
            granularity = 1f
            setLabelCount(rotulos.size, true)
            textColor = corTexto
        }
    }

    private fun configurarEscala(
        eixo: com.github.mikephil.charting.components.YAxis,
        valores: List<Float>
    ) {
        val maiorValor = max(
            10f,
            valores.maxOrNull() ?: 10f
        )

        val intervalo = when {
            maiorValor <= 50f -> 10f
            maiorValor <= 100f -> 20f
            maiorValor <= 500f -> 100f
            else -> 200f
        }

        eixo.axisMinimum = 0f
        eixo.axisMaximum =
            ceil(maiorValor / intervalo) * intervalo

        eixo.setLabelCount(
            (eixo.axisMaximum / intervalo).toInt() + 1,
            true
        )
    }
}

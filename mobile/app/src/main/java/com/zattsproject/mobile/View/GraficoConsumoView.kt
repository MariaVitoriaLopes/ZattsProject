
package com.zattsproject.mobile.View

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlin.math.ceil
import kotlin.math.max

class GraficoConsumoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BarChart(context, attrs) {

    private val corPrimaria = Color.parseColor("#E6FB1A")
    private val corTexto = Color.parseColor("#555555")
    private val corGrade = Color.parseColor("#DDDDDD")

    private var valores = listOf(
        5f, 7f, 11f, 16f, 23f,
        29f, 19f, 23f, 13f, 15f
    )

    private var rotulos = listOf(
        "5h", "6h", "7h", "8h", "9h",
        "10h", "11h", "12h", "13h", "14h"
    )

    init {
        configurarGrafico()
        atualizarGrafico(valores, rotulos)
    }

    private fun configurarGrafico() {
        description.isEnabled = false
        legend.isEnabled = false

        setTouchEnabled(false)
        setScaleEnabled(false)
        setPinchZoom(false)
        setDrawGridBackground(false)
        setDrawBarShadow(false)
        setDrawBorders(false)

        axisRight.isEnabled = false

        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textColor = corTexto
            textSize = 9f
            granularity = 1f
            setCenterAxisLabels(false)
            labelRotationAngle = 0f
        }

        axisLeft.apply {
            axisMinimum = 0f
            setDrawGridLines(true)
            gridColor = corGrade
            gridLineWidth = 1f
            setDrawAxisLine(false)
            textColor = corTexto
            textSize = 10f
            setLabelCount(6, true)

            valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(
                    value: Float,
                    axis: AxisBase?
                ): String {
                    return value.toInt().toString()
                }
            }
        }

        setExtraOffsets(4f, 12f, 4f, 4f)
    }

    fun atualizarGrafico(
        novosValores: List<Float>,
        novosRotulos: List<String>
    ) {
        require(novosValores.size == novosRotulos.size) {
            "Valores e rótulos devem ter o mesmo tamanho"
        }

        valores = novosValores
        rotulos = novosRotulos

        if (valores.isEmpty()) {
            clear()
            invalidate()
            return
        }

        val entradas = valores.mapIndexed { indice, valor ->
            BarEntry(indice.toFloat(), valor.coerceAtLeast(0f))
        }

        val conjunto = BarDataSet(entradas, "Consumo").apply {
            color = corPrimaria
            setDrawValues(false)
            setHighLightAlpha(0)
        }

        val dados = BarData(conjunto).apply {
            barWidth = 0.62f
        }

        data = dados

        xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(rotulos)
            axisMinimum = -0.5f
            axisMaximum = valores.size - 0.5f
            labelCount = valores.size
        }

        val maiorValor = max(
            10f,
            valores.maxOrNull() ?: 10f
        )

        val valorMaximo = ceil(maiorValor / 10f) * 10f

        axisLeft.axisMaximum = valorMaximo

        notifyDataSetChanged()
        invalidate()
    }
}

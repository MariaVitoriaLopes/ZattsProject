
package com.zattsproject.mobile.View

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.ceil
import kotlin.math.max

class GraficoConsumoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val densidade = resources.displayMetrics.density
    private val escalaTexto = resources.displayMetrics.scaledDensity

    private val corPrimaria = Color.parseColor("#E6FB1A")
    private val corTexto = Color.parseColor("#555555")
    private val corGrade = Color.parseColor("#DDDDDD")

    private val pintura = Paint(Paint.ANTI_ALIAS_FLAG)

    private var valores = listOf(
        5f, 7f, 11f, 16f, 23f,
        29f, 19f, 23f, 13f, 15f
    )

    private var rotulos = listOf(
        "5h", "6h", "7h", "8h", "9h",
        "10h", "11h", "12h", "13h", "14h"
    )

    fun atualizarGrafico(
        novosValores: List<Float>,
        novosRotulos: List<String>
    ) {
        require(novosValores.size == novosRotulos.size) {
            "Valores e rótulos devem ter o mesmo tamanho"
        }

        valores = novosValores
        rotulos = novosRotulos
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (valores.isEmpty()) return

        val esquerda = 35f * densidade
        val direita = width - 4f * densidade
        val topo = 12f * densidade
        val base = height - 30f * densidade

        if (direita <= esquerda || base <= topo) return

        val alturaGrafico = base - topo

        val maiorValor = max(
            10f,
            valores.maxOrNull() ?: 10f
        )

        val valorMaximo = ceil(maiorValor / 10f) * 10f

        // Linhas horizontais e valores do eixo Y
        for (i in 0..5) {
            val y = base - (alturaGrafico / 5f) * i
            val valor = valorMaximo / 5f * i

            pintura.color = corGrade
            pintura.strokeWidth = 1f * densidade

            canvas.drawLine(
                esquerda, y, direita, y, pintura
            )

            pintura.color = corTexto
            pintura.textSize = 10f * escalaTexto
            pintura.textAlign = Paint.Align.RIGHT

            canvas.drawText(
                valor.toInt().toString(),
                esquerda - 8f * densidade,
                y + 4f * densidade,
                pintura
            )
        }

        val larguraUtil = direita - esquerda
        val larguraGrupo = larguraUtil / valores.size
        val larguraBarra = larguraGrupo * 0.62f

        valores.forEachIndexed { indice, valor ->

            val centroX = esquerda +
                    larguraGrupo * (indice + 0.5f)

            val alturaBarra = (
                    valor.coerceAtLeast(0f) / valorMaximo
                    ) * alturaGrafico

            val retangulo = RectF(
                centroX - larguraBarra / 2f,
                base - alturaBarra,
                centroX + larguraBarra / 2f,
                base
            )

            pintura.color = corPrimaria

            canvas.drawRoundRect(
                retangulo,
                5f * densidade,
                5f * densidade,
                pintura
            )

            pintura.color = corTexto
            pintura.textSize = 9f * escalaTexto
            pintura.textAlign = Paint.Align.CENTER

            canvas.drawText(
                rotulos[indice],
                centroX,
                height - 9f * densidade,
                pintura
            )
        }
    }
}

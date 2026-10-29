package com.zattsproject.mobile

import android.app.Activity
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatDelegate

object AparenciaManager {

    private const val PREFS = "zatts_aparencia"
    private const val CHAVE_COR = "cor_destaque"
    private const val CHAVE_TEMA = "tema"
    private const val CHAVE_IDIOMA = "idioma"

    const val PADRAO = "padrao"
    const val LARANJA = "laranja"
    const val VERMELHO = "vermelho"
    const val AZUL = "azul"
    const val ROXO = "roxo"

    fun obterCor(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(CHAVE_COR, PADRAO) ?: PADRAO

    fun salvarCor(context: Context, cor: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(CHAVE_COR, cor)
            .apply()
    }

    fun obterTema(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(
                CHAVE_TEMA,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )

    fun salvarTema(context: Context, modo: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(CHAVE_TEMA, modo)
            .apply()

        AppCompatDelegate.setDefaultNightMode(modo)
    }

    fun obterIdioma(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(CHAVE_IDIOMA, "pt-BR") ?: "pt-BR"

    fun salvarIdioma(context: Context, idioma: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(CHAVE_IDIOMA, idioma)
            .apply()
    }

    fun corHex(context: Context): Int {
        val corRes = when (obterCor(context)) {
            LARANJA -> R.color.destaque_laranja
            VERMELHO -> R.color.destaque_vermelho
            AZUL -> R.color.destaque_azul
            ROXO -> R.color.destaque_roxo
            else -> R.color.destaque_padrao
        }

        return ContextCompat.getColor(context, corRes)
    }

    fun aplicarCor(activity: Activity) {
        val estilo = when (obterCor(activity)) {
            LARANJA -> R.style.ZattsAccentLaranja
            VERMELHO -> R.style.ZattsAccentVermelho
            AZUL -> R.style.ZattsAccentAzul
            ROXO -> R.style.ZattsAccentRoxo
            else -> R.style.ZattsAccentPadrao
        }

        activity.theme.applyStyle(estilo, true)
    }
}

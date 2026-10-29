package com.zattsproject.mobile

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.zattsproject.mobile.R

class PreferenciasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_preferencias)

        findViewById<TextView>(
            R.id.btnVoltarPreferencias
        ).setOnClickListener {
            finish()
        }

        configurarTema()
        configurarCores()
        configurarIdioma()
    }

    private fun configurarTema() {
        val spinner = findViewById<Spinner>(R.id.spinnerTema)

        val opcoes = listOf(
            "Automático (sistema)",
            "Claro",
            "Escuro"
        )

        spinner.adapter = criarAdapter(opcoes)

        val modoAtual = AparenciaManager.obterTema(this)

        val indice = when (modoAtual) {
            AppCompatDelegate.MODE_NIGHT_NO -> 1
            AppCompatDelegate.MODE_NIGHT_YES -> 2
            else -> 0
        }

        spinner.setSelection(indice)

        spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val novoModo = when (position) {
                        1 -> AppCompatDelegate.MODE_NIGHT_NO
                        2 -> AppCompatDelegate.MODE_NIGHT_YES
                        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }

                    if (novoModo != AparenciaManager.obterTema(this@PreferenciasActivity)) {
                        AparenciaManager.salvarTema(
                            this@PreferenciasActivity,
                            novoModo
                        )
                    }
                }
            }
    }

    private fun configurarCores() {
        val grupo = findViewById<RadioGroup>(R.id.grupoCores)

        val selecionada = when (AparenciaManager.obterCor(this)) {
            AparenciaManager.LARANJA -> R.id.corLaranja
            AparenciaManager.VERMELHO -> R.id.corVermelho
            AparenciaManager.AZUL -> R.id.corAzul
            AparenciaManager.ROXO -> R.id.corRoxo
            else -> R.id.corPadrao
        }

        grupo.check(selecionada)

        grupo.setOnCheckedChangeListener { _, checkedId ->

            val cor = when (checkedId) {
                R.id.corLaranja -> AparenciaManager.LARANJA
                R.id.corVermelho -> AparenciaManager.VERMELHO
                R.id.corAzul -> AparenciaManager.AZUL
                R.id.corRoxo -> AparenciaManager.ROXO
                else -> AparenciaManager.PADRAO
            }

            if (cor != AparenciaManager.obterCor(this)) {
                AparenciaManager.salvarCor(this, cor)
                recreate()
            }
        }
    }

    private fun configurarIdioma() {
        val spinner = findViewById<Spinner>(R.id.spinnerIdioma)

        val idiomas = listOf(
            "Português (Br)",
            "English"
        )

        spinner.adapter = criarAdapter(idiomas)

        val idiomaAtual = AparenciaManager.obterIdioma(this)
        spinner.setSelection(if (idiomaAtual == "en") 1 else 0)

        spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val idioma = if (position == 1) "en" else "pt-BR"

                    if (idioma != AparenciaManager.obterIdioma(this@PreferenciasActivity)) {
                        AparenciaManager.salvarIdioma(
                            this@PreferenciasActivity,
                            idioma
                        )

                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(idioma)
                        )
                    }
                }
            }
    }

    private fun criarAdapter(
        opcoes: List<String>
    ): ArrayAdapter<String> {
        return ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opcoes
        ).apply {
            setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )
        }
    }
}
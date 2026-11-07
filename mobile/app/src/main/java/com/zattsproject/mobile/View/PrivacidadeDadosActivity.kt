package com.zattsproject.mobile

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.zattsproject.mobile.AparenciaManager
import com.zattsproject.mobile.AppNavBar
import com.zattsproject.mobile.NavTab
import com.zattsproject.mobile.R

class PrivacidadeDadosActivity : AppCompatActivity() {

    private val preferencias by lazy {
        getSharedPreferences(
            "preferencias_privacidade",
            MODE_PRIVATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_privacidade_dados)

        findViewById<AppNavBar>(R.id.navBar)
            .setSelected(NavTab.SETTINGS)

        configurarVoltar()
        configurarPrivacidade()
        configurarBotoes()
    }

    private fun configurarVoltar() {

        findViewById<TextView>(
            R.id.btnVoltarPrivacidade
        ).setOnClickListener {
            finish()
        }
    }

    private fun configurarPrivacidade() {

        configurarSwitch(
            R.id.switchDadosAnonimos,
            "dados_anonimos"
        )

        configurarSwitch(
            R.id.switchRecomendacoes,
            "recomendacoes_personalizadas"
        )

        configurarSwitch(
            R.id.switchComparacao,
            "comparacao_residencias"
        )
    }

    private fun configurarSwitch(
        id: Int,
        chave: String
    ) {

        val switch = findViewById<SwitchCompat>(id)

        switch.isChecked = preferencias.getBoolean(
            chave,
            true
        )

        switch.setOnCheckedChangeListener { _, ativado ->

            preferencias.edit()
                .putBoolean(chave, ativado)
                .apply()
        }
    }

    private fun configurarBotoes() {

        findViewById<TextView>(
            R.id.btnExportarDados
        ).setOnClickListener {
            exportarDados()
        }

        findViewById<TextView>(
            R.id.btnApagarHistorico
        ).setOnClickListener {
            confirmarExclusaoHistorico()
        }

        findViewById<View>(
            R.id.btnPoliticaPrivacidade
        ).setOnClickListener {
            abrirDocumento("Política de Privacidade")
        }

        findViewById<View>(
            R.id.btnTermosUso
        ).setOnClickListener {
            abrirDocumento("Termos de Uso")
        }
    }

    private fun exportarDados() {

        Toast.makeText(
            this,
            "Exportação de dados: funcionalidade em desenvolvimento.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun confirmarExclusaoHistorico() {

        AlertDialog.Builder(this)
            .setTitle("Apagar histórico de consumo")
            .setMessage(
                "Tem certeza de que deseja apagar todas as leituras de consumo salvas? " +
                        "Esta ação não poderá ser desfeita."
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Apagar") { _, _ ->

                Toast.makeText(
                    this,
                    "Exclusão do histórico: funcionalidade em desenvolvimento.",
                    Toast.LENGTH_LONG
                ).show()
            }
            .show()
    }

    private fun abrirDocumento(nome: String) {

        Toast.makeText(
            this,
            "$nome: documento ainda não disponibilizado.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
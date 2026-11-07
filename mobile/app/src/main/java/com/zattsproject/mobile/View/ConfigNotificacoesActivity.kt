package com.zattsproject.mobile

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.zattsproject.mobile.AparenciaManager
import com.zattsproject.mobile.AppNavBar
import com.zattsproject.mobile.NavTab
import com.zattsproject.mobile.R

class ConfigNotificacoesActivity : AppCompatActivity() {

    private val preferencias by lazy {
        getSharedPreferences(
            "preferencias_notificacoes",
            MODE_PRIVATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_config_notificacoes)

        findViewById<AppNavBar>(R.id.navBar)
            .setSelected(NavTab.SETTINGS)

        configurarVoltar()
        configurarNotificacoes()
    }

    private fun configurarVoltar() {

        findViewById<TextView>(
            R.id.btnVoltarNotificacoes
        ).setOnClickListener {
            finish()
        }
    }

    private fun configurarNotificacoes() {

        configurarSwitch(
            R.id.switchNotificacoesGerais,
            "notificacoes_gerais"
        )

        configurarSwitch(
            R.id.switchNotificacoesConta,
            "notificacoes_conta"
        )

        configurarSwitch(
            R.id.switchNotificacoesConsumo,
            "notificacoes_consumo"
        )

        configurarSwitch(
            R.id.switchNotificacoesMeta,
            "notificacoes_meta"
        )

        configurarSwitch(
            R.id.switchNotificacoesAlerta,
            "notificacoes_alerta"
        )
    }

    private fun configurarSwitch(
        id: Int,
        chave: String
    ) {

        val switch = findViewById<SwitchCompat>(id)

        // Recupera o estado salvo.
        switch.isChecked = preferencias.getBoolean(
            chave,
            true
        )

        // Salva sempre que o usuário alterar.
        switch.setOnCheckedChangeListener { _, ativado ->

            preferencias.edit()
                .putBoolean(chave, ativado)
                .apply()
        }
    }
}
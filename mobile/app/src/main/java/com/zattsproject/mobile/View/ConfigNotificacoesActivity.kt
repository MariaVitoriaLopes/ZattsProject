package com.zattsproject.mobile

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.zattsproject.mobile.AparenciaManager
import com.zattsproject.mobile.AppNavBar
import com.zattsproject.mobile.NavTab
import com.zattsproject.mobile.R
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.zattsproject.mobile.Model.ConfiguracaoNotificacoes
import com.zattsproject.mobile.Network.RetrofitClient
import kotlinx.coroutines.launch

class ConfigNotificacoesActivity : AppCompatActivity() {

    private lateinit var switchGerais: SwitchCompat
    private lateinit var switchConta: SwitchCompat
    private lateinit var switchConsumo: SwitchCompat
    private lateinit var switchMeta: SwitchCompat
    private lateinit var switchAlerta: SwitchCompat

    private var carregandoConfiguracao = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_config_notificacoes)

        findViewById<AppNavBar>(R.id.navBar)
            .setSelected(NavTab.SETTINGS)

        configurarVoltar()
        configurarNotificacoes()
        carregarConfiguracao()
    }

    private fun configurarVoltar() {

        findViewById<TextView>(
            R.id.btnVoltarNotificacoes
        ).setOnClickListener {
            finish()
        }
    }

    private fun configurarNotificacoes() {

        switchGerais =
            findViewById(
                R.id.switchNotificacoesGerais
            )

        switchConta =
            findViewById(
                R.id.switchNotificacoesConta
            )

        switchConsumo =
            findViewById(
                R.id.switchNotificacoesConsumo
            )

        switchMeta =
            findViewById(
                R.id.switchNotificacoesMeta
            )

        switchAlerta =
            findViewById(
                R.id.switchNotificacoesAlerta
            )

        val listener =
            android.widget.CompoundButton
                .OnCheckedChangeListener { _, _ ->

                    if (!carregandoConfiguracao) {
                        salvarConfiguracao()
                    }
                }

        switchGerais
            .setOnCheckedChangeListener(listener)

        switchConta
            .setOnCheckedChangeListener(listener)

        switchConsumo
            .setOnCheckedChangeListener(listener)

        switchMeta
            .setOnCheckedChangeListener(listener)

        switchAlerta
            .setOnCheckedChangeListener(listener)
    }
    private fun carregarConfiguracao() {
        lifecycleScope.launch {
            try {
                val resposta =
                    RetrofitClient.apiService
                        .buscarConfiguracaoNotificacoes()
                if (resposta.isSuccessful) {
                    val configuracao =
                        resposta.body()
                            ?: return@launch

                    carregandoConfiguracao = true

                    switchGerais.isChecked =
                        configuracao.notifGerais

                    switchConta.isChecked =
                        configuracao.notifConta

                    switchConsumo.isChecked =
                        configuracao.notifConsumo

                    switchMeta.isChecked =
                        configuracao.notifMeta

                    switchAlerta.isChecked =
                        configuracao.notifAlerta

                    carregandoConfiguracao = false
                } else {
                    Toast.makeText(
                        this@ConfigNotificacoesActivity,
                        if (resposta.code() == 401) {
                            "Sua sessão expirou. Entre novamente."
                        } else {
                            "Não foi possível carregar as configurações."
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                carregandoConfiguracao = false
                Toast.makeText(
                    this@ConfigNotificacoesActivity,
                    "Não foi possível conectar ao servidor.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun salvarConfiguracao() {

        val configuracao =
            ConfiguracaoNotificacoes(
                notifGerais =
                    switchGerais.isChecked,
                notifConta =
                    switchConta.isChecked,
                notifConsumo =
                    switchConsumo.isChecked,
                notifMeta =
                    switchMeta.isChecked,
                notifAlerta =
                    switchAlerta.isChecked
            )
        lifecycleScope.launch {
            try {
                val resposta =
                    RetrofitClient.apiService
                        .salvarConfiguracaoNotificacoes(
                            configuracao
                        )
                if (!resposta.isSuccessful) {
                    Toast.makeText(
                        this@ConfigNotificacoesActivity,
                        "Não foi possível salvar a configuração.",
                        Toast.LENGTH_SHORT
                    ).show()
                    carregarConfiguracao()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ConfigNotificacoesActivity,
                    "Não foi possível salvar a configuração.",
                    Toast.LENGTH_SHORT
                ).show()
                carregarConfiguracao()
            }
        }
    }
}
package com.zattsproject.mobile

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zattsproject.mobile.AparenciaManager
import com.zattsproject.mobile.AppNavBar
import com.zattsproject.mobile.NavTab
import com.zattsproject.mobile.R

class NovoAlertsActivity : AppCompatActivity() {

    private lateinit var icones: List<ImageView>
    private var iconeSelecionado = R.drawable.ic_bolt

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_novo_alerts)

        findViewById<AppNavBar>(R.id.navBar)
            .setSelected(NavTab.ALERTS)

        configurarIcones()
        configurarSpinners()
        configurarBotoes()
    }

    private fun configurarIcones() {

        icones = listOf(
            findViewById(R.id.iconeRaio),
            findViewById(R.id.iconeBandeira),
            findViewById(R.id.iconeEscudo),
            findViewById(R.id.iconeWifi),
            findViewById(R.id.iconeAlvo),
            findViewById(R.id.iconeRelogio),
            findViewById(R.id.iconeLuz)
        )

        val recursos = listOf(
            R.drawable.ic_bolt,
            R.drawable.ic_flag,
            R.drawable.ic_maybe,
            R.drawable.ic_wifi_off,
            R.drawable.ic_crisis_alert,
            R.drawable.ic_alarm,
            R.drawable.ic_emergency
        )

        icones.forEachIndexed { indice, imagem ->

            imagem.setOnClickListener {
                iconeSelecionado = recursos[indice]
                atualizarIconeSelecionado(indice)
            }
        }

        atualizarIconeSelecionado(0)
    }

    private fun atualizarIconeSelecionado(indice: Int) {

        icones.forEachIndexed { posicao, imagem ->

            imagem.alpha = if (posicao == indice) 1f else 0.55f

            imagem.isSelected = posicao == indice
        }
    }

    private fun configurarSpinners() {

        configurarSpinner(
            R.id.spinnerCategoria,
            listOf("Alerta", "Aviso", "Informação")
        )

        configurarSpinner(
            R.id.spinnerTipoAlerta,
            listOf(
                "Consumo de energia (kWh)",
                "Percentual da meta",
                "Queda de energia",
                "Dispositivo desativado"
            )
        )

        configurarSpinner(
            R.id.spinnerLembretes,
            listOf(
                "Na hora",
                "Após 5 minutos",
                "Após 15 minutos",
                "Após 30 minutos",
                "Após 1 hora"
            )
        )

        configurarSpinner(
            R.id.spinnerRepetir,
            listOf(
                "A cada 1 hora",
                "A cada 2 horas",
                "A cada 6 horas",
                "A cada 12 horas",
                "Uma vez ao dia",
                "Não repetir"
            )
        )
    }

    private fun configurarSpinner(
        id: Int,
        opcoes: List<String>
    ) {

        val spinner = findViewById<Spinner>(id)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opcoes
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinner.adapter = adapter
    }

    private fun configurarBotoes() {

        findViewById<TextView>(R.id.btnVoltar)
            .setOnClickListener {
                finish()
            }

        findViewById<Button>(R.id.btnCancelar)
            .setOnClickListener {
                finish()
            }

        findViewById<Button>(R.id.btnCriarNovoAlerta)
            .setOnClickListener {

                Toast.makeText(
                    this,
                    "Criar alerta: funcionalidade em desenvolvimento.",
                    Toast.LENGTH_SHORT
                ).show()

                // TODO: Adicionar chamada à API para cadastrar o alerta.
                // TODO: Enviar os dados preenchidos e o ícone selecionado.
            }
    }
}
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
import android.widget.CheckBox
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.zattsproject.mobile.Model.Alerta
import com.zattsproject.mobile.Network.RetrofitClient
import kotlinx.coroutines.launch

class NovoAlertsActivity : AppCompatActivity() {

    private lateinit var icones: List<ImageView>
    private var iconeSelecionado = R.drawable.ic_bolt

    private var alertaIdEdicao: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_novo_alerts)

        findViewById<AppNavBar>(R.id.navBar)
            .setSelected(NavTab.ALERTS)

        configurarIcones()
        configurarSpinners()
        carregarDadosEdicao()
        configurarBotoes()
    }

    private fun carregarDadosEdicao() {

        alertaIdEdicao =
            intent.getStringExtra(
                "alertaId"
            )
        if (alertaIdEdicao == null) {
            return
        }
        findViewById<EditText>(
            R.id.edtNomeAlerta
        ).setText(
            intent.getStringExtra(
                "alertaNome"
            ).orEmpty()
        )
        if (
            intent.hasExtra(
                "alertaValorLimite"
            )
        ) {
            findViewById<EditText>(
                R.id.edtLimiteConsumo
            ).setText(
                intent.getDoubleExtra(
                    "alertaValorLimite",
                    0.0
                ).toString()
            )
        }
        if (
            intent.hasExtra(
                "alertaPercentual"
            )
        ) {
            findViewById<EditText>(
                R.id.edtPercentual
            ).setText(
                intent.getDoubleExtra(
                    "alertaPercentual",
                    0.0
                ).toString()
            )
        }
        findViewById<CheckBox>(
            R.id.checkAtivarAlerta
        ).isChecked =
            intent.getBooleanExtra(
                "alertaAtivo",
                true
            )
        findViewById<CheckBox>(
            R.id.checkPush
        ).isChecked =
            intent.getBooleanExtra(
                "alertaCanalPush",
                true
            )
        findViewById<CheckBox>(
            R.id.checkEmail
        ).isChecked =
            intent.getBooleanExtra(
                "alertaCanalEmail",
                true
            )
        findViewById<CheckBox>(
            R.id.checkApenasApp
        ).isChecked =
            intent.getBooleanExtra(
                "alertaCanalApp",
                true
            )
        selecionarCategoria(
            intent.getStringExtra(
                "alertaCategoria"
            )
        )
        selecionarTipo(
            intent.getStringExtra(
                "alertaTipo"
            )
        )
        selecionarLembrete(
            intent.getStringExtra(
                "alertaLembrete"
            )
        )
        selecionarRepeticao(
            intent.getStringExtra(
                "alertaRepetir"
            )
        )
        selecionarIcone(
            intent.getStringExtra(
                "alertaIcone"
            )
        )
        findViewById<Button>(
            R.id.btnCriarNovoAlerta
        ).text =
            "Salvar alterações"
    }

    private fun selecionarCategoria(
        categoria: String?
    ) {
        val posicao =
            when (categoria?.uppercase()) {
                "AVISO" ->
                    1
                "INFORMACAO" ->
                    2
                else ->
                    0
            }
        findViewById<Spinner>(
            R.id.spinnerCategoria
        ).setSelection(posicao)
    }

    private fun selecionarTipo(
        tipo: String?
    ) {
        val posicao =
            when (tipo?.uppercase()) {
                "QUEDA_ENERGIA" ->
                    2
                "DISPOSITIVO_DESATIVADO" ->
                    3
                "CONSUMO" -> {
                    if (
                        intent.hasExtra(
                            "alertaPercentual"
                        ) &&
                        !intent.hasExtra(
                            "alertaValorLimite"
                        )
                    ) {
                        1
                    } else {
                        0
                    }
                }
                else ->
                    0
            }
        findViewById<Spinner>(
            R.id.spinnerTipoAlerta
        ).setSelection(posicao)
    }

    private fun selecionarLembrete(
        lembrete: String?
    ) {
        val posicao =
            when (lembrete?.uppercase()) {
                "5_MIN" ->
                    1
                "15_MIN" ->
                    2
                "30_MIN" ->
                    3
                "1_HORA" ->
                    4
                else ->
                    0
            }
        findViewById<Spinner>(
            R.id.spinnerLembretes
        ).setSelection(posicao)
    }

    private fun selecionarRepeticao(
        repetir: String?
    ) {
        val posicao =
            when (repetir?.uppercase()) {
                "2_HORAS" ->
                    1
                "6_HORAS" ->
                    2
                "12_HORAS" ->
                    3
                "DIARIO" ->
                    4
                "NUNCA" ->
                    5
                else ->
                    0
            }
        findViewById<Spinner>(
            R.id.spinnerRepetir
        ).setSelection(posicao)
    }

    private fun selecionarIcone(
        icone: String?
    ) {
        val indice =
            when (icone?.lowercase()) {
                "flag" ->
                    1
                "shield" ->
                    2
                "wifi_off" ->
                    3
                "crisis_alert" ->
                    4
                "alarm_on",
                "alarm" ->
                    5
                "emergency",
                "power_settings_new" ->
                    6
                else ->
                    0
            }
        val recursos =
            listOf(
                R.drawable.ic_bolt,
                R.drawable.ic_flag,
                R.drawable.ic_maybe,
                R.drawable.ic_wifi_off,
                R.drawable.ic_crisis_alert,
                R.drawable.ic_alarm,
                R.drawable.ic_emergency
            )
        iconeSelecionado =
            recursos[indice]
        atualizarIconeSelecionado(
            indice
        )
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
                salvarAlerta()
            }
    }
    private fun salvarAlerta() {

        val nome =
            findViewById<EditText>(
                R.id.edtNomeAlerta
            ).text.toString().trim()
        val limite =
            findViewById<EditText>(
                R.id.edtLimiteConsumo
            ).text.toString()
                .replace(",", ".")
                .toDoubleOrNull()
        val percentual =
            findViewById<EditText>(
                R.id.edtPercentual
            ).text.toString()
                .replace(",", ".")
                .toDoubleOrNull()
        if (nome.isBlank()) {
            Toast.makeText(
                this,
                "Informe o nome do alerta.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (limite == null &&
            percentual == null) {
            Toast.makeText(
                this,
                "Informe o limite ou o percentual.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val categoriaSelecionada =
            findViewById<Spinner>(
                R.id.spinnerCategoria
            ).selectedItem.toString()
        val tipoSelecionado =
            findViewById<Spinner>(
                R.id.spinnerTipoAlerta
            ).selectedItem.toString()
        val lembreteSelecionado =
            findViewById<Spinner>(
                R.id.spinnerLembretes
            ).selectedItem.toString()
        val repetirSelecionado =
            findViewById<Spinner>(
                R.id.spinnerRepetir
            ).selectedItem.toString()
        val alerta =
            Alerta(
                nome = nome,
                tipo = converterTipo(
                    tipoSelecionado
                ),
                percentualLimite = percentual,
                valorLimite = limite,
                ativo =
                    findViewById<CheckBox>(
                        R.id.checkAtivarAlerta
                    ).isChecked,
                icone =
                    converterIcone(),
                categoria =
                    converterCategoria(
                        categoriaSelecionada
                    ),
                canalPush =
                    findViewById<CheckBox>(
                        R.id.checkPush
                    ).isChecked,
                canalEmail =
                    findViewById<CheckBox>(
                        R.id.checkEmail
                    ).isChecked,
                canalApp =
                    findViewById<CheckBox>(
                        R.id.checkApenasApp
                    ).isChecked,
                lembrete =
                    converterLembrete(
                        lembreteSelecionado
                    ),
                repetir =
                    converterRepeticao(
                        repetirSelecionado
                    )
            )
        lifecycleScope.launch {
            try {
                val idEdicao =
                    alertaIdEdicao
                val resposta =
                    if (idEdicao == null) {
                        RetrofitClient.apiService
                            .criarAlerta(alerta)
                    } else {
                        RetrofitClient.apiService
                            .editarAlerta(
                                idEdicao,
                                alerta
                            )
                    }
                if (resposta.isSuccessful) {
                    Toast.makeText(
                        this@NovoAlertsActivity,
                        if (idEdicao == null) {
                            "Alerta criado com sucesso."
                        } else {
                            "Alerta atualizado com sucesso."
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@NovoAlertsActivity,
                        if (resposta.code() == 401) {
                            "Sua sessão expirou. Entre novamente."
                        } else {
                            "Não foi possível criar o alerta."
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@NovoAlertsActivity,
                    "Não foi possível conectar ao servidor.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun converterCategoria(
        categoria: String
    ): String {

        return when (categoria) {
            "Aviso" -> "AVISO"
            "Informação" -> "INFORMACAO"
            else -> "ALERTA"
        }
    }

    private fun converterTipo(
        tipo: String
    ): String {

        return when (tipo) {

            "Percentual da meta" ->
                "CONSUMO"

            "Queda de energia" ->
                "QUEDA_ENERGIA"

            "Dispositivo desativado" ->
                "DISPOSITIVO_DESATIVADO"

            else ->
                "CONSUMO"
        }
    }

    private fun converterLembrete(
        lembrete: String
    ): String {

        return when (lembrete) {

            "Após 5 minutos" ->
                "5_MIN"

            "Após 15 minutos" ->
                "15_MIN"

            "Após 30 minutos" ->
                "30_MIN"

            "Após 1 hora" ->
                "1_HORA"

            else ->
                "NA_HORA"
        }
    }

    private fun converterRepeticao(
        repetir: String
    ): String {

        return when (repetir) {

            "A cada 2 horas" ->
                "2_HORAS"

            "A cada 6 horas" ->
                "6_HORAS"

            "A cada 12 horas" ->
                "12_HORAS"

            "Uma vez ao dia" ->
                "DIARIO"

            "Não repetir" ->
                "NUNCA"

            else ->
                "1_HORA"
        }
    }

    private fun converterIcone(): String {

        return when (iconeSelecionado) {

            R.drawable.ic_flag ->
                "flag"

            R.drawable.ic_maybe ->
                "shield"

            R.drawable.ic_wifi_off ->
                "wifi_off"

            R.drawable.ic_crisis_alert ->
                "crisis_alert"

            R.drawable.ic_alarm ->
                "alarm_on"

            R.drawable.ic_emergency ->
                "emergency"

            else ->
                "bolt"
        }
    }
}
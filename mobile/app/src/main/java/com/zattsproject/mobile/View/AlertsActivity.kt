package com.zattsproject.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.zattsproject.mobile.NovoAlertsActivity
import androidx.lifecycle.lifecycleScope
import com.zattsproject.mobile.Model.Alerta
import com.zattsproject.mobile.Network.RetrofitClient
import kotlinx.coroutines.launch
import java.util.Locale

class AlertsActivity : AppCompatActivity() {

    private var corAplicada = ""

    private lateinit var listaAlertas: LinearLayout
    private var alertas: List<Alerta> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)

        corAplicada = AparenciaManager.obterCor(this)
        setContentView(R.layout.activity_alerts)

        // Seleciona Alertas na navbar
        findViewById<AppNavBar>(R.id.navBar)
            .setSelected(NavTab.ALERTS)

        listaAlertas = findViewById(R.id.listaAlertas)

        findViewById<TextView>(R.id.btnCriarAlerta)
            .setOnClickListener {
                val intent = Intent(this, NovoAlertsActivity::class.java)
                startActivity(intent)
            }
    }

    override fun onResume() {
        super.onResume()

        val corAtual = AparenciaManager.obterCor(this)

        if (corAtual != corAplicada) {
            recreate()
            return
        }

        carregarAlertas()
    }

    private fun carregarAlertas() {
        lifecycleScope.launch {
            try {
                val resposta =
                    RetrofitClient.apiService.buscarAlertas()
                if (resposta.isSuccessful) {
                    alertas =
                        resposta.body().orEmpty()
                    mostrarAlertas()
                } else {
                    Toast.makeText(
                        this@AlertsActivity,
                        if (resposta.code() == 401) {
                            "Sua sessão expirou. Entre novamente."
                        } else {
                            "Não foi possível carregar os alertas."
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AlertsActivity,
                    "Não foi possível conectar ao servidor.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun mostrarAlertas() {

        listaAlertas.removeAllViews()
        alertas.forEach { alerta ->
            val item = layoutInflater.inflate(
                R.layout.item_alerta,
                listaAlertas,
                false
            )
            item.findViewById<ImageView>(R.id.iconeAlerta)
                .setImageResource(
                    iconeDoAlerta(alerta.icone)
                )
            item.findViewById<TextView>(R.id.txvTituloAlerta)
                .text = alerta.nome ?: "Alerta"
            item.findViewById<TextView>(R.id.txvDescricaoAlerta)
                .text = montarDescricao(alerta)
            item.findViewById<TextView>(R.id.txvCondicao)
                .text = montarCondicao(alerta)
            item.findViewById<TextView>(R.id.txvPeriodo)
                .text = formatarRepeticao(alerta.repetir)
            item.findViewById<View>(R.id.txvEmail)
                .visibility =
                if (alerta.canalEmail == true) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            item.findViewById<View>(R.id.txvPush)
                .visibility =
                if (alerta.canalPush == true) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            val switchAlerta =
                item.findViewById<Switch>(
                    R.id.switchAlerta
                )
            switchAlerta.isChecked =
                alerta.ativo == true
            switchAlerta.setOnCheckedChangeListener { _, ativo ->
                alerta.id?.let { id ->
                    alterarEstado(
                        id,
                        ativo,
                        switchAlerta
                    )
                }
            }
            item.findViewById<ImageButton>(
                R.id.btnEditarAlerta
            ).setOnClickListener {
                val intent =
                    Intent(
                        this,
                        NovoAlertsActivity::class.java
                    ).apply {
                        putExtra(
                            "alertaId",
                            alerta.id
                        )
                        putExtra(
                            "alertaNome",
                            alerta.nome
                        )
                        putExtra(
                            "alertaTipo",
                            alerta.tipo
                        )
                        putExtra(
                            "alertaCategoria",
                            alerta.categoria
                        )
                        putExtra(
                            "alertaIcone",
                            alerta.icone
                        )
                        alerta.valorLimite?.let {
                            putExtra(
                                "alertaValorLimite",
                                it
                            )
                        }
                        alerta.percentualLimite?.let {
                            putExtra(
                                "alertaPercentual",
                                it
                            )
                        }
                        putExtra(
                            "alertaAtivo",
                            alerta.ativo == true
                        )
                        putExtra(
                            "alertaCanalPush",
                            alerta.canalPush == true
                        )
                        putExtra(
                            "alertaCanalEmail",
                            alerta.canalEmail == true
                        )
                        putExtra(
                            "alertaCanalApp",
                            alerta.canalApp == true
                        )
                        putExtra(
                            "alertaLembrete",
                            alerta.lembrete
                        )
                        putExtra(
                            "alertaRepetir",
                            alerta.repetir
                        )
                    }
                startActivity(intent)
            }
            item.findViewById<ImageButton>(
                R.id.btnExcluirAlerta
            ).setOnClickListener {
                alerta.id?.let { id ->
                    confirmarExclusao(id)
                }
            }
            listaAlertas.addView(item)
        }
    }

    private fun alterarEstado(
        id: String,
        ativo: Boolean,
        switchAlerta: Switch
    ) {
        switchAlerta.isEnabled = false

        lifecycleScope.launch {
            try {
                val resposta =
                    RetrofitClient.apiService
                        .ativarAlerta(
                            id,
                            ativo
                        )
                if (!resposta.isSuccessful) {
                    Toast.makeText(
                        this@AlertsActivity,
                        "Não foi possível atualizar o alerta.",
                        Toast.LENGTH_SHORT
                    ).show()
                    carregarAlertas()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AlertsActivity,
                    "Não foi possível atualizar o alerta.",
                    Toast.LENGTH_SHORT
                ).show()
                carregarAlertas()
            } finally {
                switchAlerta.isEnabled = true
            }
        }
    }

    private fun confirmarExclusao(
        id: String
    ) {
        AlertDialog.Builder(this)
            .setTitle("Excluir alerta")
            .setMessage(
                "Deseja realmente excluir este alerta?"
            )
            .setNegativeButton(
                "Cancelar",
                null
            )
            .setPositiveButton("Excluir") { _, _ ->
                excluirAlerta(id)
            }
            .show()
    }

    private fun excluirAlerta(
        id: String
    ) {
        lifecycleScope.launch {
            try {
                val resposta =
                    RetrofitClient.apiService
                        .excluirAlerta(id)
                if (resposta.isSuccessful) {
                    Toast.makeText(
                        this@AlertsActivity,
                        "Alerta excluído com sucesso.",
                        Toast.LENGTH_SHORT
                    ).show()
                    carregarAlertas()
                } else {
                    Toast.makeText(
                        this@AlertsActivity,
                        "Não foi possível excluir o alerta.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AlertsActivity,
                    "Não foi possível excluir o alerta.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun montarDescricao(
        alerta: Alerta
    ): String {
        val partes =
            mutableListOf<String>()
        alerta.valorLimite?.let { valor ->
            if (
                alerta.tipo.equals(
                    "GASTO",
                    ignoreCase = true
                )
            ) {
                partes.add(
                    String.format(
                        Locale.getDefault(),
                        "Dispara ao atingir R$ %.2f.",
                        valor
                    )
                )
            } else {
                partes.add(
                    String.format(
                        Locale.getDefault(),
                        "Dispara ao atingir %.2f kWh.",
                        valor
                    )
                )
            }
        }
        alerta.percentualLimite?.let { percentual ->
            partes.add(
                String.format(
                    Locale.getDefault(),
                    "Dispara ao atingir %.0f%% da meta.",
                    percentual
                )
            )
        }
        return if (partes.isEmpty()) {
            "Alerta configurado."
        } else {
            partes.joinToString(" ")
        }
    }

    private fun montarCondicao(
        alerta: Alerta
    ): String {
        alerta.valorLimite?.let { valor ->
            return if (
                alerta.tipo.equals(
                    "GASTO",
                    ignoreCase = true
                )
            ) {
                String.format(
                    Locale.getDefault(),
                    "Gasto ≥ R$ %.2f",
                    valor
                )
            } else {
                String.format(
                    Locale.getDefault(),
                    "Consumo ≥ %.2f kWh",
                    valor
                )
            }
        }
        alerta.percentualLimite?.let { percentual ->
            return String.format(
                Locale.getDefault(),
                "Meta ≥ %.0f%%",
                percentual
            )
        }
        return alerta.tipo
            ?.replace("_", " ")
            ?: "Alerta"
    }

    private fun formatarRepeticao(
        repetir: String?
    ): String {
        return when (
            repetir?.uppercase()
        ) {
            "1_HORA" ->
                "A cada 1 hora"
            "6_HORAS" ->
                "A cada 6 horas"
            "DIARIO" ->
                "Uma vez ao dia"
            "NUNCA" ->
                "Não repetir"
            else ->
                repetir
                    ?.replace("_", " ")
                    ?: "Não repetir"
        }
    }

    private fun iconeDoAlerta(
        icone: String?
    ): Int {
        return when (
            icone?.lowercase()
        ) {
            "flag" ->
                R.drawable.ic_flag
            "shield" ->
                R.drawable.ic_maybe
            "wifi_off" ->
                R.drawable.ic_wifi_off
            "crisis_alert" ->
                R.drawable.ic_crisis_alert
            "alarm_on",
            "alarm" ->
                R.drawable.ic_alarm
            "power_settings_new",
            "emergency" ->
                R.drawable.ic_emergency
            else ->
                R.drawable.ic_bolt
        }
    }

    private fun mostrarMensagem(acao: String) {
        Toast.makeText(
            this,
            "$acao: funcionalidade em desenvolvimento.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

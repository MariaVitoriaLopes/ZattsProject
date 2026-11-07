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

class AlertsActivity : AppCompatActivity() {

    private lateinit var listaAlertas: LinearLayout

    private data class Alerta(
        val titulo: String,
        val descricao: String,
        val condicao: String,
        val periodo: String,
        val email: Boolean,
        val push: Boolean,
        val icone: Int
    )

    private val alertas = listOf(
        Alerta(
            "Queda de Energia",
            "Dispara sempre que a energia chega a 0 kWh em até 1 hora.",
            "Energia = 0 kWh",
            "Em até 1 hora",
            true,
            true,
            R.drawable.ic_bolt
        ),
        Alerta(
            "Dispositivo Zatts desativado",
            "Dispara quando o dispositivo IoT não está ligado.",
            "Energia = 0 kWh",
            "Em até 1 hora",
            true,
            true,
            R.drawable.ic_maybe
        ),
        Alerta(
            "Nova meta",
            "Dispara ao atingir 70% (175 kWh) da meta mensal de 250 kWh.",
            "Meta = 250 kWh",
            "Atingiu 70%",
            false,
            true,
            R.drawable.ic_flag
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)

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

        mostrarAlertas()
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
                .setImageResource(alerta.icone)

            item.findViewById<TextView>(R.id.txvTituloAlerta)
                .text = alerta.titulo

            item.findViewById<TextView>(R.id.txvDescricaoAlerta)
                .text = alerta.descricao

            item.findViewById<TextView>(R.id.txvCondicao)
                .text = alerta.condicao

            item.findViewById<TextView>(R.id.txvPeriodo)
                .text = alerta.periodo

            item.findViewById<View>(R.id.txvEmail)
                .visibility = if (alerta.email) View.VISIBLE else View.GONE

            item.findViewById<View>(R.id.txvPush)
                .visibility = if (alerta.push) View.VISIBLE else View.GONE

            item.findViewById<Switch>(R.id.switchAlerta)
                .setOnCheckedChangeListener { _, _ ->
                    mostrarMensagem("Ativar ou desativar alerta")
                }

            item.findViewById<ImageButton>(R.id.btnEditarAlerta)
                .setOnClickListener {
                    mostrarMensagem("Editar alerta")
                }

            item.findViewById<ImageButton>(R.id.btnExcluirAlerta)
                .setOnClickListener {
                    confirmarExclusao()
                }

            listaAlertas.addView(item)
        }
    }

    private fun confirmarExclusao() {
        AlertDialog.Builder(this)
            .setTitle("Excluir alerta")
            .setMessage("Deseja realmente excluir este alerta?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->
                mostrarMensagem("Excluir alerta")
            }
            .show()
    }

    private fun mostrarMensagem(acao: String) {
        Toast.makeText(
            this,
            "$acao: funcionalidade em desenvolvimento.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

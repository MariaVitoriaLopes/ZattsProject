package com.zattsproject.mobile

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class NotificationsActivity : AppCompatActivity() {

    private lateinit var listaNotificacoes: LinearLayout
    private lateinit var txvQuantidade: TextView

    private lateinit var abaGeral: TextView
    private lateinit var abaMetas: TextView
    private lateinit var abaAlertas: TextView

    private data class Notificacao(
        val titulo: String,
        val data: String,
        val icone: Int,
        val categoria: String
    )

    private val notificacoes = listOf(
        Notificacao(
            "Queda de energia foi disparado",
            "1 hora atrás",
            R.drawable.ic_bolt,
            "alertas"
        ),
        Notificacao(
            "Alerta “Queda de energia” criado",
            "2 horas atrás",
            R.drawable.ic_maybe,
            "alertas"
        ),
        Notificacao(
            "Meta criada para até 250 kWh",
            "6 de Outubro",
            R.drawable.ic_flag,
            "metas"
        ),
        Notificacao(
            "Bem-vindo! Sua conta foi criada com sucesso",
            "27 de Dezembro de 2025",
            R.drawable.ic_bem_vindo,
            "geral"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_notifications)

        findViewById<AppNavBar>(R.id.navBar)
            .setSelected(NavTab.NOTIFICATIONS)

        listaNotificacoes = findViewById(R.id.listaNotificacoes)
        txvQuantidade = findViewById(R.id.txvQuantidadeNotificacoes)

        abaGeral = findViewById(R.id.abaGeral)
        abaMetas = findViewById(R.id.abaMetas)
        abaAlertas = findViewById(R.id.abaAlertas)

        configurarAbas()
        selecionarAba(abaGeral)
        mostrarNotificacoes("geral")
    }

    private fun configurarAbas() {

        abaGeral.setOnClickListener {
            selecionarAba(abaGeral)
            mostrarNotificacoes("geral")
        }

        abaMetas.setOnClickListener {
            selecionarAba(abaMetas)
            mostrarNotificacoes("metas")
        }

        abaAlertas.setOnClickListener {
            selecionarAba(abaAlertas)
            mostrarNotificacoes("alertas")
        }
    }

    private fun selecionarAba(abaSelecionada: TextView) {

        listOf(abaGeral, abaMetas, abaAlertas).forEach { aba ->

            aba.setBackgroundResource(0)

            aba.setTextColor(
                com.google.android.material.color.MaterialColors.getColor(
                    aba,
                    com.google.android.material.R.attr.colorOnSurface
                )
            )
        }

        abaSelecionada.setBackgroundResource(
            R.drawable.bg_notificacoes_aba_ativa
        )

        abaSelecionada.setTextColor(
            com.google.android.material.color.MaterialColors.getColor(
                abaSelecionada,
                com.google.android.material.R.attr.colorOnPrimary
            )
        )
    }

    private fun mostrarNotificacoes(categoria: String) {

        listaNotificacoes.removeAllViews()

        val filtradas = if (categoria == "geral") {
            notificacoes
        } else {
            notificacoes.filter {
                it.categoria == categoria
            }
        }

        txvQuantidade.text = if (filtradas.size == 1) {
            "1 notificação"
        } else {
            "${filtradas.size} notificações"
        }

        filtradas.forEach { notificacao ->

            val item = layoutInflater.inflate(
                R.layout.item_notifications,
                listaNotificacoes,
                false
            )

            item.findViewById<ImageView>(R.id.iconeNotificacao)
                .setImageResource(notificacao.icone)

            item.findViewById<TextView>(R.id.txvTituloNotificacao)
                .text = notificacao.titulo

            item.findViewById<TextView>(R.id.txvDataNotificacao)
                .text = notificacao.data

            item.findViewById<ImageButton>(R.id.btnExcluirNotificacao)
                .setOnClickListener {
                    confirmarExclusao()
                }

            listaNotificacoes.addView(item)
        }
    }

    private fun confirmarExclusao() {

        AlertDialog.Builder(this)
            .setTitle("Excluir notificação")
            .setMessage("Deseja realmente excluir esta notificação?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->

                Toast.makeText(
                    this,
                    "Excluir notificação: funcionalidade em desenvolvimento.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }
}

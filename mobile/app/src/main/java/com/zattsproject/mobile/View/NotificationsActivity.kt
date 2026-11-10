package com.zattsproject.mobile

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zattsproject.mobile.Model.Notificacao
import com.zattsproject.mobile.Repository.NotificacaoRepository
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
class NotificationsActivity : AppCompatActivity() {

    private lateinit var listaNotificacoes: LinearLayout
    private lateinit var txvQuantidade: TextView

    private lateinit var abaGeral: TextView
    private lateinit var abaMetas: TextView
    private lateinit var abaAlertas: TextView

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
        carregarNotificacoes()
    }

    private fun configurarAbas() {

        abaGeral.setOnClickListener {
            categoriaAtual = "geral"
            selecionarAba(abaGeral)
            mostrarNotificacoes()
        }

        abaMetas.setOnClickListener {
            categoriaAtual = "metas"
            selecionarAba(abaMetas)
            mostrarNotificacoes()
        }

        abaAlertas.setOnClickListener {
            categoriaAtual = "alertas"
            selecionarAba(abaAlertas)
            mostrarNotificacoes()
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

    private val notificacaoRepository =
        NotificacaoRepository()

    private var notificacoes: List<Notificacao> =
        emptyList()

    private var categoriaAtual = "geral"

    private fun carregarNotificacoes() {

        lifecycleScope.launch {

            try {

                val resposta =
                    notificacaoRepository.listar()

                if (resposta.isSuccessful) {

                    notificacoes =
                        resposta.body().orEmpty()

                    mostrarNotificacoes()

                } else {

                    Toast.makeText(
                        this@NotificationsActivity,
                        if (resposta.code() == 401) {
                            "Sua sessão expirou. Entre novamente."
                        } else {
                            "Não foi possível carregar as notificações."
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@NotificationsActivity,
                    "Não foi possível conectar ao servidor.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun mostrarNotificacoes() {

        listaNotificacoes.removeAllViews()

        val filtradas =
            when (categoriaAtual) {

                "metas" ->
                    notificacoes.filter {
                        it.categoria.equals(
                            "META",
                            ignoreCase = true
                        )
                    }

                "alertas" ->
                    notificacoes.filter {
                        it.categoria.equals(
                            "ALERTA",
                            ignoreCase = true
                        )
                    }

                else ->
                    notificacoes
            }

        txvQuantidade.text =
            if (filtradas.size == 1) {
                "1 notificação"
            } else {
                "${filtradas.size} notificações"
            }

        filtradas.forEach { notificacao ->

            val item =
                layoutInflater.inflate(
                    R.layout.item_notifications,
                    listaNotificacoes,
                    false
                )

            item.findViewById<ImageView>(
                R.id.iconeNotificacao
            ).setImageResource(
                iconeDaCategoria(
                    notificacao.categoria
                )
            )

            item.findViewById<TextView>(
                R.id.txvTituloNotificacao
            ).text =
                notificacao.titulo

            item.findViewById<TextView>(
                R.id.txvDataNotificacao
            ).text =
                formatarData(
                    notificacao.criadaEm
                )

            item.findViewById<ImageButton>(
                R.id.btnExcluirNotificacao
            ).setOnClickListener {
                confirmarExclusao()
            }

            listaNotificacoes.addView(item)
        }
    }

    private fun iconeDaCategoria(
        categoria: String
    ): Int {

        return when (
            categoria.uppercase()
        ) {

            "META" ->
                R.drawable.ic_flag

            "ALERTA" ->
                R.drawable.ic_bolt

            "CONSUMO" ->
                R.drawable.ic_bolt

            "CONTA" ->
                R.drawable.ic_maybe

            else ->
                R.drawable.ic_bem_vindo
        }
    }

    private fun formatarData(
        criadaEm: String
    ): String {
        return try {
            val data =
                LocalDateTime.parse(criadaEm)
            val agora =
                LocalDateTime.now()
            val minutos =
                Duration.between(
                    data,
                    agora
                ).toMinutes()
            when {
                minutos < 1 ->
                    "Agora"
                minutos < 60 ->
                    "$minutos min atrás"
                minutos < 1440 -> {
                    val horas =
                        minutos / 60

                    if (horas == 1L) {
                        "1 hora atrás"
                    } else {
                        "$horas horas atrás"
                    }
                }
                else ->
                    data.format(
                        DateTimeFormatter.ofPattern(
                            "dd/MM/yyyy 'às' HH:mm"
                        )
                    )
            }
        } catch (e: Exception) {
            criadaEm
                .replace("T", " ")
                .take(16)
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

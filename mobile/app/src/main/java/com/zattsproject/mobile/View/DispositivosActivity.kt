package com.zattsproject.mobile

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class DispositivosActivity : AppCompatActivity() {

    private lateinit var listaDispositivos: LinearLayout
    private lateinit var txvQuantidade: TextView

    private data class Dispositivo(
        val nome: String,
        val data: String,
        val icone: Int
    )

    private val dispositivos = listOf(
        Dispositivo(
            "Firefox (Windows 11)",
            "Conectado às 17:33",
            R.drawable.ic_public
        ),
        Dispositivo(
            "Samsung M13",
            "17 de setembro de 2026",
            R.drawable.ic_android
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dispositivos)

        findViewById<AppNavBar>(R.id.navBar)
            .setSelected(NavTab.SETTINGS)

        listaDispositivos = findViewById(R.id.listaDispositivos)
        txvQuantidade = findViewById(R.id.txvQuantidadeDispositivos)

        findViewById<TextView>(R.id.btnVoltarDispositivos)
            .setOnClickListener {
                finish()
            }

        mostrarDispositivos()
    }

    private fun mostrarDispositivos() {

        listaDispositivos.removeAllViews()

        txvQuantidade.text = "${dispositivos.size} dispositivos"

        dispositivos.forEach { dispositivo ->

            val item = layoutInflater.inflate(
                R.layout.item_dispositivo,
                listaDispositivos,
                false
            )

            item.findViewById<ImageView>(R.id.iconeDispositivo)
                .setImageResource(dispositivo.icone)

            item.findViewById<TextView>(R.id.txvNomeDispositivo)
                .text = dispositivo.nome

            item.findViewById<TextView>(R.id.txvDataDispositivo)
                .text = dispositivo.data

            item.findViewById<TextView>(R.id.btnDesconectarDispositivo)
                .setOnClickListener {
                    confirmarDesconexao(dispositivo)
                }

            listaDispositivos.addView(item)
        }
    }

    private fun confirmarDesconexao(dispositivo: Dispositivo) {

        AlertDialog.Builder(this)
            .setTitle("Desconectar dispositivo")
            .setMessage(
                "Deseja realmente desconectar ${dispositivo.nome} da sua conta?"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Desconectar") { _, _ ->

                Toast.makeText(
                    this,
                    "Desconectar dispositivo: funcionalidade em desenvolvimento.",
                    Toast.LENGTH_SHORT
                ).show()

                // TODO: Implementar encerramento da sessão pela API.
            }
            .show()
    }
}
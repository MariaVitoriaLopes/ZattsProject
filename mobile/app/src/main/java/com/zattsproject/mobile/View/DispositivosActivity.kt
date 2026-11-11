package com.zattsproject.mobile

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.zattsproject.mobile.Model.SessaoDispositivo
import com.zattsproject.mobile.Network.RetrofitClient
import kotlinx.coroutines.launch
import android.view.View

class DispositivosActivity : AppCompatActivity() {

    private lateinit var listaDispositivos: LinearLayout
    private lateinit var txvQuantidade: TextView

    private var dispositivos:
            List<SessaoDispositivo> = emptyList()

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
    }

    override fun onResume() {
        super.onResume()
        carregarDispositivos()
    }

    private fun carregarDispositivos() {
        lifecycleScope.launch {
            try {
                val resposta =
                    RetrofitClient.apiService
                        .buscarDispositivos()
                if (resposta.isSuccessful) {
                    dispositivos =
                        resposta.body().orEmpty()

                    mostrarDispositivos()
                } else {
                    Toast.makeText(
                        this@DispositivosActivity,
                        if (resposta.code() == 401) {
                            "Sua sessão expirou. Entre novamente."
                        } else {
                            "Não foi possível carregar os dispositivos."
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@DispositivosActivity,
                    "Não foi possível conectar ao servidor.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun mostrarDispositivos() {
        listaDispositivos.removeAllViews()
        txvQuantidade.text =
            if (dispositivos.size == 1) {
                "1 dispositivo"
            } else {
                "${dispositivos.size} dispositivos"
            }
        dispositivos.forEach { dispositivo ->
            val item =
                layoutInflater.inflate(
                    R.layout.item_dispositivo,
                    listaDispositivos,
                    false
                )
            item.findViewById<ImageView>(
                R.id.iconeDispositivo
            ).setImageResource(
                if (
                    dispositivo.tipo.equals(
                        "ANDROID",
                        ignoreCase = true
                    )
                ) {
                    R.drawable.ic_android
                } else {
                    R.drawable.ic_public
                }
            )
            item.findViewById<TextView>(
                R.id.txvNomeDispositivo
            ).text =
                if (dispositivo.atual) {
                    "${dispositivo.nome} (este dispositivo)"
                } else {
                    dispositivo.nome
                }
            item.findViewById<TextView>(
                R.id.txvDataDispositivo
            ).text =
                dispositivo.conectadoEm
            item.findViewById<TextView>(
                R.id.txvStatusDispositivo
            ).visibility = View.GONE
            item.findViewById<TextView>(
                R.id.btnDesconectarDispositivo
            ).setOnClickListener {
                confirmarDesconexao(
                    dispositivo
                )
            }
            listaDispositivos.addView(item)
        }
    }

    private fun confirmarDesconexao(
        dispositivo: SessaoDispositivo
    ) {
        AlertDialog.Builder(this)
            .setTitle(
                "Desconectar dispositivo"
            )
            .setMessage(
                "Deseja realmente desconectar " +
                        "${dispositivo.nome} da sua conta?"
            )
            .setNegativeButton(
                "Cancelar",
                null
            )
            .setPositiveButton(
                "Desconectar"
            ) { _, _ ->
                desconectarDispositivo(
                    dispositivo
                )
            }
            .show()
    }

    private fun desconectarDispositivo(
        dispositivo: SessaoDispositivo
    ) {
        lifecycleScope.launch {
            try {
                val resposta =
                    RetrofitClient.apiService
                        .desconectarDispositivo(
                            dispositivo.id
                        )
                if (resposta.isSuccessful) {
                    val atual =
                        resposta.body()
                            ?.atual == true
                    if (atual) {
                        RetrofitClient
                            .limparCookies()
                        Toast.makeText(
                            this@DispositivosActivity,
                            "Dispositivo desconectado.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent =
                            Intent(
                                this@DispositivosActivity,
                                MainActivity::class.java
                            ).apply {

                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@DispositivosActivity,
                            "Dispositivo desconectado com sucesso.",
                            Toast.LENGTH_SHORT
                        ).show()

                        carregarDispositivos()
                    }
                } else {
                    Toast.makeText(
                        this@DispositivosActivity,
                        when (resposta.code()) {

                            401 ->
                                "Sua sessão expirou."

                            403 ->
                                "Você não pode desconectar esse dispositivo."

                            404 ->
                                "Dispositivo não encontrado."

                            else ->
                                "Não foi possível desconectar o dispositivo."
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@DispositivosActivity,
                    "Não foi possível conectar ao servidor.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
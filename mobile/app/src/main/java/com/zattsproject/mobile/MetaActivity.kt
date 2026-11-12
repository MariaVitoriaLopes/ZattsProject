
package com.zattsproject.mobile

import android.os.Bundle
import android.util.Log
import android.util.Log.e
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zattsproject.mobile.Network.RetrofitClient
import kotlinx.coroutines.launch
import java.util.Locale
import java.net.SocketTimeoutException

class MetaActivity : AppCompatActivity() {

    private lateinit var spinnerTipo: Spinner
    private lateinit var edtValor: EditText
    private lateinit var txvUnidade: TextView
    private lateinit var btnSalvar: Button
    private lateinit var btnCancelar: Button

    private var tipoAtual = "CONSUMO"
    private var possuiMeta = false

    private val tipos = listOf(
        "Consumo de energia (kWh)",
        "Gastos de energia (R$)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meta)

        inicializarComponentes()
        configurarSpinner()
        carregarDadosRecebidos()
        configurarBotoes()
    }

    private fun inicializarComponentes() {

        spinnerTipo = findViewById(R.id.spinnerTipoMeta)
        edtValor = findViewById(R.id.edtValorMeta)
        txvUnidade = findViewById(R.id.txvUnidadeMeta)
        btnSalvar = findViewById(R.id.btnSalvarMeta)
        btnCancelar = findViewById(R.id.btnCancelarMeta)
    }

    private fun configurarSpinner() {

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            tipos
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerTipo.adapter = adapter

        spinnerTipo.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    txvUnidade.text =
                        if (position == 0) "kWh" else "R$"
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {}
            }
    }

    private fun carregarDadosRecebidos() {

        tipoAtual = intent.getStringExtra("tipoMeta")
            ?: "CONSUMO"

        val valor = intent.getDoubleExtra(
            "valorMeta",
            0.0
        )

        possuiMeta = intent.getBooleanExtra(
            "possuiMeta",
            false
        )

        spinnerTipo.setSelection(
            if (tipoAtual == "VALOR") 1 else 0
        )

        // Mantém o tipo da meta existente.
        spinnerTipo.isEnabled = !possuiMeta

        if (valor > 0) {
            edtValor.setText(
                String.format(Locale.US, "%.2f", valor)
            )
        }

        btnSalvar.text =
            if (possuiMeta) "Salvar alterações"
            else "Cadastrar meta"
    }

    private fun configurarBotoes() {

        findViewById<TextView>(
            R.id.btnVoltarMeta
        ).setOnClickListener {
            finish()
        }

        btnCancelar.setOnClickListener {
            finish()
        }

        btnSalvar.setOnClickListener {
            salvarMeta()
        }
    }

    private fun salvarMeta() {

        val valorTexto = edtValor.text.toString()
            .trim()
            .replace(",", ".")

        val valor = valorTexto.toDoubleOrNull()

        if (valor == null || !valor.isFinite() || valor <= 0) {
            edtValor.error = "Informe um valor válido."
            edtValor.requestFocus()
            return
        }

        val tipo = if (spinnerTipo.selectedItemPosition == 0) {
            "CONSUMO"
        } else {
            "VALOR"
        }

        btnSalvar.isEnabled = false
        btnCancelar.isEnabled = false

        btnSalvar.text = "Salvando..."

        lifecycleScope.launch {

            try {

                val valorFormatado = String.format(
                    Locale.US,
                    "%.2f",
                    valor
                )

                Log.d(
                    "META_MOBILE",
                    "Enviando meta: tipo=$tipo, valor=$valorFormatado, edicao=$possuiMeta"
                )

                val resposta = if (possuiMeta) {

                    RetrofitClient.apiService.editarMeta(
                        tipo,
                        valorFormatado
                    )

                } else {

                    RetrofitClient.apiService.criarMeta(
                        tipo,
                        valorFormatado
                    )
                }

                val caminhoFinal = resposta.raw()
                    .request.url.encodedPath

                Log.d(
                    "META_MOBILE",
                    "HTTP: ${resposta.code()} | URL final: $caminhoFinal"
                )

                if (caminhoFinal.endsWith("/login")) {

                    Toast.makeText(
                        this@MetaActivity,
                        "Sessão expirada. Faça login novamente.",
                        Toast.LENGTH_LONG
                    ).show()

                } else if (resposta.isSuccessful) {

                    Toast.makeText(
                        this@MetaActivity,
                        if (possuiMeta)
                            "Meta atualizada com sucesso!"
                        else
                            "Meta cadastrada com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()

                    setResult(RESULT_OK)
                    finish()

                } else {

                    Toast.makeText(
                        this@MetaActivity,
                        "Erro ao salvar meta. Código: ${resposta.code()}",
                        Toast.LENGTH_LONG
                    ).show()

                }

            } catch (e: SocketTimeoutException) {

            Log.w(
                "META_MOBILE",
                "Tempo de resposta excedido. A requisição pode ter sido processada.",
                e
            )

            Toast.makeText(
                this@MetaActivity,
                "A meta foi enviada, mas o servidor demorou para responder.",
                Toast.LENGTH_LONG
            ).show()

            setResult(RESULT_OK)
            finish()

        } catch (e: Exception) {

            Log.e(
                "META_MOBILE",
                "Erro ao salvar meta",
                e
            )

            Toast.makeText(
                this@MetaActivity,
                "Erro ao salvar meta: ${e.message}",
                Toast.LENGTH_LONG
            ).show()

            } finally {

                if (!isFinishing) {
                    btnSalvar.isEnabled = true
                    btnCancelar.isEnabled = true

                    btnSalvar.text =
                        if (possuiMeta) "Salvar alterações"
                        else "Cadastrar meta"
                }
            }
        }
    }
}

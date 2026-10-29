package com.zattsproject.mobile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.zattsproject.mobile.R
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class ContaActivity : AppCompatActivity() {

    private lateinit var edtCep: EditText
    private lateinit var edtLogradouro: EditText
    private lateinit var edtNumero: EditText
    private lateinit var edtComplemento: EditText
    private lateinit var edtBairro: EditText
    private lateinit var edtCidade: EditText
    private lateinit var spinnerUf: Spinner

    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    private var consultaPendente: Runnable? = null
    private var cepConsultado = ""
    private var atualizandoCep = false

    private val estados = listOf(
        "SP", "RJ", "MG", "ES", "PR", "SC", "RS",
        "BA", "PE", "CE", "DF", "GO", "MT", "MS",
        "PA", "AM", "AC", "AL", "AP", "MA", "PB",
        "PI", "RN", "RO", "RR", "SE", "TO"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_conta)

        iniciarCampos()
        configurarSeletores()
        configurarCep()
        configurarBotoes()
    }

    private fun iniciarCampos() {
        edtCep = findViewById(R.id.edtCep)
        edtLogradouro = findViewById(R.id.edtLogradouro)
        edtNumero = findViewById(R.id.edtNumero)
        edtComplemento = findViewById(R.id.edtComplemento)
        edtBairro = findViewById(R.id.edtBairro)
        edtCidade = findViewById(R.id.edtCidade)
        spinnerUf = findViewById(R.id.spinnerUf)
    }

    private fun configurarSeletores() {
        val fusos = listOf(
            "UTC-3 (Horário de Brasília)",
            "UTC-4 (Amazonas)",
            "UTC-5 (Acre)",
            "UTC-2 (Fernando de Noronha)"
        )

        configurarSpinner(R.id.spinnerFuso, fusos)
        configurarSpinner(R.id.spinnerUf, estados)
    }

    private fun configurarSpinner(
        spinnerId: Int,
        opcoes: List<String>
    ) {
        val spinner = findViewById<Spinner>(spinnerId)

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

    // Observa a digitação e aplica a máscara 00000-000
    private fun configurarCep() {

        edtCep.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {}

            override fun afterTextChanged(s: Editable?) {

                if (atualizandoCep) return

                val numeros = s.toString()
                    .filter { it.isDigit() }
                    .take(8)

                val formatado = if (numeros.length > 5) {
                    numeros.take(5) + "-" + numeros.drop(5)
                } else {
                    numeros
                }

                if (s.toString() != formatado) {
                    atualizandoCep = true
                    edtCep.setText(formatado)
                    edtCep.setSelection(formatado.length)
                    atualizandoCep = false
                }

                consultaPendente?.let {
                    handler.removeCallbacks(it)
                }

                if (numeros.length != 8) {
                    cepConsultado = ""
                    return
                }

                if (numeros == cepConsultado) return

                val tarefa = Runnable {
                    consultarCep(numeros)
                }

                consultaPendente = tarefa

                // Pequeno intervalo para evitar consultas
                // desnecessárias durante a digitação.
                handler.postDelayed(tarefa, 400)
            }
        })
    }

    // Consulta a API ViaCEP fora da thread principal
    private fun consultarCep(cep: String) {

        cepConsultado = cep

        executor.execute {

            var conexao: HttpURLConnection? = null

            try {
                val url = URL(
                    "https://viacep.com.br/ws/$cep/json/"
                )

                conexao = url.openConnection() as HttpURLConnection

                conexao.requestMethod = "GET"
                conexao.connectTimeout = 5000
                conexao.readTimeout = 5000

                if (conexao.responseCode != 200) {
                    throw Exception("Falha na consulta do CEP")
                }

                val resposta = conexao.inputStream
                    .bufferedReader()
                    .use { it.readText() }

                val json = JSONObject(resposta)

                if (json.optBoolean("erro", false)) {
                    runOnUiThread {
                        if (cepAtual() == cep) {
                            edtCep.error = "CEP não encontrado"
                            Toast.makeText(
                                this,
                                "CEP não encontrado.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    return@execute
                }

                val logradouro = json.optString("logradouro")
                val bairro = json.optString("bairro")
                val cidade = json.optString("localidade")
                val uf = json.optString("uf")
                val complemento = json.optString("complemento")

                runOnUiThread {

                    // Evita preencher um endereço antigo
                    // se o usuário já mudou o CEP.
                    if (cepAtual() != cep || isFinishing || isDestroyed) {
                        return@runOnUiThread
                    }

                    edtLogradouro.setText(logradouro)
                    edtBairro.setText(bairro)
                    edtCidade.setText(cidade)

                    // Preenche complemento somente se estiver vazio
                    if (
                        complemento.isNotBlank() &&
                        edtComplemento.text.isNullOrBlank()
                    ) {
                        edtComplemento.setText(complemento)
                    }

                    val indiceUf = estados.indexOf(uf)

                    if (indiceUf >= 0) {
                        spinnerUf.setSelection(indiceUf)
                    }

                    // Número não é fornecido pelo ViaCEP
                    // e deve ser preenchido pelo usuário.

                    Toast.makeText(
                        this,
                        "Endereço encontrado!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                runOnUiThread {
                    if (cepAtual() == cep && !isFinishing) {
                        cepConsultado = ""

                        Toast.makeText(
                            this,
                            "Não foi possível consultar o CEP. " +
                                    "Preencha o endereço manualmente.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } finally {
                conexao?.disconnect()
            }
        }
    }

    private fun cepAtual(): String {
        return edtCep.text.toString()
            .filter { it.isDigit() }
    }

    private fun configurarBotoes() {

        findViewById<TextView>(R.id.btnVoltar)
            .setOnClickListener {
                finish()
            }

        findViewById<Button>(R.id.btnSalvarConta)
            .setOnClickListener {

                val senha = findViewById<EditText>(
                    R.id.edtSenha
                ).text.toString()

                val confirmacao = findViewById<EditText>(
                    R.id.edtConfirmarSenha
                ).text.toString()

                if (senha != confirmacao) {
                    Toast.makeText(
                        this,
                        "As senhas não coincidem.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                // TODO: COLOQUE O CAMINHO DA API AQUI.
                // Enviar os dados da conta e do endereço
                // para o Spring Boot utilizando Retrofit.
                //
                // Exemplo de dados disponíveis:
                // edtCep.text.toString()
                // edtLogradouro.text.toString()
                // edtNumero.text.toString()
                // edtComplemento.text.toString()
                // edtBairro.text.toString()
                // edtCidade.text.toString()
                // estados[spinnerUf.selectedItemPosition]

                Toast.makeText(
                    this,
                    "Coloque aqui o caminho da API para salvar.",
                    Toast.LENGTH_LONG
                ).show()
            }

        findViewById<Button>(R.id.btnSairConta)
            .setOnClickListener {

                // TODO: COLOQUE A LÓGICA DE LOGOUT AQUI.
                // Encerrar a sessão e retornar ao login.

                Toast.makeText(
                    this,
                    "Coloque aqui o caminho para sair da conta.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        findViewById<Button>(R.id.btnExcluirConta)
            .setOnClickListener {

                AlertDialog.Builder(this)
                    .setTitle("Excluir minha conta")
                    .setMessage(
                        "Esta ação pode excluir permanentemente " +
                                "todos os seus dados. Deseja continuar?"
                    )
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Continuar") { _, _ ->

                        // TODO: COLOQUE O CAMINHO DA API AQUI.
                        // Solicitar a exclusão da conta
                        // ao back-end Spring Boot.

                        Toast.makeText(
                            this,
                            "Coloque aqui o caminho da API para excluir.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .show()
            }
    }

    override fun onDestroy() {
        consultaPendente?.let {
            handler.removeCallbacks(it)
        }

        executor.shutdownNow()

        super.onDestroy()
    }
}
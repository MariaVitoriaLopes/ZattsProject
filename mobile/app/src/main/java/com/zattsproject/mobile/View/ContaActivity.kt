package com.zattsproject.mobile

import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import com.zattsproject.mobile.Network.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import android.util.Log
import com.zattsproject.mobile.Model.Conta

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

        // Seleciona Configurações na navbar
        findViewById<AppNavBar>(R.id.navBar)
            .setSelected(NavTab.SETTINGS)

        iniciarCampos()
        configurarSeletores()
        configurarCep()
        configurarBotoes()
        carregarConta()
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
                salvarConta()
            }

        findViewById<Button>(R.id.btnSairConta)
            .setOnClickListener {

                lifecycleScope.launch {

                    try {

                        val resposta = RetrofitClient.apiService.logout()

                        if (!resposta.isSuccessful) {
                            Toast.makeText(
                                this@ContaActivity,
                                "Não foi possível encerrar a sessão no servidor.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: CancellationException) {
                        throw e

                    } catch (e: Exception) {

                        Toast.makeText(
                            this@ContaActivity,
                            "Erro ao encerrar sessão.",
                            Toast.LENGTH_SHORT
                        ).show()

                    } finally {

                        RetrofitClient.limparCookies()

                        val intent = Intent(
                            this@ContaActivity,
                            LoginActivity::class.java
                        )

                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK

                        startActivity(intent)
                        finish()
                    }
                }
            }

        findViewById<Button>(R.id.btnExcluirConta)
            .setOnClickListener {

                AlertDialog.Builder(this)
                    .setTitle("Excluir conta")
                    .setMessage(
                        "Tem certeza de que deseja excluir sua conta? " +
                                "Essa ação não poderá ser desfeita."
                    )
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Excluir") { _, _ ->

                        lifecycleScope.launch {

                            try {

                                val resposta =
                                    RetrofitClient.apiService.excluirConta()

                                Log.d(
                                    "ZATTS_EXCLUIR",
                                    "HTTP: ${resposta.code()}"
                                )

                                if (resposta.isSuccessful) {

                                    RetrofitClient.limparCookies()

                                    Toast.makeText(
                                        this@ContaActivity,
                                        "Conta excluída com sucesso!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    val intent = Intent(
                                        this@ContaActivity,
                                        LoginActivity::class.java
                                    )

                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                                    startActivity(intent)
                                    finish()

                                } else {

                                    val erro = resposta.errorBody()?.string()

                                    Log.e(
                                        "ZATTS_EXCLUIR",
                                        "Erro HTTP ${resposta.code()}: $erro"
                                    )

                                    Toast.makeText(
                                        this@ContaActivity,
                                        "Erro ${resposta.code()}: $erro",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            } catch (e: CancellationException) {

                                throw e

                            } catch (e: Exception) {

                                Log.e(
                                    "ZATTS_EXCLUIR",
                                    "Falha ao excluir conta.",
                                    e
                                )

                                Toast.makeText(
                                    this@ContaActivity,
                                    "Erro ao excluir conta: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    .show()
            }
    }


    private fun carregarConta() {

        lifecycleScope.launch {

            try {

                val resposta = RetrofitClient.apiService.buscarConta()

                if (!resposta.isSuccessful) {

                    Toast.makeText(
                        this@ContaActivity,
                        "Não foi possível carregar os dados da conta.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@launch
                }

                val conta = resposta.body() ?: return@launch

                findViewById<EditText>(R.id.edtNome)
                    .setText(conta.nome.orEmpty())

                findViewById<EditText>(R.id.edtEmail)
                    .setText(conta.email.orEmpty())

                findViewById<EditText>(R.id.edtTelefone)
                    .setText(conta.telefone.orEmpty())

                // Evita que o CEP salvo dispare uma nova consulta.
                cepConsultado = conta.cep.orEmpty()
                    .filter { it.isDigit() }

                edtCep.setText(conta.cep.orEmpty())
                edtLogradouro.setText(conta.logradouro.orEmpty())
                edtNumero.setText(conta.numero.orEmpty())
                edtComplemento.setText(conta.complemento.orEmpty())
                edtBairro.setText(conta.bairro.orEmpty())
                edtCidade.setText(conta.cidade.orEmpty())

                val indiceUf = estados.indexOf(conta.uf)

                if (indiceUf >= 0) {
                    spinnerUf.setSelection(indiceUf)
                }

                val spinnerFuso =
                    findViewById<Spinner>(R.id.spinnerFuso)

                val adapter = spinnerFuso.adapter

                for (i in 0 until adapter.count) {

                    if (adapter.getItem(i).toString() ==
                        conta.fusoHorario) {

                        spinnerFuso.setSelection(i)
                        break
                    }
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                Log.e("ZATTS_CONTA", "Erro ao carregar conta", e)

                Toast.makeText(
                    this@ContaActivity,
                    "Erro ao carregar os dados.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun salvarConta() {

        val novaSenha = findViewById<EditText>(
            R.id.edtSenha
        ).text.toString()

        val confirmarSenha = findViewById<EditText>(
            R.id.edtConfirmarSenha
        ).text.toString()

        val alterarSenha =
            novaSenha.isNotEmpty() ||
                    confirmarSenha.isNotEmpty()

        if (alterarSenha) {

            if (novaSenha != confirmarSenha) {
                Toast.makeText(
                    this,
                    "As novas senhas não coincidem.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val senhaValida = Regex(
                """^(?=\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$"""
            ).matches(novaSenha)

            if (!senhaValida) {
                Toast.makeText(
                    this,
                    "A nova senha precisa ter 8 caracteres, " +
                            "letras maiúsculas e minúsculas, " +
                            "número e caractere especial.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        val conta = Conta(
            nome = findViewById<EditText>(R.id.edtNome)
                .text.toString().trim(),

            email = findViewById<EditText>(R.id.edtEmail)
                .text.toString().trim(),

            telefone = findViewById<EditText>(R.id.edtTelefone)
                .text.toString().trim(),

            fusoHorario = findViewById<Spinner>(R.id.spinnerFuso)
                .selectedItem.toString(),

            cep = edtCep.text.toString().trim(),
            logradouro = edtLogradouro.text.toString().trim(),
            numero = edtNumero.text.toString().trim(),
            complemento = edtComplemento.text.toString().trim(),
            bairro = edtBairro.text.toString().trim(),
            cidade = edtCidade.text.toString().trim(),
            uf = spinnerUf.selectedItem.toString(),

            novaSenha = novaSenha,
            confirmarSenha = confirmarSenha
        )

        if (conta.email.isNullOrBlank()) {
            Toast.makeText(
                this,
                "Preencha o e-mail.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val botaoSalvar = findViewById<Button>(R.id.btnSalvarConta)
        botaoSalvar.isEnabled = false

        lifecycleScope.launch {

            try {

                val resposta =
                    RetrofitClient.apiService.atualizarConta(conta)

                if (resposta.isSuccessful) {

                    findViewById<EditText>(R.id.edtSenha)
                        .text.clear()

                    findViewById<EditText>(R.id.edtConfirmarSenha)
                        .text.clear()

                    Toast.makeText(
                        this@ContaActivity,
                        "Alterações salvas com sucesso!",
                        Toast.LENGTH_LONG
                    ).show()

                } else {

                    val erro = resposta.errorBody()?.string()

                    Log.e(
                        "ZATTS_CONTA",
                        "Erro HTTP ${resposta.code()}: $erro"
                    )

                    val mensagem = try {
                        JSONObject(erro.orEmpty())
                            .optString("erro", "Não foi possível salvar.")
                    } catch (e: Exception) {
                        "Não foi possível salvar as alterações."
                    }

                    Toast.makeText(
                        this@ContaActivity,
                        mensagem,
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                Log.e("ZATTS_CONTA", "Erro ao salvar conta", e)

                Toast.makeText(
                    this@ContaActivity,
                    "Erro de conexão ao salvar alterações.",
                    Toast.LENGTH_LONG
                ).show()

            } finally {
                botaoSalvar.isEnabled = true
            }
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
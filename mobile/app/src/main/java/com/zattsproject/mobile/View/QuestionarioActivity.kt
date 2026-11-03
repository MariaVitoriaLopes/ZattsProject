
package com.zattsproject.mobile

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

import com.zattsproject.mobile.Model.Questionario
import com.zattsproject.mobile.Network.RetrofitClient
import com.zattsproject.mobile.Repository.QuestionarioRepository

import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

import org.json.JSONObject

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class QuestionarioActivity : AppCompatActivity() {

    private val repository = QuestionarioRepository()

    // COMPONENTES
    private lateinit var progresso: LinearProgressIndicator
    private lateinit var grupoOpcoes: android.widget.RadioGroup

    private lateinit var txvNumero: android.widget.TextView
    private lateinit var txvPergunta: android.widget.TextView

    private lateinit var layoutData: TextInputLayout
    private lateinit var layoutValor: TextInputLayout
    private lateinit var layoutConsumo: TextInputLayout
    private lateinit var layoutAjudaConsumo: View

    private lateinit var edtData: TextInputEditText
    private lateinit var edtValor: TextInputEditText
    private lateinit var edtConsumo: TextInputEditText

    private lateinit var btnAnterior: MaterialButton
    private lateinit var btnProxima: MaterialButton
    private lateinit var btnVoltarLogin: FloatingActionButton

    // CONTROLE DAS ETAPAS
    private var etapaAtual = 1
    private val totalEtapas = 7

    // RESPOSTAS
    private var qtdPessoas: Int? = null
    private var concessionaria: String? = null
    private var vencimentoIso: String? = null

    private var valorMes1 = ""
    private var valorMes2 = ""
    private var valorMes3 = ""

    private var consumoMes1 = ""
    private var consumoMes2 = ""
    private var consumoMes3 = ""

    private var definirMeta: String? = null
    private var tipoMeta: String? = null
    private var valorMeta = ""

    // CONTROLE INTERNO DA ÚLTIMA ETAPA
    private var selecionandoTipoMeta = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_questionario)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        inicializarComponentes()
        configurarEventos()
        renderizarEtapa()
    }

    // =====================================================
    // INICIALIZAÇÃO
    // =====================================================

    private fun inicializarComponentes() {

        progresso = findViewById(R.id.progressoQuestionario)

        grupoOpcoes = findViewById(R.id.grupoOpcoes)

        txvNumero = findViewById(R.id.txv_numeroQuestao)
        txvPergunta = findViewById(R.id.txv_pergunta)

        layoutData = findViewById(R.id.layoutData)
        layoutValor = findViewById(R.id.layoutValor)
        layoutConsumo = findViewById(R.id.layoutConsumo)

        layoutAjudaConsumo = findViewById(
            R.id.layoutAjudaConsumo
        )

        edtData = findViewById(R.id.edtData)
        edtValor = findViewById(R.id.edtValor)
        edtConsumo = findViewById(R.id.edtConsumo)

        btnAnterior = findViewById(R.id.btn_anterior)
        btnProxima = findViewById(R.id.btn_proxima)

        btnVoltarLogin = findViewById(R.id.btn_voltarLogin)
    }

    // =====================================================
    // EVENTOS
    // =====================================================

    private fun configurarEventos() {

        btnProxima.setOnClickListener {

            if (!capturarEValidarEtapa()) {
                return@setOnClickListener
            }

            if (etapaAtual == 7) {

                if (definirMeta == "sim" && !selecionandoTipoMeta) {

                    // Abre a seleção do tipo e valor da meta
                    selecionandoTipoMeta = true
                    renderizarEtapa()

                } else {

                    // Salva o questionário completo
                    salvarQuestionario()
                }

            } else {

                etapaAtual++
                renderizarEtapa()
            }
        }

        btnAnterior.setOnClickListener {

            if (etapaAtual == 7 && selecionandoTipoMeta) {

                selecionandoTipoMeta = false

            } else if (etapaAtual > 1) {

                etapaAtual--
            }

            renderizarEtapa()
        }

        btnVoltarLogin.setOnClickListener {
            realizarLogout()
        }

        findViewById<android.widget.TextView>(
            R.id.txv_voltarLogin
        ).setOnClickListener {
            realizarLogout()
        }

        edtData.setOnClickListener {
            abrirSeletorData()
        }
    }

    // =====================================================
    // RENDERIZAÇÃO DAS ETAPAS
    // =====================================================

    private fun renderizarEtapa() {

        esconderCampos()

        progresso.progress = (etapaAtual * 100) / totalEtapas

        txvNumero.text = "Questão %02d".format(etapaAtual)

        btnAnterior.visibility =
            if (etapaAtual == 1 && !selecionandoTipoMeta) {
                View.GONE
            } else {
                View.VISIBLE
            }

        btnProxima.text = if (etapaAtual == 7 && selecionandoTipoMeta) {
            "Finalizar  ✓"
        } else {
            "Próxima  →"
        }

        when (etapaAtual) {

            1 -> mostrarQuantidadePessoas()

            2 -> mostrarConcessionaria()

            3 -> mostrarData()

            4 -> mostrarMes(1)

            5 -> mostrarMes(2)

            6 -> mostrarMes(3)

            7 -> mostrarMeta()
        }

    }

    private fun esconderCampos() {

        grupoOpcoes.visibility = View.GONE

        layoutData.visibility = View.GONE
        layoutValor.visibility = View.GONE
        layoutConsumo.visibility = View.GONE

        layoutAjudaConsumo.visibility = View.GONE

        grupoOpcoes.removeAllViews()

        edtValor.setText("")
        edtConsumo.setText("")
    }

    // =====================================================
    // QUESTÃO 1 - QUANTIDADE DE PESSOAS
    // =====================================================

    private fun mostrarQuantidadePessoas() {

        txvPergunta.text =
            "Quantas pessoas moram na sua casa?"

        grupoOpcoes.visibility = View.VISIBLE

        val opcoes = (1..5).map {
            Pair("$it pessoa${if (it > 1) "s" else ""}", it.toString())
        }

        configurarOpcoes(
            opcoes,
            qtdPessoas?.toString()
        )
    }

    // =====================================================
    // QUESTÃO 2 - CONCESSIONÁRIA
    // =====================================================

    private fun mostrarConcessionaria() {

        txvPergunta.text =
            "Qual é a sua concessionária de energia?"

        grupoOpcoes.visibility = View.VISIBLE

        val opcoes = listOf(
            Pair("EDP", "edp"),
            Pair("Enel", "enel"),
            Pair("CPFL", "cpfl"),
            Pair("Elektro", "elektro"),
            Pair("Cemig", "cemig"),
            Pair("Light", "light"),
            Pair("Copel", "copel"),
            Pair("Outra", "outra")
        )

        configurarOpcoes(
            opcoes,
            concessionaria
        )
    }

    // =====================================================
    // QUESTÃO 3 - DATA
    // =====================================================

    private fun mostrarData() {

        txvPergunta.text =
            "Qual é o vencimento da sua última conta de energia?"

        layoutData.visibility = View.VISIBLE

        edtData.setText(
            formatarDataExibicao(vencimentoIso)
        )
    }

    private fun abrirSeletorData() {

        val calendario = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, ano, mes, dia ->

                val data = Calendar.getInstance()

                data.set(ano, mes, dia)

                vencimentoIso = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                ).format(data.time)

                edtData.setText(
                    SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale("pt", "BR")
                    ).format(data.time)
                )

            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)

        ).show()
    }

    private fun formatarDataExibicao(data: String?): String {

        if (data.isNullOrBlank()) return ""

        return try {

            val entrada = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )

            val saida = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale("pt", "BR")
            )

            saida.format(entrada.parse(data)!!)

        } catch (e: Exception) {

            ""
        }
    }

    // =====================================================
    // QUESTÕES 4, 5 E 6 - CONTAS DE ENERGIA
    // =====================================================

    private fun mostrarMes(numero: Int) {

        txvPergunta.text = when (numero) {

            1 -> "Informe os dados da sua primeira conta."

            2 -> "Informe os dados da sua segunda conta."

            else -> "Informe os dados da sua terceira conta."
        }

        layoutValor.visibility = View.VISIBLE
        layoutConsumo.visibility = View.VISIBLE

        layoutAjudaConsumo.visibility = View.VISIBLE

        layoutValor.hint = "Valor da conta (R$)"

        layoutConsumo.hint = "Consumo da conta (kWh)"

        when (numero) {

            1 -> {
                edtValor.setText(valorMes1)
                edtConsumo.setText(consumoMes1)
            }

            2 -> {
                edtValor.setText(valorMes2)
                edtConsumo.setText(consumoMes2)
            }

            3 -> {
                edtValor.setText(valorMes3)
                edtConsumo.setText(consumoMes3)
            }
        }

        if (numero == 1) {

            layoutAjudaConsumo.visibility = View.VISIBLE

        } else {

            layoutAjudaConsumo.visibility = View.GONE
        }
    }

    // =====================================================
    // QUESTÃO 7 - META
    // =====================================================

    private fun mostrarMeta() {

        grupoOpcoes.visibility = View.VISIBLE

        if (!selecionandoTipoMeta) {

            txvPergunta.text =
                "Você deseja definir uma meta de economia?"

            configurarOpcoes(
                listOf(
                    Pair("Sim", "sim"),
                    Pair("Não", "nao")
                ),
                definirMeta
            )

        } else {

            txvPergunta.text =
                "Qual tipo de meta você deseja definir?"

            configurarOpcoes(
                listOf(
                    Pair("Reduzir consumo (kWh)", "CONSUMO"),
                    Pair("Reduzir valor da conta (R$)", "VALOR")
                ),
                tipoMeta
            )

            layoutValor.visibility = View.VISIBLE

            layoutValor.hint = if (tipoMeta == "CONSUMO") {
                "Meta de consumo (kWh)"
            } else {
                "Meta de valor (R$)"
            }

            edtValor.setText(valorMeta)
        }
    }

    // =====================================================
    // CONFIGURAÇÃO DINÂMICA DO RADIOGROUP
    // =====================================================

    private fun configurarOpcoes(
        opcoes: List<Pair<String, String>>,
        selecionado: String?
    ) {

        grupoOpcoes.removeAllViews()

        opcoes.forEach { (texto, valor) ->

            val radio = RadioButton(this)

            radio.id = View.generateViewId()

            radio.text = texto
            radio.tag = valor

            radio.textSize = 18f

            radio.minHeight = 48.dp()

            radio.buttonTintList =
                android.content.res.ColorStateList.valueOf(
                    obterCorAtributo(com.google.android.material.R.attr.colorOnSurface)
                )

            grupoOpcoes.addView(radio)

            if (valor == selecionado) {
                grupoOpcoes.check(radio.id)
            }
        }
    }

    private fun obterCorAtributo(atributo: Int): Int {

        val typedValue = android.util.TypedValue()

        theme.resolveAttribute(
            atributo,
            typedValue,
            true
        )

        return typedValue.data
    }

    private fun Int.dp(): Int {

        return (this * resources.displayMetrics.density).toInt()
    }

    private fun obterOpcaoSelecionada(): String? {

        val selecionado = grupoOpcoes.checkedRadioButtonId

        if (selecionado == -1) return null

        val radio = grupoOpcoes.findViewById<RadioButton>(
            selecionado
        )

        return radio?.tag?.toString()
    }

    // =====================================================
    // CAPTURA E VALIDAÇÃO
    // =====================================================

    private fun capturarEValidarEtapa(): Boolean {

        when (etapaAtual) {

            1 -> {

                val resposta = obterOpcaoSelecionada()

                if (resposta == null) {
                    return mostrarErro("Selecione a quantidade de pessoas.")
                }

                qtdPessoas = resposta.toInt()
            }

            2 -> {

                val resposta = obterOpcaoSelecionada()

                if (resposta == null) {
                    return mostrarErro("Selecione sua concessionária.")
                }

                concessionaria = resposta
            }

            3 -> {

                if (vencimentoIso.isNullOrBlank()) {
                    return mostrarErro("Informe o vencimento da última conta.")
                }
            }

            4 -> {

                valorMes1 = edtValor.text.toString().trim()
                consumoMes1 = edtConsumo.text.toString().trim()

                if (!validarNumero(valorMes1) ||
                    !validarNumero(consumoMes1)) {

                    return mostrarErro(
                        "Informe o valor e o consumo do primeiro mês."
                    )
                }
            }

            5 -> {

                valorMes2 = edtValor.text.toString().trim()
                consumoMes2 = edtConsumo.text.toString().trim()

                if (!validarParOpcional(valorMes2, consumoMes2)) {

                    return mostrarErro(
                        "Informe o valor e o consumo do segundo mês, ou deixe os dois vazios."
                    )
                }
            }

            6 -> {

                valorMes3 = edtValor.text.toString().trim()
                consumoMes3 = edtConsumo.text.toString().trim()

                if (!validarParOpcional(valorMes3, consumoMes3)) {

                    return mostrarErro(
                        "Informe o valor e o consumo do terceiro mês, ou deixe os dois vazios."
                    )
                }
            }

            7 -> {

                if (!selecionandoTipoMeta) {

                    val resposta = obterOpcaoSelecionada()

                    if (resposta == null) {
                        return mostrarErro("Selecione Sim ou Não.")
                    }

                    definirMeta = resposta

                    if (resposta == "nao") {

                        tipoMeta = null
                        valorMeta = ""
                    }

                } else {

                    tipoMeta = obterOpcaoSelecionada()

                    if (tipoMeta == null) {
                        return mostrarErro("Selecione o tipo de meta.")
                    }

                    valorMeta = edtValor.text.toString().trim()

                    if (!validarNumero(valorMeta)) {

                        return mostrarErro(
                            "Informe um valor válido para sua meta."
                        )
                    }

                    if (valorMeta.replace(",", ".").toBigDecimalOrNull()
                            ?.signum() != 1) {

                        return mostrarErro(
                            "A meta deve ser maior que zero."
                        )
                    }
                }
            }
        }

        return true
    }

    private fun validarNumero(valor: String): Boolean {

        return valor.matches(
            Regex("^\\d+([.,]\\d{1,2})?$")
        )
    }

    private fun validarParOpcional(
        valor: String,
        consumo: String
    ): Boolean {

        if (valor.isBlank() && consumo.isBlank()) {
            return true
        }

        return validarNumero(valor) &&
                validarNumero(consumo)
    }

    private fun mostrarErro(mensagem: String): Boolean {

        Toast.makeText(
            this,
            mensagem,
            Toast.LENGTH_LONG
        ).show()

        return false
    }

    // =====================================================
    // ENVIAR QUESTIONÁRIO
    // =====================================================


    private fun salvarQuestionario() {

        val questionario = Questionario(

            qtdPessoas = qtdPessoas!!,
            concessionaria = concessionaria!!,
            vencimentoUltimaConta = vencimentoIso!!,

            valorMes1 = valorMes1,
            valorMes2 = valorMes2.ifBlank { null },
            valorMes3 = valorMes3.ifBlank { null },

            consumoMes1 = consumoMes1,
            consumoMes2 = consumoMes2.ifBlank { null },
            consumoMes3 = consumoMes3.ifBlank { null },

            definirMeta = definirMeta!!,
            tipoMeta = tipoMeta,

            valorMeta = if (definirMeta == "sim") {
                valorMeta
            } else {
                null
            }
        )

        btnProxima.isEnabled = false
        btnAnterior.isEnabled = false

        lifecycleScope.launch {

            try {

                Log.d(
                    "ZATTS_QUESTIONARIO",
                    "Enviando questionário..."
                )

                val resposta = repository.salvar(questionario)

                Log.d(
                    "ZATTS_QUESTIONARIO",
                    "Código HTTP: ${resposta.code()}"
                )

                Log.d(
                    "ZATTS_QUESTIONARIO",
                    "Sucesso: ${resposta.isSuccessful}"
                )

                if (resposta.isSuccessful) {

                    Log.d(
                        "ZATTS_QUESTIONARIO",
                        "Resposta: ${resposta.body()}"
                    )

                    Toast.makeText(
                        this@QuestionarioActivity,
                        "Questionário concluído com sucesso!",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(
                        this@QuestionarioActivity,
                        HomeActivity::class.java
                    )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)

                    finish()

                } else {

                    val erro = resposta.errorBody()?.string()

                    Log.e(
                        "ZATTS_QUESTIONARIO",
                        "Erro HTTP: ${resposta.code()}"
                    )

                    Log.e(
                        "ZATTS_QUESTIONARIO",
                        "Corpo do erro: $erro"
                    )

                    val mensagem = try {

                        JSONObject(erro ?: "{}")
                            .optString(
                                "erro",
                                "Não foi possível salvar o questionário."
                            )

                    } catch (e: Exception) {

                        "Erro HTTP: ${resposta.code()}"
                    }

                    mostrarMensagem(mensagem)
                }

            } catch (e: CancellationException) {

                throw e

            } catch (e: IOException) {

                Log.e(
                    "ZATTS_QUESTIONARIO",
                    "Erro de conexão.",
                    e
                )

                mostrarMensagem(
                    "Não foi possível conectar ao servidor."
                )

            } catch (e: Exception) {

                Log.e(
                    "ZATTS_QUESTIONARIO",
                    "Erro inesperado ao salvar questionário.",
                    e
                )

                mostrarMensagem(
                    "Erro inesperado ao salvar o questionário."
                )

            } finally {

                btnProxima.isEnabled = true
                btnAnterior.isEnabled = true
            }
        }
    }


    // =====================================================
    // LOGOUT
    // =====================================================

    private fun realizarLogout() {

        btnVoltarLogin.isEnabled = false

        lifecycleScope.launch {

            try {

                val resposta = RetrofitClient.apiService.logout()

                Log.d(
                    "ZATTS_SESSAO",
                    "Logout HTTP: ${resposta.code()}"
                )

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                Log.e(
                    "ZATTS_SESSAO",
                    "Erro ao realizar logout.",
                    e
                )

            } finally {

                // Remove o cookie armazenado no aplicativo
                RetrofitClient.limparCookies()

                val intent = Intent(
                    this@QuestionarioActivity,
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

    private fun mostrarMensagem(mensagem: String) {

        Toast.makeText(
            this,
            mensagem,
            Toast.LENGTH_LONG
        ).show()
    }
}

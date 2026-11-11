package com.zattsproject.mobile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.zattsproject.mobile.R
import androidx.lifecycle.lifecycleScope
import com.zattsproject.mobile.Model.AjudaContatoRequest
import com.zattsproject.mobile.Network.RetrofitClient
import kotlinx.coroutines.launch

class AjudaSuporteActivity : AppCompatActivity() {

    private lateinit var edtBuscaFaq: EditText
    private lateinit var edtAssunto: AutoCompleteTextView
    private lateinit var edtMensagem: EditText
    private lateinit var layoutMensagem: TextInputLayout
    private lateinit var btnEnviarMensagem: MaterialButton

    private data class PerguntaFaq(
        val card: MaterialCardView,
        val pergunta: TextView,
        val resposta: TextView,
        val icone: ImageView
    )

    private fun chaveAssunto(
        assunto: String
    ): String {

        return when (assunto) {

            "Conta e acesso" ->
                "conta"

            "Dispositivo" ->
                "dispositivo"

            "Consumo de energia" ->
                "consumo"

            "Metas de consumo" ->
                "meta"

            "Alertas e notificações" ->
                "meta"

            "Configurações do aplicativo" ->
                "outro"

            "Outro assunto" ->
                "outro"

            else ->
                "outro"
        }
    }

    private val perguntasFaq = mutableListOf<PerguntaFaq>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajuda_suporte)

        inicializarComponentes()
        configurarVoltar()
        configurarPerguntasFrequentes()
        configurarBuscaFaq()
        configurarAssuntos()
        configurarEnvioMensagem()
    }

    private fun inicializarComponentes() {
        edtBuscaFaq = findViewById(R.id.edtBuscaFaq)
        edtAssunto = findViewById(R.id.edtAssunto)
        edtMensagem = findViewById(R.id.edtMensagem)
        layoutMensagem = findViewById(R.id.layoutMensagem)
        btnEnviarMensagem = findViewById(R.id.btnEnviarMensagem)

        perguntasFaq.add(
            PerguntaFaq(
                findViewById(R.id.cardFaq1),
                findViewById(R.id.txvPerguntaFaq1),
                findViewById(R.id.txvRespostaFaq1),
                findViewById(R.id.iconeFaq1)
            )
        )
        perguntasFaq.add(
            PerguntaFaq(
                findViewById(R.id.cardFaq2),
                findViewById(R.id.txvPerguntaFaq2),
                findViewById(R.id.txvRespostaFaq2),
                findViewById(R.id.iconeFaq2)
            )
        )
        perguntasFaq.add(
            PerguntaFaq(
                findViewById(R.id.cardFaq3),
                findViewById(R.id.txvPerguntaFaq3),
                findViewById(R.id.txvRespostaFaq3),
                findViewById(R.id.iconeFaq3)
            )
        )
        perguntasFaq.add(
            PerguntaFaq(
                findViewById(R.id.cardFaq4),
                findViewById(R.id.txvPerguntaFaq4),
                findViewById(R.id.txvRespostaFaq4),
                findViewById(R.id.iconeFaq4)
            )
        )
        perguntasFaq.add(
            PerguntaFaq(
                findViewById(R.id.cardFaq5),
                findViewById(R.id.txvPerguntaFaq5),
                findViewById(R.id.txvRespostaFaq5),
                findViewById(R.id.iconeFaq5)
            )
        )
        perguntasFaq.add(
            PerguntaFaq(
                findViewById(R.id.cardFaq6),
                findViewById(R.id.txvPerguntaFaq6),
                findViewById(R.id.txvRespostaFaq6),
                findViewById(R.id.iconeFaq6)
            )
        )
        perguntasFaq.add(
            PerguntaFaq(
                findViewById(R.id.cardFaq7),
                findViewById(R.id.txvPerguntaFaq7),
                findViewById(R.id.txvRespostaFaq7),
                findViewById(R.id.iconeFaq7)
            )
        )
    }

    private fun configurarVoltar() {
        findViewById<FloatingActionButton>(R.id.btn_voltar).setOnClickListener {
            finish()
        }
    }

    private fun configurarPerguntasFrequentes() {
        perguntasFaq.forEach { item ->
            item.card.setOnClickListener {
                val expandir = item.resposta.visibility != View.VISIBLE

                item.resposta.visibility = if (expandir) View.VISIBLE else View.GONE
                item.icone.animate()
                    .rotation(if (expandir) 180f else 0f)
                    .setDuration(180)
                    .start()
            }
        }
    }

    private fun configurarBuscaFaq() {
        edtBuscaFaq.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                texto: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                texto: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val pesquisa = texto?.toString()?.trim().orEmpty()

                perguntasFaq.forEach { item ->
                    val corresponde = item.pergunta.text
                        .toString()
                        .contains(pesquisa, ignoreCase = true)

                    item.card.visibility = if (corresponde) View.VISIBLE else View.GONE

                    if (!corresponde) {
                        item.resposta.visibility = View.GONE
                        item.icone.rotation = 0f
                    }
                }
            }

            override fun afterTextChanged(texto: Editable?) = Unit
        })
    }

    private fun configurarAssuntos() {
        val assuntos = listOf(
            "Conta e acesso",
            "Dispositivo",
            "Consumo de energia",
            "Metas de consumo",
            "Alertas e notificações",
            "Configurações do aplicativo",
            "Outro assunto"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            assuntos
        )

        edtAssunto.setAdapter(adapter)
        edtAssunto.setOnClickListener {
            edtAssunto.showDropDown()
        }
    }
    private fun configurarEnvioMensagem() {

        btnEnviarMensagem.setOnClickListener {

            val assunto =
                edtAssunto.text
                    .toString()
                    .trim()

            val mensagem =
                edtMensagem.text
                    .toString()
                    .trim()

            if (assunto.isEmpty()) {

                edtAssunto.error =
                    "Selecione um assunto."

                return@setOnClickListener
            }

            if (mensagem.isEmpty()) {

                layoutMensagem.error =
                    "Digite sua mensagem."

                edtMensagem.requestFocus()

                return@setOnClickListener
            }

            if (mensagem.length > 1000) {

                layoutMensagem.error =
                    "A mensagem deve ter no máximo 1000 caracteres."

                edtMensagem.requestFocus()

                return@setOnClickListener
            }

            layoutMensagem.error = null

            enviarMensagem(
                assunto,
                mensagem
            )
        }
    }
    private fun enviarMensagem(
        assunto: String,
        mensagem: String
    ) {

        btnEnviarMensagem.isEnabled = false

        lifecycleScope.launch {

            try {

                val resposta =
                    RetrofitClient.apiService
                        .enviarContatoSuporte(
                            AjudaContatoRequest(
                                assunto =
                                    chaveAssunto(
                                        assunto
                                    ),
                                mensagem = mensagem
                            )
                        )

                if (resposta.isSuccessful) {

                    Toast.makeText(
                        this@AjudaSuporteActivity,
                        resposta.body()
                            ?.mensagem
                            ?: "Mensagem enviada com sucesso.",
                        Toast.LENGTH_LONG
                    ).show()

                    edtMensagem.setText("")
                    edtAssunto.setText(
                        "",
                        false
                    )

                } else {

                    val texto =
                        when (resposta.code()) {

                            400 ->
                                "Verifique o assunto e a mensagem."

                            401 ->
                                "Sua sessão expirou. Entre novamente."

                            else ->
                                "Não foi possível enviar sua mensagem."
                        }

                    Toast.makeText(
                        this@AjudaSuporteActivity,
                        texto,
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@AjudaSuporteActivity,
                    "Não foi possível conectar ao servidor.",
                    Toast.LENGTH_LONG
                ).show()

            } finally {

                btnEnviarMensagem.isEnabled = true
            }
        }
    }
}
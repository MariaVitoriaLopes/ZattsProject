
package com.zattsproject.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.google.android.material.textfield.TextInputEditText
import com.zattsproject.mobile.Model.Cadastro
import com.zattsproject.mobile.Repository.UsuarioRepository

import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

import android.graphics.Color
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged

class CadastroActivity : AppCompatActivity() {

    private val repository = UsuarioRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro)

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

        val edtEmail = findViewById<EditText>(R.id.edt_emailLogin)
        val edtSenha = findViewById<TextInputEditText>(R.id.edt_senhaLogin)
        val edtConfirmarSenha = findViewById<TextInputEditText>(R.id.edt_confirmarSenha)
        val btnCadastrar = findViewById<Button>(R.id.btn_recuperarSenha)
        val btnVoltar = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_login)
        val txvTamanho = findViewById<TextView>(R.id.txv_tamanho)
        val txvEspecial = findViewById<TextView>(R.id.txv_especial)
        val txvMaiuscula = findViewById<TextView>(R.id.txv_maiuscula)
        val txvMinuscula = findViewById<TextView>(R.id.txv_minuscula)
        val txvNumero = findViewById<TextView>(R.id.txv_numero)
        val txvCoincidem = findViewById<TextView>(R.id.txv_coincidem)
        val txvLinkCadastro = findViewById<TextView>(R.id.txv_linkLogin)

        btnVoltar.setOnClickListener {
            finish()
        }


        val corVerde = Color.parseColor("#2E7D32")
        val corPadrao = Color.parseColor("#757575")

        fun atualizarRequisito(
            textView: TextView,
            valido: Boolean
        ) {
            textView.setTextColor(
                if (valido) corVerde else corPadrao
            )
        }

        txvLinkCadastro.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        fun verificarSenha() {
            val senha = edtSenha.text.toString()
            val confirmarSenha = edtConfirmarSenha.text.toString()

            atualizarRequisito(
                txvTamanho,
                senha.length >= 8
            )

            atualizarRequisito(
                txvEspecial,
                senha.any { !it.isLetterOrDigit() && !it.isWhitespace() }
            )

            atualizarRequisito(
                txvMaiuscula,
                senha.any { it.isUpperCase() }
            )

            atualizarRequisito(
                txvMinuscula,
                senha.any { it.isLowerCase() }
            )

            atualizarRequisito(
                txvNumero,
                senha.any { it.isDigit() }
            )

            atualizarRequisito(
                txvCoincidem,
                confirmarSenha.isNotEmpty() && senha == confirmarSenha
            )
        }

// Verifica enquanto digita a senha
        edtSenha.doAfterTextChanged {
            verificarSenha()
        }

// Verifica enquanto digita a confirmação
        edtConfirmarSenha.doAfterTextChanged {
            verificarSenha()
        }

// Define o estado inicial dos requisitos
        verificarSenha()


        btnCadastrar.setOnClickListener {

            val email = edtEmail.text.toString().trim()
            val senha = edtSenha.text.toString()
            val confirmarSenha =
                edtConfirmarSenha.text.toString()

            // VALIDAR CAMPOS
            if (email.isBlank() ||
                senha.isBlank() ||
                confirmarSenha.isBlank()
            ) {
                mostrarMensagem("Preencha todos os campos!")
                return@setOnClickListener
            }

            // VALIDAR E-MAIL
            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email).matches()
            ) {
                edtEmail.error = "E-mail inválido"
                return@setOnClickListener
            }

            val senhaValida =
                senha.length >= 8 &&
                        senha.any { it.isUpperCase() } &&
                        senha.any { it.isLowerCase() } &&
                        senha.any { it.isDigit() } &&
                        senha.any {
                            !it.isLetterOrDigit() && !it.isWhitespace()
                        }

            if (!senhaValida) {
                edtSenha.error = "A senha não atende aos requisitos."
                mostrarMensagem("Verifique os requisitos da senha.")
                return@setOnClickListener
            }

            // VALIDAR SENHAS
            if (senha != confirmarSenha) {
                edtConfirmarSenha.error =
                    "As senhas não coincidem"
                return@setOnClickListener
            }

            val cadastro = Cadastro(
                email = email,
                senha = senha,
                confirmarSenha = confirmarSenha
            )

            btnCadastrar.isEnabled = false

            lifecycleScope.launch {
                try {
                    val resposta =
                        repository.cadastrar(cadastro)

                    if (resposta.isSuccessful) {

                        mostrarMensagem(
                            resposta.body()?.mensagem
                                ?: "Cadastro realizado com sucesso!"
                        )

                        val intent = Intent(this@CadastroActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {

                        val codigoHttp = resposta.code()
                        val erroJson = resposta.errorBody()?.string()

                        Log.e(
                            "ZATTS_API",
                            "Erro HTTP: $codigoHttp"
                        )

                        Log.e(
                            "ZATTS_API",
                            "Resposta do servidor: $erroJson"
                        )

                        val mensagemErro = try {
                            org.json.JSONObject(
                                erroJson ?: "{}"
                            ).optString(
                                "erro",
                                "Erro HTTP: $codigoHttp"
                            )
                        } catch (e: Exception) {
                            "Erro HTTP: $codigoHttp"
                        }

                        mostrarMensagem(mensagemErro)
                    }

                } catch (e: IOException) {

                    Log.e(
                        "ZATTS_API",
                        "ERRO AO CONECTAR COM SPRING BOOT",
                        e
                    )

                    mostrarMensagem(
                        "Erro: ${e.message}"
                    )
                } catch (e: HttpException) {

                    mostrarMensagem(
                        "Erro HTTP: ${e.code()}"
                    )

                } catch (e: Exception) {

                    mostrarMensagem(
                        "Erro inesperado ao realizar cadastro."
                    )

                } finally {
                    btnCadastrar.isEnabled = true
                }
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

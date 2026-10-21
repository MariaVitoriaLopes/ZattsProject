
package com.zattsproject.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

import com.zattsproject.mobile.Model.Login
import com.zattsproject.mobile.Repository.UsuarioRepository

import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private val repository = UsuarioRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

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

        // CAMPOS
        val edtEmail = findViewById<EditText>(
            R.id.edt_emailLogin
        )

        val edtSenha = findViewById<TextInputEditText>(
            R.id.edt_senhaLogin
        )

        val btnEntrar = findViewById<Button>(
            R.id.btn_recuperarSenha
        )

        val btnVoltar = findViewById<FloatingActionButton>(
            R.id.fab_login
        )

        val txvCadastrar = findViewById<TextView>(
            R.id.txv_linkLogin
        )

        // VOLTAR
        btnVoltar.setOnClickListener {
            finish()
        }

        // IR PARA CADASTRO
        txvCadastrar.setOnClickListener {
            val intent = Intent(
                this,
                CadastroActivity::class.java
            )

            startActivity(intent)
        }

        // ENTRAR
        btnEntrar.setOnClickListener {

            val email = edtEmail.text.toString().trim()
            val senha = edtSenha.text.toString()

            // VALIDAR CAMPOS
            if (email.isBlank() || senha.isBlank()) {
                mostrarMensagem("Preencha todos os campos!")
                return@setOnClickListener
            }

            // VALIDAR E-MAIL
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.error = "E-mail inválido"
                return@setOnClickListener
            }

            val login = Login(
                email = email,
                senha = senha
            )

            btnEntrar.isEnabled = false

            lifecycleScope.launch {

                try {

                    val resposta = repository.login(login)

                    Log.d(
                        "ZATTS_API",
                        "LOGIN - HTTP: ${resposta.code()}"
                    )

                    Log.d(
                        "ZATTS_API",
                        "LOGIN - URL: ${resposta.raw().request().url()}"
                    )

                    if (resposta.isSuccessful) {

                        mostrarMensagem(
                            resposta.body()?.mensagem
                                ?: "Login realizado com sucesso!"
                        )

                        // Por enquanto, o backend apenas
                        // confirma as credenciais.
                        // A navegação para a Home ficará
                        // para quando implementarmos
                        // a autenticação da sessão.
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {

                        val codigoHttp = resposta.code()
                        val erroJson = resposta.errorBody()?.string()

                        Log.e(
                            "ZATTS_API",
                            "Erro HTTP no login: $codigoHttp"
                        )

                        Log.e(
                            "ZATTS_API",
                            "Resposta do login: $erroJson"
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

                } catch (e: CancellationException) {
                    throw e

                } catch (e: IOException) {

                    Log.e(
                        "ZATTS_API",
                        "Erro ao conectar com Spring Boot",
                        e
                    )

                    mostrarMensagem(
                        "Não foi possível conectar ao servidor."
                    )

                } catch (e: Exception) {

                    Log.e(
                        "ZATTS_API",
                        "Erro inesperado no login",
                        e
                    )

                    mostrarMensagem(
                        "Erro inesperado ao realizar login."
                    )

                } finally {
                    btnEntrar.isEnabled = true
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

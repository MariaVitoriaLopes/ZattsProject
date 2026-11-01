package com.zattsproject.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.zattsproject.mobile.Network.RetrofitClient
import kotlinx.coroutines.launch
import android.util.Log

class RecuperarSenhaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar_senha)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // COMPONENTES
        val edtEmail = findViewById<TextInputEditText>(R.id.edt_emailLogin)
        val btnRecuperarSenha = findViewById<MaterialButton>(R.id.btn_recuperarSenha)
        val txvLinkRecSenha = findViewById<TextView>(R.id.txv_linkRecSenha)
        val txvVoltarLogin = findViewById<TextView>(R.id.txv_voltarLogin)
        val fabLogin = findViewById<FloatingActionButton>(R.id.fab_login)

        // VOLTAR PARA LOGIN
        fabLogin.setOnClickListener {
            finish()
        }

        txvVoltarLogin.setOnClickListener {
            finish()
        }

        // IR PARA CADASTRO
        txvLinkRecSenha.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        // ENVIAR EMAIL DE RECUPERAÇÃO
        btnRecuperarSenha.setOnClickListener {

            val email = edtEmail.text.toString().trim()

            if (email.isEmpty()) {
                edtEmail.error = "Informe seu e-mail"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.error = "Informe um e-mail válido"
                return@setOnClickListener
            }

            btnRecuperarSenha.isEnabled = false

            lifecycleScope.launch {
                try {
                    val resposta = RetrofitClient.apiService.recuperarSenha(email)

                    if (resposta.isSuccessful) {
                        Toast.makeText(
                            this@RecuperarSenhaActivity,
                            "Se o e-mail estiver cadastrado, você receberá um link de recuperação.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@RecuperarSenhaActivity,
                            "Não foi possível solicitar a recuperação.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } catch (e: Exception) {

                    Log.e(
                        "ZATTS_RECUPERACAO",
                        "Erro ao solicitar recuperação de senha",
                        e
                    )

                    Toast.makeText(
                        this@RecuperarSenhaActivity,
                        "Erro: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    btnRecuperarSenha.isEnabled = true
                }
            }
        }
    }
}

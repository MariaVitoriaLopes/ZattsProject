package com.zattsproject.mobile

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.zattsproject.mobile.Network.RetrofitClient
import com.zattsproject.mobile.Repository.UsuarioRepository

import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val usuarioRepository =
        UsuarioRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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

        // LOGO CONFORME O TEMA
        val imvLogo = findViewById<ImageView>(
            R.id.imv_logo
        )

        val modoEscuro = (
                resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK
                ) == Configuration.UI_MODE_NIGHT_YES

        val logo = if (modoEscuro) {
            R.drawable.logo_oficial_branca_exportada
        } else {
            R.drawable.logo_oficial_preta_exportada
        }

        imvLogo.setImageDrawable(
            AppCompatResources.getDrawable(
                this,
                logo
            )
        )

        // BOTÃO COMEÇAR
        val btnComecar = findViewById<Button>(
            R.id.btn_comecar
        )

        btnComecar.setOnClickListener {

            val intent = Intent(
                this,
                CadastroActivity::class.java
            )

            startActivity(intent)
        }

        verificarSessaoExistente()
    }

    private fun verificarSessaoExistente() {

        lifecycleScope.launch {

            try {

                val resposta =
                    usuarioRepository.verificarSessao()

                // Não existe mais sessão válida
                if (!resposta.isSuccessful) {

                    if (resposta.code() == 401) {
                        RetrofitClient.limparCookies()
                    }

                    return@launch
                }

                val usuario =
                    resposta.body()
                        ?: return@launch

                RetrofitClient
                    .marcarSessaoAutenticada()

                val destino =
                    if (usuario.questionarioRespondido == true) {

                        HomeActivity::class.java

                    } else {

                        IniciarPesquisaActivity::class.java
                    }

                val intent = Intent(
                    this@MainActivity,
                    destino
                )

                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)

                finish()

            } catch (e: Exception) {
            }
        }
    }
}
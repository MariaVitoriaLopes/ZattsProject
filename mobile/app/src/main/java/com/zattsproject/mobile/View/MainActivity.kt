
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

class MainActivity : AppCompatActivity() {

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
            AppCompatResources.getDrawable(this, logo)
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
    }
}

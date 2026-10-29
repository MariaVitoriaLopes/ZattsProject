
package com.zattsproject.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge
import com.zattsproject.mobile.ContaActivity
import com.zattsproject.mobile.AppNavBar
import com.zattsproject.mobile.NavTab
import android.content.res.ColorStateList

class SettingsActivity : AppCompatActivity() {

    private var corAplicada = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)
        corAplicada = AparenciaManager.obterCor(this)

        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        // Ajusta o conteúdo às barras do Android
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // Seleciona Configurações na navbar
        findViewById<AppNavBar>(R.id.navBar)
            .setSelected(NavTab.SETTINGS)

        // Abrir Conta
        findViewById<View>(R.id.item_conta)
            .setOnClickListener {
                startActivity(
                    Intent(this, ContaActivity::class.java)
                )
            }

        // Abrir Preferências
        findViewById<View>(R.id.item_preferencias)
            .setOnClickListener {
                startActivity(
                    Intent(this, PreferenciasActivity::class.java)
                )
            }
    }

    override fun onResume() {
        super.onResume()

        val corAtual = AparenciaManager.obterCor(this)

        if (corAtual != corAplicada) {
            recreate()
        }
    }
}

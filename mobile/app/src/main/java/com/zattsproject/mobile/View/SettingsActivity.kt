
package com.zattsproject.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private var corAplicada = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)

        corAplicada = AparenciaManager.obterCor(this)

        setContentView(R.layout.activity_settings)

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

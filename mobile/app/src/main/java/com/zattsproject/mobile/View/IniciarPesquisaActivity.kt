
package com.zattsproject.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zattsproject.mobile.QuestionarioActivity

class IniciarPesquisaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_iniciar_pesquisa)

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

        // BOTÃO COMEÇAR QUESTIONÁRIO
        val btnComecar = findViewById<Button>(
            R.id.btn_comecarQuestionario
        )

        btnComecar.setOnClickListener {

            val intent = Intent(
                this,
                QuestionarioActivity::class.java
            )

            startActivity(intent)
        }
    }
}

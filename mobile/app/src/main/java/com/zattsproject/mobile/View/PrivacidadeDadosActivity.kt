package com.zattsproject.mobile

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.zattsproject.mobile.AparenciaManager
import com.zattsproject.mobile.AppNavBar
import com.zattsproject.mobile.NavTab
import com.zattsproject.mobile.R
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.lifecycleScope
import com.zattsproject.mobile.Model.PrivacidadeConfiguracao
import com.zattsproject.mobile.Network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

class PrivacidadeDadosActivity : AppCompatActivity() {

    private lateinit var switchDadosAnonimos: SwitchCompat
    private lateinit var switchRecomendacoes: SwitchCompat
    private lateinit var switchComparacao: SwitchCompat

    private var carregandoPrivacidade = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AparenciaManager.aplicarCor(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_privacidade_dados)

        findViewById<AppNavBar>(R.id.navBar)
            .setSelected(NavTab.SETTINGS)

        configurarVoltar()
        configurarPrivacidade()
        configurarBotoes()
        carregarPrivacidade()
    }

    private fun configurarVoltar() {

        findViewById<TextView>(
            R.id.btnVoltarPrivacidade
        ).setOnClickListener {
            finish()
        }
    }

    private fun configurarPrivacidade() {

        switchDadosAnonimos =
            findViewById(
                R.id.switchDadosAnonimos
            )

        switchRecomendacoes =
            findViewById(
                R.id.switchRecomendacoes
            )

        switchComparacao =
            findViewById(
                R.id.switchComparacao
            )

        val listener =
            android.widget.CompoundButton
                .OnCheckedChangeListener { _, _ ->

                    if (!carregandoPrivacidade) {
                        salvarPrivacidade()
                    }
                }

        switchDadosAnonimos
            .setOnCheckedChangeListener(listener)

        switchRecomendacoes
            .setOnCheckedChangeListener(listener)

        switchComparacao
            .setOnCheckedChangeListener(listener)
    }

    private fun carregarPrivacidade() {
        lifecycleScope.launch {
            try {
                val resposta =
                    RetrofitClient.apiService
                        .buscarPrivacidade()
                if (resposta.isSuccessful) {
                    val dados =
                        resposta.body()
                            ?: return@launch
                    carregandoPrivacidade = true
                    switchDadosAnonimos.isChecked =
                        dados.compartilharDadosUso
                    switchRecomendacoes.isChecked =
                        dados.recomendacoesPersonalizadas
                    switchComparacao.isChecked =
                        dados.comparacaoResidencias
                    carregandoPrivacidade = false
                } else {
                    Toast.makeText(
                        this@PrivacidadeDadosActivity,
                        if (resposta.code() == 401) {
                            "Sua sessão expirou. Entre novamente."
                        } else {
                            "Não foi possível carregar as preferências."
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                carregandoPrivacidade = false
                Toast.makeText(
                    this@PrivacidadeDadosActivity,
                    "Não foi possível conectar ao servidor.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun salvarPrivacidade() {
        val dados =
            PrivacidadeConfiguracao(
                compartilharDadosUso =
                    switchDadosAnonimos.isChecked,
                recomendacoesPersonalizadas =
                    switchRecomendacoes.isChecked,
                comparacaoResidencias =
                    switchComparacao.isChecked
            )
        lifecycleScope.launch {
            try {
                val resposta =
                    RetrofitClient.apiService
                        .salvarPrivacidade(dados)
                if (!resposta.isSuccessful) {
                    Toast.makeText(
                        this@PrivacidadeDadosActivity,
                        "Não foi possível salvar as preferências.",
                        Toast.LENGTH_SHORT
                    ).show()
                    carregarPrivacidade()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PrivacidadeDadosActivity,
                    "Não foi possível salvar as preferências.",
                    Toast.LENGTH_SHORT
                ).show()
                carregarPrivacidade()
            }
        }
    }

    private fun salvarArquivoExportacao(
        corpo: ResponseBody
    ): String {
        val nomeArquivo =
            "zatts-dados.json"
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q
        ) {
            val valores =
                ContentValues().apply {
                    put(
                        MediaStore.Downloads.DISPLAY_NAME,
                        nomeArquivo
                    )
                    put(
                        MediaStore.Downloads.MIME_TYPE,
                        "application/json"
                    )
                    put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS
                    )
                }
            val uri =
                contentResolver.insert(
                    MediaStore.Downloads
                        .EXTERNAL_CONTENT_URI,
                    valores
                )
                    ?: throw IllegalStateException(
                        "Não foi possível criar o arquivo."
                    )
            try {
                contentResolver
                    .openOutputStream(uri)
                    ?.use { saida ->

                        corpo.byteStream()
                            .use { entrada ->

                                entrada.copyTo(saida)
                            }
                    }
                    ?: throw IllegalStateException(
                        "Não foi possível abrir o arquivo."
                    )
            } catch (e: Exception) {
                contentResolver.delete(
                    uri,
                    null,
                    null
                )
                throw e
            }
            return "Downloads/$nomeArquivo"
        }
        val pasta =
            getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS
            ) ?: filesDir
        val arquivo =
            File(
                pasta,
                nomeArquivo
            )
        FileOutputStream(arquivo)
            .use { saida ->
                corpo.byteStream()
                    .use { entrada ->
                        entrada.copyTo(saida)
                    }
            }
        return arquivo.absolutePath
    }

    private fun exportarDados() {

        lifecycleScope.launch {

            try {

                val resposta =
                    RetrofitClient.apiService
                        .exportarDados()

                if (!resposta.isSuccessful) {

                    Toast.makeText(
                        this@PrivacidadeDadosActivity,
                        if (resposta.code() == 401) {
                            "Sua sessão expirou. Entre novamente."
                        } else {
                            "Não foi possível exportar os dados."
                        },
                        Toast.LENGTH_SHORT
                    ).show()

                    return@launch
                }

                val corpo = resposta.body()

                if (corpo == null) {

                    Toast.makeText(
                        this@PrivacidadeDadosActivity,
                        "Arquivo de exportação vazio.",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@launch
                }

                val local =
                    withContext(Dispatchers.IO) {
                        salvarArquivoExportacao(corpo)
                    }

                Toast.makeText(
                    this@PrivacidadeDadosActivity,
                    "Dados exportados para $local",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {

                Toast.makeText(
                    this@PrivacidadeDadosActivity,
                    "Não foi possível exportar os dados.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun configurarBotoes() {

        findViewById<TextView>(
            R.id.btnExportarDados
        ).setOnClickListener {
            exportarDados()
        }

        findViewById<TextView>(
            R.id.btnApagarHistorico
        ).setOnClickListener {
            confirmarExclusaoHistorico()
        }

        findViewById<View>(
            R.id.btnPoliticaPrivacidade
        ).setOnClickListener {
            abrirDocumento("Política de Privacidade")
        }

        findViewById<View>(
            R.id.btnTermosUso
        ).setOnClickListener {
            abrirDocumento("Termos de Uso")
        }
    }

    private fun confirmarExclusaoHistorico() {

        AlertDialog.Builder(this)
            .setTitle("Apagar histórico de consumo")
            .setMessage(
                "Tem certeza de que deseja apagar todas as leituras de consumo salvas? " +
                        "Esta ação não poderá ser desfeita."
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Apagar") { _, _ ->
                apagarHistorico()
            }
            .show()
    }

    private fun apagarHistorico() {
        lifecycleScope.launch {
            try {
                val resposta =
                    RetrofitClient.apiService
                        .apagarHistorico()
                if (resposta.isSuccessful) {
                    val quantidade =
                        resposta.body()
                            ?.registrosExcluidos
                            ?: 0
                    Toast.makeText(
                        this@PrivacidadeDadosActivity,
                        "Histórico apagado. " +
                                "$quantidade registros excluídos.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@PrivacidadeDadosActivity,
                        if (resposta.code() == 401) {
                            "Sua sessão expirou."
                        } else {
                            "Não foi possível apagar o histórico."
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PrivacidadeDadosActivity,
                    "Não foi possível apagar o histórico.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun abrirDocumento(nome: String) {

        Toast.makeText(
            this,
            "$nome: documento ainda não disponibilizado.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
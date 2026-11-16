package com.zattsproject.mobile

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.zattsproject.mobile.Model.Notificacao
import com.zattsproject.mobile.Network.RetrofitClient
import com.zattsproject.mobile.Repository.NotificacaoRepository
import java.util.concurrent.TimeUnit

class NotificacaoWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        val usuarioId = RetrofitClient.obterUsuarioId()

        if (usuarioId.isNullOrBlank()) {
            Log.d(TAG, "Nenhum usuário logado.")
            return Result.success()
        }

        return try {

            val resposta =
                NotificacaoRepository().listar()

            if (!resposta.isSuccessful) {

                Log.e(
                    TAG,
                    "Erro ao consultar notificações: ${resposta.code()}"
                )

                return if (resposta.code() >= 500) {
                    Result.retry()
                } else {
                    Result.success()
                }
            }

            val notificacoes =
                resposta.body().orEmpty()

            verificarNovasNotificacoes(
                usuarioId,
                notificacoes
            )

            Result.success()

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Erro no Worker de notificações.",
                e
            )

            Result.retry()
        }
    }

    private fun verificarNovasNotificacoes(
        usuarioId: String,
        notificacoes: List<Notificacao>
    ) {

        val preferencias =
            applicationContext.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val chaveInicializado =
            "inicializado_$usuarioId"

        val chaveIds =
            "ids_$usuarioId"

        val idsAtuais =
            notificacoes
                .map { it.id }
                .toSet()

        val inicializado =
            preferencias.getBoolean(
                chaveInicializado,
                false
            )

        if (!inicializado) {

            preferencias.edit()
                .putBoolean(
                    chaveInicializado,
                    true
                )
                .putStringSet(
                    chaveIds,
                    idsAtuais
                )
                .apply()

            Log.d(
                TAG,
                "Monitoramento de notificações inicializado."
            )

            return
        }

        val idsConhecidos =
            preferencias.getStringSet(
                chaveIds,
                emptySet()
            )
                ?.toSet()
                .orEmpty()

        val novas =
            notificacoes
                .filter {
                    it.id !in idsConhecidos &&
                            !it.lida
                }
                .reversed()

        novas.forEach { notificacao ->

            mostrarNotificacao(
                notificacao
            )
        }

        preferencias.edit()
            .putStringSet(
                chaveIds,
                idsAtuais
            )
            .apply()

        if (novas.isNotEmpty()) {

            Log.d(
                TAG,
                "${novas.size} nova(s) notificação(ões) exibida(s)."
            )
        }
    }

    private fun mostrarNotificacao(
        notificacao: Notificacao
    ) {

        criarCanalNotificacao()

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Log.d(
                TAG,
                "Permissão de notificações não concedida."
            )

            return
        }

        val intent =
            Intent(
                applicationContext,
                NotificationsActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val pendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(
                applicationContext,
                CHANNEL_ID
            )
                .setSmallIcon(
                    R.drawable.logo_oficial_branca_exportada
                )
                .setContentTitle(
                    notificacao.titulo
                )
                .setContentText(
                    notificacao.mensagem
                        ?: "Você possui uma nova notificação."
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            notificacao.mensagem
                                ?: notificacao.titulo
                        )
                )
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

        NotificationManagerCompat
            .from(applicationContext)
            .notify(
                notificacao.id.hashCode(),
                notification
            )
    }

    private fun criarCanalNotificacao() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val canal =
                NotificationChannel(
                    CHANNEL_ID,
                    "Notificações Zatts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {

                    description =
                        "Alertas e notificações do Zatts"
                }

            val manager =
                applicationContext.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            manager.createNotificationChannel(
                canal
            )
        }
    }

    companion object {

        private const val TAG =
            "ZATTS_WORKER"

        private const val PREFS_NAME =
            "zatts_notificacoes_worker"

        private const val CHANNEL_ID =
            "zatts_notificacoes"

        private const val WORK_NAME =
            "zatts_notificacoes_background"

        fun agendar(context: Context) {

            val restricoes =
                Constraints.Builder()
                    .setRequiredNetworkType(
                        NetworkType.CONNECTED
                    )
                    .build()

            val trabalho =
                PeriodicWorkRequestBuilder<NotificacaoWorker>(
                    15,
                    TimeUnit.MINUTES
                )
                    .setConstraints(restricoes)
                    .build()

            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    trabalho
                )

            Log.d(
                TAG,
                "Monitoramento em segundo plano agendado."
            )
        }
    }
}
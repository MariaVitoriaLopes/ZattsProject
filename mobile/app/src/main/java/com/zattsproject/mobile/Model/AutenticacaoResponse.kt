
package com.zattsproject.mobile.Model

import com.google.gson.annotations.SerializedName

data class AutenticacaoResponse(

    @SerializedName("mensagem")
    val mensagem: String? = null,

    @SerializedName("erro")
    val erro: String? = null,

    @SerializedName("usuarioId")
    val usuarioId: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("questionarioRespondido")
    val questionarioRespondido: Boolean? = null
)


package com.zattsproject.mobile.Network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import org.json.JSONObject

class PersistentCookieJar(context: Context) : CookieJar {

    private val preferences = context.getSharedPreferences(
        "zatts_cookies",
        Context.MODE_PRIVATE
    )

    override fun saveFromResponse(
        url: HttpUrl,
        cookies: List<Cookie>
    ) {

        val editor = preferences.edit()

        cookies.forEach { cookie ->

            val dados = JSONObject().apply {
                put("name", cookie.name)
                put("value", cookie.value)
                put("domain", cookie.domain)
                put("path", cookie.path)
                put("expiresAt", cookie.expiresAt)
                put("secure", cookie.secure)
                put("httpOnly", cookie.httpOnly)
                put("hostOnly", cookie.hostOnly)
            }

            val chave = "cookie_${cookie.domain}_${cookie.name}"

            editor.putString(chave, dados.toString())
        }

        editor.apply()
    }

    override fun loadForRequest(
        url: HttpUrl
    ): List<Cookie> {

        val cookiesValidos = mutableListOf<Cookie>()
        val editor = preferences.edit()

        preferences.all.forEach { (chave, valor) ->

            try {

                val dados = JSONObject(valor as String)

                val builder = Cookie.Builder()
                    .name(dados.getString("name"))
                    .value(dados.getString("value"))
                    .path(dados.getString("path"))
                    .expiresAt(dados.getLong("expiresAt"))

                if (dados.getBoolean("hostOnly")) {

                    builder.hostOnlyDomain(
                        dados.getString("domain")
                    )

                } else {

                    builder.domain(
                        dados.getString("domain")
                    )
                }

                if (dados.getBoolean("secure")) {
                    builder.secure()
                }

                if (dados.getBoolean("httpOnly")) {
                    builder.httpOnly()
                }

                val cookie = builder.build()

                if (cookie.expiresAt <= System.currentTimeMillis()) {

                    editor.remove(chave)

                } else if (cookie.matches(url)) {

                    cookiesValidos.add(cookie)
                }

            } catch (e: Exception) {

                editor.remove(chave)
            }
        }

        editor.apply()

        return cookiesValidos
    }

    fun limparCookies() {
        preferences.edit().clear().apply()
    }
}

package com.zattsproject.mobile.View

import com.zattsproject.mobile.R
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.transition.TransitionManager
import com.zattsproject.mobile.AlertsActivity
import com.zattsproject.mobile.HistoryActivity
import com.zattsproject.mobile.HomeActivity
import com.zattsproject.mobile.NotificationsActivity
import com.zattsproject.mobile.SettingsActivity

enum class NavTab(
    @DrawableRes val icon: Int,
    val label: String,
    val destination: Class<*>
) {
    HOME(R.drawable.home, "Home", HomeActivity::class.java),
    HISTORY(R.drawable.history, "Histórico", HistoryActivity::class.java),
    ALERTS(R.drawable.alert, "Alertas", AlertsActivity::class.java),
    NOTIFICATIONS(R.drawable.notifications, "Notificações", NotificationsActivity::class.java),
    SETTINGS(R.drawable.settings, "Configurações", SettingsActivity::class.java)
}

class AppNavBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private class ItemViews(
        val root: View,
        val icon: ImageView,
        val label: TextView,
        val dot: View
    )

    private val items = mutableMapOf<NavTab, ItemViews>()
    private var selectedTab: NavTab = NavTab.HOME
    private var showNotificationDot = false

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        val inflater = LayoutInflater.from(context)
        NavTab.values().forEach { tab ->
            val view = inflater.inflate(R.layout.item_nav, this, false)
            val item = ItemViews(
                root = view,
                icon = view.findViewById(R.id.navIcon),
                label = view.findViewById(R.id.navLabel),
                dot = view.findViewById(R.id.navDot)
            )
            item.icon.setImageResource(tab.icon)
            item.label.text = tab.label
            view.contentDescription = tab.label
            view.setOnClickListener { navigateTo(tab) }

            items[tab] = item
            addView(view)
        }
        render(animate = false)
    }

    private fun navigateTo(tab: NavTab) {
        // Já está nessa tela, não faz nada
        if (tab == selectedTab) return

        val intent = Intent(context, tab.destination).apply {
            // Evita empilhar a mesma tela várias vezes
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        context.startActivity(intent)
    }

    fun setSelected(tab: NavTab, animate: Boolean = false) {
        selectedTab = tab
        render(animate)
    }

    fun setNotificationDot(visible: Boolean) {
        showNotificationDot = visible
        render(animate = false)
    }

    private fun render(animate: Boolean) {
        if (animate) TransitionManager.beginDelayedTransition(this)

        items.forEach { (tab, item) ->
            val selected = tab == selectedTab

            item.root.setBackgroundResource(if (selected) R.drawable.bg_nav_selected else 0)
            item.label.visibility = if (selected) View.VISIBLE else View.GONE
            item.dot.visibility =
                if (tab == NavTab.NOTIFICATIONS && showNotificationDot) View.VISIBLE else View.GONE

            item.root.layoutParams = LayoutParams(
                if (selected) LayoutParams.WRAP_CONTENT else 0,
                LayoutParams.MATCH_PARENT,
                if (selected) 0f else 1f
            )
        }
    }
}
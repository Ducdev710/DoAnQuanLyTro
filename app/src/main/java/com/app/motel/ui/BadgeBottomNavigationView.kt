package com.app.motel.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.app.motel.R
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView

class BadgeBottomNavigationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    private val badgeMap = HashMap<Int, TextView>()

    fun addBadgeAt(itemId: Int, count: Int) {
        // Remove any existing badge first
        removeBadge(itemId)

        // Get the item view directly by ID
        val itemView = findViewById<BottomNavigationItemView>(itemId) ?: return

        // Create a new badge
        val badge = TextView(context).apply {
            text = count.toString()
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            textSize = 10f
            background = ContextCompat.getDrawable(context, R.drawable.notification_badge)
        }

        // Add the badge to the view
        val params = FrameLayout.LayoutParams(
            context.resources.getDimensionPixelSize(R.dimen.badge_size),
            context.resources.getDimensionPixelSize(R.dimen.badge_size)
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            topMargin = context.resources.getDimensionPixelSize(R.dimen.badge_top_margin)
            marginEnd = context.resources.getDimensionPixelSize(R.dimen.badge_right_margin)
        }

        itemView.addView(badge, params)
        badgeMap[itemId] = badge

        Log.d("BadgeNav", "Added badge to item with ID: $itemId")
    }

    override fun removeBadge(itemId: Int) {
        val badge = badgeMap[itemId] ?: return
        val itemView = findViewById<BottomNavigationItemView>(itemId) ?: return

        itemView.removeView(badge)
        badgeMap.remove(itemId)

        Log.d("BadgeNav", "Removed badge from item with ID: $itemId")
    }

    fun clearAllBadges() {
        val ids = ArrayList(badgeMap.keys)
        for (id in ids) {
            removeBadge(id)
        }
    }
}
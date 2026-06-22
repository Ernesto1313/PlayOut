package com.ernesto.playout.ui.utils

import com.ernesto.playout.R

fun categoryDrawable(categoria: String?): Int = when (categoria) {
    "Chess" -> R.drawable.chess
    "Ping-Pong", "Pingpong", "PingPong", "ping-pong" -> R.drawable.pingpong
    "Football" -> R.drawable.football
    "Fencing" -> R.drawable.fencing
    "Skating" -> R.drawable.skate
    "Volleyball" -> R.drawable.volleyball
    "Basketball" -> R.drawable.basketball
    "Calisthenics" -> R.drawable.calisthenics
    "Athletics" -> R.drawable.atletism
    "Minigolf", "Mini Golf" -> R.drawable.minigolf
    "Pétanque" -> R.drawable.petanque
    "Other", "Otro", "other" -> R.drawable.other
    else -> R.drawable.other
}

fun formatDistance(meters: Float): String = when {
    meters < 1000 -> "${meters.toInt()} m"
    else -> "${"%.1f".format(meters / 1000)} km"
}

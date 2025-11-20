package com.example.itrainer.utils

import com.example.itrainer.R

object CategoryColors {
    fun getCategoryColor(categoryName: String?): Int {
        return when (categoryName?.lowercase()) {
            "minibasket" -> R.color.category_minibasket
            "benjamín" -> R.color.category_benjamin
            "infantil y preinf 2ª" -> R.color.category_infantil
            "cadete" -> R.color.category_cadete
            else -> R.color.category_default
        }
    }

    fun getCategoryIcon(categoryName: String?): String {
        return when (categoryName?.lowercase()) {
            "minibasket" -> "🏀"
            "benjamín" -> "⛹️"
            "infantil y preinf 2ª" -> "🏆"
            "cadete" -> "🥇"
            else -> "🔵"
        }
    }
}
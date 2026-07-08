package com.ernesto.playout.data.util

import com.ernesto.playout.data.model.Review

object RatingCalculator {

    private const val DAY_MS = 86_400_000.0

    // Star weight by age in days relative to most recent review
    private fun starWeight(ageDays: Double): Double {
        // today 1.0, 6mo 0.75, 1y 0.5, 2y 0.25, 3y 0.1
        return when {
            ageDays <= 0 -> 1.0
            ageDays >= 1095 -> 0.1
            ageDays <= 180 -> 1.0 - (0.25 * (ageDays / 180))
            ageDays <= 365 -> 0.75 - (0.25 * ((ageDays - 180) / 185))
            ageDays <= 730 -> 0.5 - (0.25 * ((ageDays - 365) / 365))
            else -> 0.25 - (0.15 * ((ageDays - 730) / 365))
        }
    }

    // Condition weight decays faster
    private fun conditionWeight(ageDays: Double): Double {
        // today 1.0, 3mo 0.75, 6mo 0.5, 1y 0.25, 2y 0.1, 3y 0.05
        return when {
            ageDays <= 0 -> 1.0
            ageDays >= 1095 -> 0.05
            ageDays <= 90 -> 1.0 - (0.25 * (ageDays / 90))
            ageDays <= 180 -> 0.75 - (0.25 * ((ageDays - 90) / 90))
            ageDays <= 365 -> 0.5 - (0.25 * ((ageDays - 180) / 185))
            ageDays <= 730 -> 0.25 - (0.15 * ((ageDays - 365) / 365))
            else -> 0.1 - (0.05 * ((ageDays - 730) / 365))
        }
    }

    data class RatingResult(
        val avgStars: Double,
        val avgCondition: Int,  // rounded to 1, 2 or 3
        val reviewCount: Int
    )

    fun calculate(reviews: List<Review>): RatingResult {
        if (reviews.isEmpty()) {
            return RatingResult(0.0, 0, 0)
        }
        // Most recent timestamp is the reference point (relative decay)
        val mostRecent = reviews.maxOf { it.timestamp }

        var starWeightedSum = 0.0
        var starWeightTotal = 0.0
        var condWeightedSum = 0.0
        var condWeightTotal = 0.0

        reviews.forEach { review ->
            val ageDays = (mostRecent - review.timestamp) / DAY_MS
            val sw = starWeight(ageDays)
            val cw = conditionWeight(ageDays)
            starWeightedSum += review.stars * sw
            starWeightTotal += sw
            condWeightedSum += review.condition * cw
            condWeightTotal += cw
        }

        val avgStars = if (starWeightTotal > 0) starWeightedSum / starWeightTotal else 0.0
        val avgCondRaw = if (condWeightTotal > 0) condWeightedSum / condWeightTotal else 0.0
        val avgCondition = avgCondRaw.let {
            when {
                it <= 0 -> 0
                it < 1.5 -> 1
                it < 2.5 -> 2
                else -> 3
            }
        }

        return RatingResult(avgStars, avgCondition, reviews.size)
    }
}

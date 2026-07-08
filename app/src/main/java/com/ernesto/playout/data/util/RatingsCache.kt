package com.ernesto.playout.data.util

import com.ernesto.playout.data.model.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object RatingsCache {
    private val _ratings = MutableStateFlow<Map<Int, RatingCalculator.RatingResult>>(emptyMap())
    val ratings: StateFlow<Map<Int, RatingCalculator.RatingResult>> = _ratings

    fun update(reviews: List<Review>) {
        val grouped = reviews.groupBy { it.facilityFid }
        val computed = grouped.mapValues { (_, revs) ->
            RatingCalculator.calculate(revs)
        }
        _ratings.value = computed
    }

    fun getRating(fid: Int): RatingCalculator.RatingResult? {
        return _ratings.value[fid]
    }
}

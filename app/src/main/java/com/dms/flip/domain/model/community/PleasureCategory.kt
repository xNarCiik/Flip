package com.dms.flip.domain.model.community

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
enum class PleasureCategory: Parcelable {
    ALL,
    FOOD,
    ENTERTAINMENT,
    SOCIAL,
    WELLNESS,
    CREATIVE,
    OUTDOOR,
    SPORT,
    SHOPPING,
    CULTURE,
    LEARNING,
    OTHER
}

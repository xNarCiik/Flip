package com.dms.flip.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.dms.flip.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getTodayDayIdentifier(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

@Composable
fun formatRequestTime(timestamp: Long): String {
    val days = ((System.currentTimeMillis() - timestamp) / (1_000 * 60 * 60 * 24)).toInt()
    return when (days) {
        0 -> stringResource(id = R.string.community_request_time_today)
        1 -> stringResource(id = R.string.community_request_time_yesterday)
        else -> pluralStringResource(
            id = R.plurals.community_request_time_days,
            count = days,
            days
        )
    }
}

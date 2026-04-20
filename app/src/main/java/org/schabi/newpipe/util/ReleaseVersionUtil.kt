package org.schabi.newpipe.util

import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object ReleaseVersionUtil {
    private const val MIN_UPDATE_CHECK_SECONDS = 6 * 60 * 60L
    private const val MAX_UPDATE_CHECK_SECONDS = 72 * 60 * 60L

    @JvmStatic
    fun isLastUpdateCheckExpired(expiryEpochSeconds: Long): Boolean {
        return Instant.now().epochSecond >= expiryEpochSeconds
    }

    @JvmStatic
    fun coerceUpdateCheckExpiry(expiryHeaderValue: String?): Long {
        val now = Instant.now().epochSecond
        val minAllowed = now + MIN_UPDATE_CHECK_SECONDS
        val maxAllowed = now + MAX_UPDATE_CHECK_SECONDS

        val parsedExpiry = expiryHeaderValue
            ?.takeIf { it.isNotBlank() }
            ?.let {
                runCatching {
                    ZonedDateTime.parse(it, DateTimeFormatter.RFC_1123_DATE_TIME).toEpochSecond()
                }.getOrNull()
            }

        return when {
            parsedExpiry == null -> minAllowed
            parsedExpiry < minAllowed -> minAllowed
            parsedExpiry > maxAllowed -> maxAllowed
            else -> parsedExpiry
        }
    }
}

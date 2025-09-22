package it.unibo.jakta.scheduler.server

import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Utils {
    fun formatTimestamp(timestamp: Long?): String {
        if (timestamp == null) return "N/A"
        val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    fun generateRunHashWithJobId(
        jobId: String,
        parameters: Map<String, String>,
    ): String {
        val sortedParams = parameters.toSortedMap()
        val paramString = sortedParams.entries.joinToString("|") { "${it.key}=${it.value}" }
        val combined = "$jobId::$paramString"

        return MessageDigest
            .getInstance("SHA-256")
            .digest(combined.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(16)
    }
}

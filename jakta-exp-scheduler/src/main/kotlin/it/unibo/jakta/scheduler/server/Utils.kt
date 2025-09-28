package it.unibo.jakta.scheduler.server

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors
import kotlin.use

object Utils {
    fun formatTimestamp(timestamp: Long?): String {
        if (timestamp == null) return "N/A"
        val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    fun generateRunHashWithJobId(
        jobName: String,
        params: Map<String, String>,
    ): String {
        val sortedParams = params.toSortedMap()
        val paramString =
            "$jobName&" +
                sortedParams.entries.joinToString("&") {
                    "${it.key}=${it.value}"
                }
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(paramString.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(32)
    }

    fun loadCache(cacheDir: String): Set<String> =
        try {
            val experimentsPath = Paths.get(cacheDir)
            if (!Files.exists(experimentsPath)) {
                println("Experiments directory '$cacheDir' does not exist, creating it...")
                Files.createDirectories(experimentsPath)
                emptySet()
            } else {
                fetchDirectories(experimentsPath).also { hashes ->
                    println("Found ${hashes.size} existing experiment directories in '$cacheDir'")
                }
            }
        } catch (e: Exception) {
            println("Error loading existing hashes: ${e.message}")
            emptySet()
        }

    fun fetchDirectories(path: Path): Set<String> =
        Files
            .list(path)
            .use { stream ->
                stream
                    .filter { Files.isDirectory(it) }
                    .map { it.fileName.toString() }
                    .collect(Collectors.toSet())
            }
}

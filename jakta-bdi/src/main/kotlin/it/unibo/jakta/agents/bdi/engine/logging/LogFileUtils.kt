package it.unibo.jakta.agents.bdi.engine.logging

import it.unibo.jakta.agents.bdi.engine.FileUtils.processLog
import it.unibo.jakta.agents.bdi.engine.logging.events.LogEventContext
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.layout.template.json.util.JsonWriter
import org.apache.logging.log4j.message.ObjectMessage
import java.io.File
import java.net.URI

object LogFileUtils {
    fun findMasLogFiles(expDir: String): Map<File, String> =
        File(expDir)
            .listFiles { f -> f.extension == LOG_FILE_EXTENSION }
            .mapNotNull { f ->
                var id: String? = null
                processLog(f) { logEntry ->
                    id = logEntry.logLogger
                    false // just process the first line and then stop
                }
                id
                    ?.takeIf { it.matches(logFileRegex) && extractLastComponent(it) == "Mas" }
                    ?.let { f to it }
            }.toMap()

    fun extractAgentLogFiles(
        expDir: String,
        masId: String,
    ): Map<File, String> =
        File(expDir)
            .listFiles { f -> f.extension == LOG_FILE_EXTENSION }
            .mapNotNull { f ->
                var id: String? = null
                processLog(f) { logEntry ->
                    id = logEntry.logLogger
                    false // just process the first line and then stop
                }
                id
                    ?.takeIf { it.matches(logFileRegex) && countUuids(it) == 2 && it.startsWith(masId) }
                    ?.let { f to it }
            }.toMap()

    fun countUuids(filename: String): Int {
        val pattern = Regex(UUID_PATTERN)
        return pattern.findAll(filename).count()
    }

    const val LOG_FILE_EXTENSION = "jsonl"
    const val UUID_PATTERN = "[a-f0-9]{8}(?:-[a-f0-9]{4}){3}-[a-f0-9]{12}"
    private const val COMPONENT_PATTERN = "([^-]+)"

    // Unified pattern that captures all components between UUIDs
    const val LOG_FILE_PATTERN =
        "^$COMPONENT_PATTERN(?:-$UUID_PATTERN(?:-$COMPONENT_PATTERN))*(?:-$UUID_PATTERN)?(?:\\.$LOG_FILE_EXTENSION)?$"

    val logFileRegex = Regex(LOG_FILE_PATTERN)

    fun extractLastId(fullName: String): String? {
        val regex = Regex("($UUID_PATTERN)(?=(?:[^-]*(?:\\.$LOG_FILE_EXTENSION)?)?$)")
        return regex
            .find(fullName)
            ?.groups
            ?.get(1)
            ?.value
    }

    @JvmStatic
    fun extractLastComponent(fullName: String): String =
        logFileRegex.find(fullName)?.let { matchResult ->
            matchResult.groups
                .filterNotNull()
                .drop(1) // Skip the full match (group 0)
                .lastOrNull { it.value != fullName }
                ?.value
        } ?: fullName

    @JvmStatic
    fun resolveObjectMessage(message: ObjectMessage): String =
        when (val param = message.parameter) {
            is LogEventContext -> param.event.description ?: ""
            else -> param.toString()
        }

    @JvmStatic
    fun resolveObjectMessage(
        json: Json,
        message: ObjectMessage,
        jsonWriter: JsonWriter,
    ) {
        val param = message.parameter

        if (param == null) {
            jsonWriter.writeNull()
            return
        }

        when (param) {
            is LogEventContext -> {
                try {
                    val jsonString = json.encodeToString(LogEventContext.serializer(), param)
                    jsonWriter.writeRawString(jsonString)
                } catch (_: Exception) {
                    jsonWriter.writeString(param.toString())
                }
            }

            else -> jsonWriter.writeString(param.toString())
        }
    }

    fun extractHostnameAndPort(urlString: String): Pair<String, Int?> =
        try {
            val url = URI(urlString)
            val hostname = url.host
            val port = if (url.port == -1) null else url.port
            Pair(hostname, port)
        } catch (e: Exception) {
            println("Invalid URL: ${e.message}")
            Pair("", null)
        }
}

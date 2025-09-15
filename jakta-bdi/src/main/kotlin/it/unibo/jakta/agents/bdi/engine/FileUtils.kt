package it.unibo.jakta.agents.bdi.engine

import it.unibo.jakta.agents.bdi.engine.logging.LogEntry
import it.unibo.jakta.agents.bdi.engine.logging.loggers.JaktaLogger
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JaktaJsonComponent
import java.io.BufferedReader
import java.io.File
import java.io.IOException

object FileUtils {
    fun getResourceAsFile(path: String): File? =
        this.javaClass
            .getResource(path)
            ?.path
            ?.let { File(it) }

    fun writeToFile(
        content: String,
        file: File,
    ) = try {
        file.writeText(content)
    } catch (e: IOException) {
        println("Error writing to file: ${e.message}")
    }

    private inline fun <reified T> logLineProcessor(
        reader: BufferedReader,
        logger: JaktaLogger? = null,
        processFunction: (T) -> Boolean,
    ): Boolean {
        var lineCount = 0
        var errorCount = 0
        var shouldContinue = true

        reader.useLines { lines ->
            for (line in lines) {
                if (!shouldContinue) break

                lineCount++
                val logEntry =
                    try {
                        JaktaJsonComponent.json.decodeFromString<T>(line)
                    } catch (e: Exception) {
                        errorCount++
                        logger?.warn { "Could not parse line $lineCount: ${e.message} as a ${T::class.simpleName}." }
                        null
                    }
                logEntry?.let {
                    shouldContinue = processFunction(it)
                }
            }
        }
        logger?.info { "Processing complete. Total entries: $lineCount, Events not parseable: $errorCount" }
        return shouldContinue
    }

    fun processLog(
        file: File,
        logger: JaktaLogger? = null,
        processFunction: (LogEntry) -> Boolean,
    ) {
        val reader = file.bufferedReader()
        logLineProcessor(reader, logger, processFunction)
    }
}

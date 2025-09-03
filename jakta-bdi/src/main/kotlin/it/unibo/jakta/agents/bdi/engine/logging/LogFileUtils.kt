package it.unibo.jakta.agents.bdi.engine.logging

import it.unibo.jakta.agents.bdi.engine.logging.loggers.JaktaLogger
import java.io.File

object LogFileUtils {
    fun findMasLogFiles(expDir: String): List<File> =
        File(expDir)
            .listFiles { file ->
                file.name.matches(JaktaLogger.logFileRegex) &&
                    JaktaLogger.extractLastComponent(file.name) == "Mas"
            }?.toList() ?: emptyList()

    fun extractAgentLogFiles(
        expDir: String,
        masLogFile: File,
    ): List<File> {
        val masId = JaktaLogger.extractLastId(masLogFile.name) ?: return emptyList()
        return File(expDir)
            .listFiles { file ->
                file.name.matches(JaktaLogger.logFileRegex) &&
                    countUuids(file.name) == 2 &&
                    file.name.startsWith("Mas-$masId-")
            }?.toList() ?: emptyList()
    }

    fun countUuids(filename: String): Int {
        val pattern = Regex(JaktaLogger.UUID_PATTERN)
        return pattern.findAll(filename).count()
    }
}

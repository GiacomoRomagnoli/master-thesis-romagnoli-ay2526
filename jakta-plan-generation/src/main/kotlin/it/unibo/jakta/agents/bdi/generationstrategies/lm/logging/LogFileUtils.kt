package it.unibo.jakta.agents.bdi.generationstrategies.lm.logging

import it.unibo.jakta.agents.bdi.engine.logging.LogFileUtils.countUuids
import it.unibo.jakta.agents.bdi.engine.logging.loggers.JaktaLogger
import java.io.File

object LogFileUtils {
    fun extractPgpLogFiles(
        expDir: String,
        agentLogFile: File,
    ): List<File> =
        File(expDir)
            .listFiles { file ->
                file.name.matches(JaktaLogger.logFileRegex) &&
                    countUuids(file.name) == 3 &&
                    file.name.startsWith(agentLogFile.name.substringBeforeLast("-"))
            }?.toList() ?: emptyList()
}

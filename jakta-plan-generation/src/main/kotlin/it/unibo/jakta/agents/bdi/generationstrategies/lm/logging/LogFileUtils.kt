package it.unibo.jakta.agents.bdi.generationstrategies.lm.logging

import it.unibo.jakta.agents.bdi.engine.FileUtils.processLog
import it.unibo.jakta.agents.bdi.engine.logging.LogFileUtils.LOG_FILE_EXTENSION
import it.unibo.jakta.agents.bdi.engine.logging.LogFileUtils.countUuids
import it.unibo.jakta.agents.bdi.engine.logging.LogFileUtils.logFileRegex
import java.io.File

object LogFileUtils {
    fun extractPgpLogFiles(
        expDir: String,
        agentId: String,
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
                    ?.takeIf {
                        id.matches(logFileRegex) &&
                            countUuids(id) == 3 &&
                            id.startsWith(agentId.substringBeforeLast("-"))
                    }?.let { f to it }
            }.toMap()
}

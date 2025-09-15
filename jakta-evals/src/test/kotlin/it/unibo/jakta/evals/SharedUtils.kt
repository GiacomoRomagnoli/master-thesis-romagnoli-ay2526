package it.unibo.jakta.evals

import it.unibo.jakta.agents.bdi.engine.FileUtils.getResourceAsFile

object SharedUtils {
    val testMasLogFile =
        getResourceAsFile("/testLogs/Mas.jsonl")
            ?: error("Could not find mas test log file")
    val testAgentLogFile =
        getResourceAsFile(
            "/testLogs/ExplorerRobot.jsonl",
        )
            ?: error("Could not find agent test log file")
    val testPgpLogFile =
        getResourceAsFile(
            "/testLogs/Pgp_admiring_bell.jsonl",
        )
            ?: error("Could not find pgp test log file")

    const val PGP_ID = "42ec329a-7bf8-4359-9dec-4e745bd6beec"
}

package it.unibo.jakta.scheduler.server.domain

data class CommandResult(
    val exitCode: Int,
    val output: String?,
    val error: String?,
)

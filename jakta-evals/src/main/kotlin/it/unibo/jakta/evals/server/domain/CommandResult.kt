package it.unibo.jakta.evals.server.domain

data class CommandResult(
    val exitCode: Int,
    val output: String?,
    val error: String?,
)

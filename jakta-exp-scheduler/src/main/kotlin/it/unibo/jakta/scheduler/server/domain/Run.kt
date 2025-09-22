package it.unibo.jakta.scheduler.server.domain

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Run(
    val id: String = UUID.randomUUID().toString(),
    val jobId: String,
    val parameters: Map<String, String>,
    val command: List<String>,
    val status: RunStatus = RunStatus.PENDING,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val exitCode: Int? = null,
    val output: String? = null,
    val error: String? = null,
)

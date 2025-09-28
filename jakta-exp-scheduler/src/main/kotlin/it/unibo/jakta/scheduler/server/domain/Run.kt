package it.unibo.jakta.scheduler.server.domain

import kotlinx.serialization.Serializable

@Serializable
data class Run(
    val id: String,
    val jobId: String,
    val parameters: Map<String, String>,
    val command: List<String>,
    val status: RunStatus = RunStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val exitCode: Int? = null,
    val output: String? = null,
    val error: String? = null,
)

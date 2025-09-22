package it.unibo.jakta.scheduler.server.domain

import kotlinx.serialization.Serializable

@Serializable
data class JobResponse(
    val id: String,
    val name: String,
    val status: JobStatus,
    val createdAt: Long,
    val totalRuns: Int,
    val pendingRuns: Int,
    val runningRuns: Int,
    val completedRuns: Int,
    val failedRuns: Int,
    val cancelledRuns: Int,
)

package it.unibo.jakta.evals.server.domain

import kotlinx.serialization.Serializable

@Serializable
data class ExperimentResponse(
    val id: String,
    val name: String,
    val status: ExperimentStatus,
    val createdAt: Long,
    val totalRuns: Int,
    val pendingRuns: Int,
    val runningRuns: Int,
    val completedRuns: Int,
    val failedRuns: Int,
    val cancelledRuns: Int,
)

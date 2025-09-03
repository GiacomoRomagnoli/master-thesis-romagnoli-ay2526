package it.unibo.jakta.evals.server.domain

import kotlinx.serialization.Serializable

@Serializable
data class ExperimentRequest(
    val name: String,
    val commandTemplate: List<String>,
    val parameters: Map<String, List<String>>,
    val maxParallel: Int = 1,
)

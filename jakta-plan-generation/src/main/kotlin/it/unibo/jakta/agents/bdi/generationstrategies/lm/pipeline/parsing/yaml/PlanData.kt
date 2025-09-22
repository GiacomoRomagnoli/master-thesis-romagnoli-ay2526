package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.yaml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlanData(
    @SerialName("EVENT") val event: String,
    @SerialName("CONDITIONS") val conditions: List<String>,
    @SerialName("OPERATIONS") val operations: List<String>,
)

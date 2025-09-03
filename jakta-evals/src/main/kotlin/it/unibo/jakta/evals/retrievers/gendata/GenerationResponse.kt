package it.unibo.jakta.evals.retrievers.gendata

import kotlinx.serialization.Serializable

@Serializable
data class GenerationResponse(
    val data: GenerationData,
)

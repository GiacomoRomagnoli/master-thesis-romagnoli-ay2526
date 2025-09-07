package it.unibo.jakta.evals.retrievers.plandata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PlanData")
data class PlanData(
    val invocationContext: InvocationContext,
    val pgpInvocation: PGPInvocation,
)

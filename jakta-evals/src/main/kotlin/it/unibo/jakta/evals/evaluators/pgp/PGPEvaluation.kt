package it.unibo.jakta.evals.evaluators.pgp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PGPEvaluationResult")
data class PGPEvaluation(
    val amountGeneratedPlans: Int,
    val averageAmountBeliefs: Double,
    val averageAmountOperations: Double,
    val amountGeneralPlan: Int,
    val amountUselessPlans: Int,
    val amountInadequateUsageGoals: Int,
    val amountInadequateUsageBeliefs: Int,
    val amountInadequateUsageActions: Int,
)

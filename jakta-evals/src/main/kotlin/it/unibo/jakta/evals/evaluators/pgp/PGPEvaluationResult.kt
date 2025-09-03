package it.unibo.jakta.evals.evaluators.pgp

import it.unibo.jakta.agents.bdi.engine.plans.Plan
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PGPEvaluationResult")
data class PGPEvaluationResult(
    val masId: String?,
    val agentId: String?,
    val pgpId: String,
    val parsedPlans: List<Plan>,
    val amountGeneratedPlans: Int,
    val averageAmountBeliefs: Double,
    val averageAmountOperations: Double,
    val amountGeneralPlan: Int,
    val amountInventedGoals: Int,
    val amountInventedBeliefs: Int,
    val amountUselessPlans: Int,
    val amountNotParseablePlans: Int,
    val amountInadequateUsageGoals: Int,
    val amountInadequateUsageBeliefs: Int,
    val amountInadequateUsageActions: Int,
    val timeUntilCompletion: Long? = null,
    val executable: Boolean,
    val achievesGoal: Boolean,
    val generationConfig: LMGenerationConfig? = null,
)

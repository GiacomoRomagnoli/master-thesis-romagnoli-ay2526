package it.unibo.jakta.evals.evaluators.run

import it.unibo.jakta.evals.evaluators.path.PathEvaluation
import it.unibo.jakta.evals.evaluators.pgp.PGPEvaluation
import it.unibo.jakta.evals.retrievers.gendata.GenerationData
import it.unibo.jakta.evals.retrievers.plandata.PlanData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("RunEvaluation")
data class RunEvaluation(
    val runId: String,
    val pathEvaluation: PathEvaluation? = null,
    val pgpEvaluation: PGPEvaluation? = null,
    val planData: PlanData? = null,
    val generationData: GenerationData? = null,
)

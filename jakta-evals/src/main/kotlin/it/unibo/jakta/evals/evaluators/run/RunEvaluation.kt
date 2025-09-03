package it.unibo.jakta.evals.evaluators.run

import it.unibo.jakta.evals.evaluators.path.PathEvaluationResult
import it.unibo.jakta.evals.evaluators.pgp.PGPEvaluationResult
import it.unibo.jakta.evals.retrievers.gendata.GenerationData
import it.unibo.jakta.evals.retrievers.plandata.PlanData

data class RunEvaluation(
    val pathEvaluation: PathEvaluationResult,
    val pgpEvaluation: PGPEvaluationResult,
    val planData: PlanData,
    val generationData: GenerationData,
)

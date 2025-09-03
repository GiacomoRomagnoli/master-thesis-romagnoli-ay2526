package it.unibo.jakta.evals.evaluators.run

import it.unibo.jakta.evals.evaluators.Evaluator
import it.unibo.jakta.evals.retrievers.plandata.PGPInvocation

class RunEvaluator(
    authToken: String,
    val retrieveGenerationData: Boolean = false,
    val run: PGPInvocation,
) : Evaluator<RunEvaluation> {
//    private val genDataClient = GenerationClient(authToken)
//    private val chatCompletionId = run.chatCompletionId
//    private val planDataRetriever = PlanDataRetriever()
//    private val pgpEvaluator = PGPEvaluator()
//    private val pathEvaluator = PathEvaluator()

    override fun eval(): RunEvaluation {
//        val context = planDataRetriever.retrieve()
//
//        val generationData = run {
//
//            if (retrieveGenerationData && chatCompletionId != null) {
//                val genDataRetriever = GenerationDataRetriever(genDataClient, chatCompletionId)
//                genDataRetriever.retrieve()
//            } else {
//                null
//            }
//        }
//
//        val pgpData = pgpEvaluator.eval(context, *invocations)
//
//        val pathData = pathEvaluator.eval()

//        return RunEvaluation()
        TODO()
    }
}

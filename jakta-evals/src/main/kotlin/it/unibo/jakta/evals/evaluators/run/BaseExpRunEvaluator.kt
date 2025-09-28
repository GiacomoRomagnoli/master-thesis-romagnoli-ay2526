package it.unibo.jakta.evals.evaluators.run

import it.unibo.jakta.agents.bdi.engine.logging.LogFileUtils.extractAgentLogFiles
import it.unibo.jakta.agents.bdi.engine.logging.LogFileUtils.findMasLogFiles
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.LogFileUtils.extractPgpLogFiles
import it.unibo.jakta.evals.evaluators.Evaluator
import it.unibo.jakta.evals.evaluators.pgp.PGPEvaluator
import it.unibo.jakta.evals.retrievers.gendata.GenerationClient
import it.unibo.jakta.evals.retrievers.gendata.GenerationDataRetriever
import it.unibo.jakta.evals.retrievers.plandata.BaseExpPGPDataRetriever
import java.io.File

// TODO remove code duplication
// CPD-OFF
class BaseExpRunEvaluator(
    val expDir: String,
    val retrieveGenerationData: Boolean = false,
    val authToken: String? = null,
) : Evaluator<List<RunEvaluation>> {
    override fun eval(): List<RunEvaluation> {
        val masLogFiles = findMasLogFiles(expDir)?.ifEmpty { return emptyList() }
        val runEvaluations = mutableListOf<RunEvaluation>()

        masLogFiles?.forEach { (masLogFile, masId) ->
            println("Found $masLogFile")
            extractAgentLogFiles(expDir, masId).forEach { (agentLogFile, agentId) ->
                println("Found $agentLogFile")
                extractPgpLogFiles(expDir, agentId)?.forEach { (pgpLogFile, pgpId) ->
                    println("Found $pgpLogFile")
                    val planDataRetriever = BaseExpPGPDataRetriever(masLogFile, agentLogFile, pgpLogFile, pgpId)
                    val planData = planDataRetriever.retrieve()
                    val pgpEvaluator = PGPEvaluator(planData.invocationContext, planData.pgpInvocation)
                    val pgpEvaluation = pgpEvaluator.eval()

                    val chatCompletionId = planData.pgpInvocation.chatCompletionId
                    val generationData =
                        if (retrieveGenerationData && authToken != null && chatCompletionId != null) {
                            val genDataClient = GenerationClient(authToken)
                            val genDataRetriever = GenerationDataRetriever(genDataClient, chatCompletionId)
                            genDataRetriever.retrieve()
                        } else {
                            null
                        }

                    val runId = File(expDir).name
                    runEvaluations +=
                        RunEvaluation(
                            runId = runId,
                            planData = planData,
                            pgpEvaluation = pgpEvaluation,
                            generationData = generationData,
                        )
                }
            }
        }

        return runEvaluations
    }
}
// CPD-ON

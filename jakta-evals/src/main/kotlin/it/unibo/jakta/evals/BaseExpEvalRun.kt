package it.unibo.jakta.evals

import com.github.ajalt.clikt.core.main
import it.unibo.jakta.evals.evaluators.run.BaseExpRunEvaluator
import java.io.File

// TODO remove code duplication
// CPD-OFF
class BaseExpEvalRun : AbstractEvalRun() {
    override fun run() {
        val runEvaluator = BaseExpRunEvaluator(runDir, retrieveGenerationData, authToken)
        val result =
            runEvaluator.eval().also {
                it.ifEmpty { println("No PGPs found at $runDir") }
            }
        result.forEach { res ->
            val metricsDirectory = File("${metricsDir}${res.runId}").apply { mkdirs() }
            println("Writing results to $metricsDirectory")
            writeEvaluationResult(metricsDirectory, res, "evaluation_result")
            res.planData?.pgpInvocation?.let { writeGenerationResult(metricsDirectory, it, "generation_result") }
            res.planData
                ?.pgpInvocation
                ?.history
                ?.let { writeChatHistory(metricsDirectory, it, "chat_history") }
        }
    }
}
// CPD-ON

fun main(args: Array<String>) = BaseExpEvalRun().main(args)

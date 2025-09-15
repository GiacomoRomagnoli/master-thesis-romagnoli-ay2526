package it.unibo.jakta.evals

import com.github.ajalt.clikt.core.main
import it.unibo.jakta.evals.evaluators.run.AblationRunEvaluator
import java.io.File

class AblationEvalRun : EvalRun() {
    override fun run() {
        val runEvaluator = AblationRunEvaluator(runDir, retrieveGenerationData, authToken)
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

fun main(args: Array<String>) = AblationEvalRun().main(args)

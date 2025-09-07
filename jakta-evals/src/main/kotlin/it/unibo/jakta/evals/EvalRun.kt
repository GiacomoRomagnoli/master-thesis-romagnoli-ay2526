package it.unibo.jakta.evals

import com.aallam.openai.api.chat.ChatMessage
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import it.unibo.jakta.agents.bdi.engine.FileUtils.writeToFile
import it.unibo.jakta.agents.bdi.engine.Jakta.capitalize
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.planFormatter
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JaktaJsonComponent
import it.unibo.jakta.evals.evaluators.run.RunEvaluation
import it.unibo.jakta.evals.evaluators.run.RunEvaluator
import it.unibo.jakta.evals.retrievers.plandata.PGPInvocation
import it.unibo.jakta.exp.explorer.ModuleLoader
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.encodeToStream
import java.io.File

/**
 * Command-line tool for evaluating the results of the experiments.
 */
@OptIn(ExperimentalSerializationApi::class)
class EvalRun : CliktCommand() {
    val expDir: String by option()
        .default(DEFAULT_EXP_DIR)
        .help("The directory where the experiments' traces are stored.")

    val retrieveGenerationData: Boolean by option()
        .flag()
        .help("Whether to retrieve generation data from OpenRouter or not.")

    val metricsDir: String by option()
        .default(DEFAULT_METRICS_DIR)
        .help("The directory where the result of the evaluation is stored.")

    val authToken: String by option(envvar = "API_KEY")
        .default(DEFAULT_TOKEN)
        .help("The secret API key to use for authentication with the server")

    init {
        ModuleLoader.loadModules()
    }

    override fun run() {
        val runEvaluator = RunEvaluator(expDir, retrieveGenerationData, authToken)
        val result =
            runEvaluator.eval().also {
                it.ifEmpty { println("Nothing found at $expDir") }
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

    companion object {
        const val DEFAULT_EXP_DIR = "experiments/"
        const val DEFAULT_TOKEN = ""
        const val DEFAULT_METRICS_DIR = "metrics/"

        fun writeEvaluationResult(
            metricsDir: File,
            evaluationResult: RunEvaluation,
            fileName: String,
        ) {
            val file = File(metricsDir, "$fileName.json")
            JaktaJsonComponent.json.encodeToStream(evaluationResult, file.outputStream())
        }

        fun writeGenerationResult(
            metricsDir: File,
            generationResult: PGPInvocation,
            fileName: String,
        ) {
            val file = File(metricsDir, "$fileName.txt")
            val formattedContent =
                generationResult
                    .generatedPlans
                    .joinToString("\n\n") { planFormatter.format(it) ?: "" }
            writeToFile(formattedContent, file)
        }

        fun writeChatHistory(
            metricsDir: File,
            history: List<ChatMessage>,
            fileName: String,
        ) {
            val file = File(metricsDir, "$fileName.txt")
            if (history.isNotEmpty()) {
                writeToFile(formatHistory(history), file)
            }
        }

        private fun formatHistory(history: List<ChatMessage>) =
            history
                .joinToString("\n") {
                    "-".repeat(80) + "\n" +
                        it.role.role.capitalize() + "\n" +
                        "-".repeat(80) + "\n" +
                        it.content + "\n"
                }
    }
}

fun main(args: Array<String>) = EvalRun().main(args)

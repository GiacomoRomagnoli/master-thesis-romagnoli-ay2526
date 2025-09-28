package it.unibo.jakta.evals

import com.aallam.openai.api.chat.ChatMessage
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import it.unibo.jakta.agents.bdi.engine.FileUtils.writeToFile
import it.unibo.jakta.agents.bdi.engine.Jakta.capitalize
import it.unibo.jakta.agents.bdi.engine.Jakta.separator
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.planFormatter
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JaktaJsonComponent
import it.unibo.jakta.evals.evaluators.run.RunEvaluation
import it.unibo.jakta.evals.retrievers.plandata.PGPInvocation
import it.unibo.jakta.exp.base.BaseExpRunner.modulesToLoad
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.util.UUID

/**
 * Command-line tool for evaluating the results of the experiments.
 */
@OptIn(ExperimentalSerializationApi::class)
abstract class AbstractRunEvaluator : CliktCommand() {
    val runId: String by option()
        .default(UUID.randomUUID().toString())
        .help("The UUID that identifies the invocation run.")

    val runDir: String by option()
        .default(DEFAULT_RUN_DIR)
        .help("The directory where the experiments' traces are stored.")

    val retrieveGenerationData: Boolean by option()
        .boolean()
        .default(false)
        .help("Whether to retrieve generation data from OpenRouter or not.")

    val metricsDir: String by option()
        .default(DEFAULT_METRICS_DIR)
        .help("The directory where the result of the evaluation is stored.")

    val authToken: String by option(envvar = "API_KEY")
        .default(DEFAULT_TOKEN)
        .help("The secret API key to use for authentication with the server")

    init {
        JaktaKoin.loadAdditionalModules(modulesToLoad)
    }

    companion object {
        val DEFAULT_RUN_DIR = "experiments$separator"
        const val DEFAULT_TOKEN = ""
        val DEFAULT_METRICS_DIR = "metrics$separator"

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

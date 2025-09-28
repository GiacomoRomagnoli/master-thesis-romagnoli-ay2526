package it.unibo.jakta.scheduler

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import it.unibo.jakta.agents.bdi.engine.Jakta.separator
import it.unibo.jakta.evals.AbstractRunEvaluator.Companion.DEFAULT_TOKEN
import it.unibo.jakta.scheduler.client.JobClient
import it.unibo.jakta.scheduler.server.Utils.fetchDirectories
import it.unibo.jakta.scheduler.server.domain.jobRequests.ChatCompletionJobRequest
import it.unibo.jakta.scheduler.server.domain.jobRequests.EvalJobRequest
import it.unibo.jakta.scheduler.server.domain.jobRequests.JobRequest
import it.unibo.jakta.scheduler.server.domain.jobRequests.SimulatorJobRequest
import kotlinx.coroutines.runBlocking
import kotlin.io.path.Path
import kotlin.io.path.exists

class AblationStudyScheduler : CliktCommand() {
    val chatCompletionExePath: String? by option(envvar = "LM_INVOKER_EXE_PATH")
        .help("Path to the executable that runs that requests chat completions.")

    val simulatorExePath: String? by option(envvar = "SIMULATOR_EXE_PATH")
        .help("Path to the executable that runs the evaluated plans in the simulator.")

    val evaluatorExePath: String? by option(envvar = "EVAL_EXE_PATH")
        .help("Path to the executable that evaluates the plan generated and their results.")

    val authToken: String by option(envvar = "API_KEY")
        .default(DEFAULT_TOKEN)
        .help("The secret API key to use for authentication with the lm inference server")

    val availableProcessors = Runtime.getRuntime().availableProcessors()
    val client = JobClient()
    val invokerLogs = "invoker-logs$separator"
    val simulatorLogs = "simulator-logs$separator"
    val metricsPath = "metrics$separator"
    val promptPath = "..${separator}jakta-lm-invoker${separator}prompt$separator"
    val repetitions = 1
    val envType = "sparse"

    override fun run(): Unit =
        runBlocking {
            try {
                // Phase 1: Chat Completion Jobs
                val chatCompletionJobs = createChatCompletionJobs()
                executeJobs("chat completion", chatCompletionJobs)

                println("\nNo more chat completion jobs, starting other jobs...")

                // Phase 2: Simulator Jobs
                val invokerCachePath = Path(invokerLogs)
                if (invokerCachePath.exists()) {
                    val completionPaths = fetchDirectories(Path(invokerLogs)).map { "$invokerLogs$it" }.toSet()
                    if (completionPaths.isNotEmpty()) {
                        val simulationJobs = createSimulationJobs(completionPaths)
                        executeJobs("simulation", simulationJobs)
                    } else {
                        println("No chat completion logs found")
                    }
                } else {
                    println("No other jobs found")
                }

                // Phase 3: Evaluator Jobs
                val simulatorCachePath = Path(simulatorLogs)
                if (simulatorCachePath.exists()) {
                    val simulationPaths = fetchDirectories(Path(simulatorLogs)).map { "$simulatorLogs$it" }.toSet()
                    if (simulationPaths.isNotEmpty()) {
                        val evaluationJobs = createEvaluationJobs(simulationPaths)
                        executeJobs("evaluation", evaluationJobs)
                    } else {
                        println("No simulator logs found")
                    }
                } else {
                    println("No other jobs found")
                }
            } catch (e: Exception) {
                println("Error: ${e.message}")
                e.printStackTrace()
            } finally {
                client.close()
            }
        }

    private fun createChatCompletionJobs(): List<JobRequest> =
        chatCompletionExePath?.let { exePath ->
            List(repetitions) { i ->
                ChatCompletionJobRequest(
                    name = "LM Invocation - Run $i",
                    parameters =
                        buildMap {
//                        put("dry-run", listOf("true"))
                            put("log-to-file", setOf("true"))
                            put("prompt-snippets-path", setOf(promptPath))
                            put("model-id", buildSet { add("deepseek/deepseek-chat-v3.1:free") })
                            put("temperature", setOf("0.1"))
                            put("reasoning-effort", setOf("minimal"))
                            put("max-tokens", setOf("4069"))
                            put("lm-server-url", setOf("https://openrouter.ai/api/v1/"))
                            put("lm-server-token", setOf(authToken))
                            put("log-to-file", setOf("true"))
                            put("log-dir", setOf(invokerLogs))
                            put("environment-type", setOf(envType))
                            put("without-admissible-beliefs-and-goals", setOf("false"))
                            put("asl-syntax-explanation-level", setOf("Standard"))
                            put("with-bdi-agent-definition", setOf("false"))
                            put("few-shot", setOf("false"))
                            put("without-logic-description", setOf("false"))
                            put("without-nl-description", setOf("false"))
                            put("prompt-technique", setOf("NoCoT"))
                        },
                    maxParallel = availableProcessors,
                    executablePath = exePath,
                    cachePath = invokerLogs,
                )
            }
        } ?: emptyList()

    private fun createSimulationJobs(completionPaths: Set<String>): List<JobRequest> =
        buildList {
            simulatorExePath?.let { path ->
                add(
                    SimulatorJobRequest(
                        name = "Simulation",
                        parameters =
                            buildMap {
                                put("replay-exp", setOf("true"))
                                put("exp-replay-path", completionPaths)
                                put("prompt-snippets-path", setOf(promptPath))
                                put("run-timeout-millis", setOf("5000"))
                                put("log-to-file", setOf("true"))
                                put("log-dir", setOf(simulatorLogs))
                                put("environment-type", setOf(envType))
                            },
                        maxParallel = availableProcessors,
                        executablePath = path,
                        cachePath = simulatorLogs,
                    ),
                )
            }
        }

    private fun createEvaluationJobs(simulationPaths: Set<String>): List<JobRequest> =
        buildList {
            evaluatorExePath?.let { path ->
                add(
                    EvalJobRequest(
                        name = "Evaluation",
                        parameters =
                            buildMap {
                                put("run-dir", simulationPaths)
                                put("metrics-dir", setOf(metricsPath))
                                // put("retrieve-generation-data", listOf("true"))
                            },
                        maxParallel = availableProcessors,
                        executablePath = path,
                        cachePath = metricsPath,
                    ),
                )
            }
        }

    private suspend fun executeJobs(
        phaseName: String,
        jobs: List<JobRequest>,
    ) {
        if (jobs.isEmpty()) {
            println("\nNo $phaseName jobs to execute")
            return
        }

        val jobIDs = createJobs(jobs)
        println("\nCreated ${jobIDs.size} $phaseName jobs")

        printCurrentJobs()

        startJobsSequentially(jobIDs, phaseName)
    }

    private suspend fun createJobs(jobs: List<JobRequest>): List<String> =
        jobs.mapNotNull { job -> client.createJob(job) }

    private suspend fun printCurrentJobs() {
        println("\nCurrent jobs:")
        val allJobs = client.listJobs()
        allJobs.forEach { exp ->
            println("  - ${exp.name} (${exp.status}) - ${exp.totalRuns} runs")
        }
    }

    private suspend fun startJobsSequentially(
        jobIDs: List<String>,
        phaseName: String,
    ) {
        println("\nStarting $phaseName jobs:")

        jobIDs.forEachIndexed { index, jobId ->
            println("\nStarting job ${index + 1} of ${jobIDs.size}...")
            client.startJob(jobId)
            client.waitForJobCompletion(jobId)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) = AblationStudyScheduler().main(args)
    }
}

package it.unibo.jakta.scheduler

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import it.unibo.jakta.scheduler.client.JobClient
import it.unibo.jakta.scheduler.server.domain.jobRequests.ChatCompletionJobRequest
import it.unibo.jakta.scheduler.server.domain.jobRequests.EvalJobRequest
import it.unibo.jakta.scheduler.server.domain.jobRequests.SimulatorJobRequest
import kotlinx.coroutines.runBlocking

class AblationStudyScheduler : CliktCommand() {
    val chatCompletionExePath: String? by option(envvar = "LM_INVOKER_EXE_PATH")
        .help("Path to the executable that runs that requests chat completions.")

    val simulatorExePath: String? by option(envvar = "SIMULATOR_EXE_PATH")
        .help("Path to the executable that runs the evaluated plans in the simulator.")

    val evaluatorExePath: String? by option(envvar = "EVAL_EXE_PATH")
        .help("Path to the executable that evaluates the plan generated and their results.")

    val availableProcessors = Runtime.getRuntime().availableProcessors()
    val client = JobClient()

    override fun run(): Unit =
        runBlocking {
            try {
                println("Scheduling jobs")
                val jobs =
                    buildList {
                        chatCompletionExePath
                            ?.let {
                                ChatCompletionJobRequest(
                                    name = "Ask an LLM to generate a plan",
                                    parameters =
                                        buildMap {
                                            put("model", listOf("deepseek", "openai/gpt-oss"))
                                        },
                                    maxParallel = availableProcessors,
                                    executablePath = it,
                                )
                            }.let { add(it) }

                        simulatorExePath
                            ?.let {
                                SimulatorJobRequest(
                                    name = "Run the simulator",
                                    parameters =
                                        buildMap {
                                            put("name", listOf("World"))
                                        },
                                    maxParallel = availableProcessors,
                                    executablePath = it,
                                )
                            }.let { add(it) }

                        evaluatorExePath
                            ?.let {
                                EvalJobRequest(
                                    name = "Evaluate the results",
                                    parameters =
                                        buildMap {
                                            put("name", listOf("World"))
                                        },
                                    maxParallel = availableProcessors,
                                    executablePath = it,
                                )
                            }.let { add(it) }
                    }

                val jobIDs = mutableListOf<String>()

                jobs.forEach { job ->
                    val id = job?.let { client.createJob(it) }
                    if (id != null) {
                        jobIDs.add(id)
                    }
                }

                println("\nCreated ${jobIDs.size} jobs")

                println("\nCurrent jobs:")
                val allJobs = client.listJobs()
                allJobs.forEach { exp ->
                    println("  - ${exp.name} (${exp.status}) - ${exp.totalRuns} runs")
                }

                println("\nStarting jobs:")
                if (jobIDs.isNotEmpty()) {
                    println("\nStarting first job...")
                    val firstId = jobIDs.first()
                    client.startJob(firstId)

                    // Wait for it to complete
                    client.waitForJobCompletion(firstId)

                    // Start another job if available
                    if (jobIDs.size > 1) {
                        println("\nStarting second job...")
                        val secondId = jobIDs[1]
                        client.startJob(secondId)
                        client.waitForJobCompletion(secondId)
                    }
                }

                println("\nNo more jobs!")
            } catch (e: Exception) {
                println("Error: ${e.message}")
                e.printStackTrace()
            } finally {
                client.close()
            }
        }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) = AblationStudyScheduler().main(args)
    }
}

package it.unibo.jakta.playground

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import it.unibo.jakta.evals.server.domain.ExperimentRequest
import it.unibo.jakta.evals.server.domain.ExperimentResponse
import it.unibo.jakta.evals.server.domain.ExperimentStatus
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

@Serializable
data class CreateExperimentResponse(
    val id: String,
)

class ExperimentClient(
    private val baseUrl: String = "http://localhost:8080",
) {
    private val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    },
                )
            }
        }

    suspend fun createExperiment(request: ExperimentRequest): String? =
        try {
            val response: CreateExperimentResponse =
                client
                    .post("$baseUrl/api/experiments") {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }.body()

            println("Created experiment: ${request.name} (ID: ${response.id})")
            response.id
        } catch (e: Exception) {
            println("Failed to create experiment: ${e.message}")
            null
        }

    suspend fun startExperiment(experimentId: String): Boolean =
        try {
            client.post("$baseUrl/api/experiments/$experimentId/start")
            println("Started experiment: $experimentId")
            true
        } catch (e: Exception) {
            println("Failed to start experiment: ${e.message}")
            false
        }

    suspend fun stopExperiment(experimentId: String): Boolean =
        try {
            client.post("$baseUrl/api/experiments/$experimentId/stop")
            println("Stopped experiment: $experimentId")
            true
        } catch (e: Exception) {
            println("Failed to stop experiment: ${e.message}")
            false
        }

    suspend fun getExperiment(experimentId: String): ExperimentResponse? =
        try {
            client.get("$baseUrl/api/experiments/$experimentId").body()
        } catch (e: Exception) {
            println("Failed to get experiment: ${e.message}")
            null
        }

    suspend fun listExperiments(): List<ExperimentResponse> =
        try {
            client.get("$baseUrl/api/experiments").body()
        } catch (e: Exception) {
            println("Failed to list experiments: ${e.message}")
            emptyList()
        }

    suspend fun waitForExperimentCompletion(
        experimentId: String,
        pollInterval: Long = 2000,
    ) {
        println("Waiting for experiment $experimentId to complete...")

        while (true) {
            val experiment = getExperiment(experimentId)
            if (experiment == null) {
                println("Experiment not found")
                break
            }

            println("Status: ${experiment.status} (${experiment.completedRuns}/${experiment.totalRuns} completed)")

            when (experiment.status) {
                ExperimentStatus.COMPLETED, ExperimentStatus.FAILED, ExperimentStatus.CANCELLED -> {
                    println("Experiment finished with status: ${experiment.status}")
                    break
                }

                else -> {}
            }

            delay(pollInterval)
        }
    }

    fun close() {
        client.close()
    }
}

suspend fun main() {
    val client = ExperimentClient()

    try {
        println("Scheduling experiments")

        val experiments =
            listOf(
                ExperimentRequest(
                    name = "File System Test",
                    commandTemplate =
                        listOf(
                            "jakta-exp/build/native/nativeCompile/jakta-exp",
                            "--model-id",
                            "{model}",
                            "--timeout-millis",
                            "{timeout}",
                        ),
                    parameters =
                        mapOf(
                            "model" to listOf("test", "test1", "test2", "test3"),
                            "timeout" to listOf("500", "1000", "2000"),
                        ),
                    maxParallel = 2,
                ),
                ExperimentRequest(
                    name = "Performance Benchmark",
                    commandTemplate =
                        listOf(
                            "echo",
                            "Running benchmark with {threads} threads and {iterations} iterations",
                        ),
                    parameters =
                        mapOf(
                            "threads" to listOf("1", "2", "4", "8"),
                            "iterations" to listOf("100", "1000"),
                        ),
                    maxParallel = 3,
                ),
                ExperimentRequest(
                    name = "Simple Echo Test",
                    commandTemplate = listOf("echo", "Hello {name}!"),
                    parameters =
                        mapOf(
                            "name" to listOf("World", "Kotlin", "Ktor", "Experiments"),
                        ),
                    maxParallel = 1,
                ),
            )

        val experimentIds = mutableListOf<String>()

        // Create all experiments
        for (experiment in experiments) {
            val id = client.createExperiment(experiment)
            if (id != null) {
                experimentIds.add(id)
            }
        }

        println("\nCreated ${experimentIds.size} experiments")

        // List all experiments
        println("\nCurrent experiments:")
        val allExperiments = client.listExperiments()
        allExperiments.forEach { exp ->
            println("  - ${exp.name} (${exp.status}) - ${exp.totalRuns} runs")
        }

        // Start some experiments
        if (experimentIds.isNotEmpty()) {
            println("\nStarting first experiment...")
            val firstId = experimentIds.first()
            client.startExperiment(firstId)

            // Wait for it to complete
            client.waitForExperimentCompletion(firstId)

            // Start another experiment if available
            if (experimentIds.size > 1) {
                println("\nStarting second experiment...")
                val secondId = experimentIds[1]
                client.startExperiment(secondId)
                client.waitForExperimentCompletion(secondId)
            }
        }

        println("\nClient finished!")
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    } finally {
        client.close()
    }
}

suspend fun createAndStartExperiment(
    client: ExperimentClient,
    experiment: ExperimentRequest,
): String? {
    val id = client.createExperiment(experiment) ?: return null
    return if (client.startExperiment(id)) id else null
}

suspend fun monitorExperiments(client: ExperimentClient) {
    while (true) {
        val experiments = client.listExperiments()
        println("\nExperiment Status Summary:")
        experiments.forEach { exp ->
            println("${exp.name}: ${exp.status} (${exp.completedRuns}/${exp.totalRuns})")
        }
        delay(5.seconds)
    }
}

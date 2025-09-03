package it.unibo.jakta.evals.server

import it.unibo.jakta.evals.server.domain.CommandResult
import it.unibo.jakta.evals.server.domain.Experiment
import it.unibo.jakta.evals.server.domain.ExperimentResponse
import it.unibo.jakta.evals.server.domain.ExperimentStatus
import it.unibo.jakta.evals.server.domain.Run
import it.unibo.jakta.evals.server.domain.RunStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

class ExperimentScheduler(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) {
    private val experiments = ConcurrentHashMap<String, Experiment>()
    private val runs = ConcurrentHashMap<String, Run>()
    private val executors = ConcurrentHashMap<String, Job>()

    fun cartesianProduct(params: Map<String, List<String>>): Sequence<Map<String, String>> {
        if (params.isEmpty()) return sequenceOf(emptyMap())

        return params.entries.fold(sequenceOf(emptyMap())) { acc, (key, values) ->
            acc.flatMap { map ->
                values.asSequence().map { value ->
                    map + (key to value)
                }
            }
        }
    }

    fun createExperiment(experiment: Experiment): String {
        experiments[experiment.id] = experiment

        val experimentRuns =
            cartesianProduct(experiment.parameters)
                .map { paramCombination ->
                    val command = interpolateCommand(experiment.commandTemplate, paramCombination)
                    Run(
                        experimentId = experiment.id,
                        parameters = paramCombination,
                        command = command,
                    )
                }.toList()

        experimentRuns.forEach { run -> runs[run.id] = run }
        println("Created experiment '${experiment.name}' with ${experimentRuns.size} runs")
        return experiment.id
    }

    private fun interpolateCommand(
        template: List<String>,
        params: Map<String, String>,
    ): List<String> =
        template.map { part ->
            params.entries.fold(part) { acc, (key, value) ->
                acc.replace("{$key}", value)
            }
        }

    fun startExperiment(experimentId: String): Boolean {
        val experiment = experiments[experimentId] ?: return false

        if (executors.containsKey(experimentId)) {
            println("Experiment $experimentId is already running")
            return false
        }

        val limitedDispatcher = Dispatchers.Default.limitedParallelism(experiment.maxParallel)
        val job =
            scope.launch(limitedDispatcher) {
                val pendingRuns =
                    runs.values.filter { run ->
                        run.experimentId == experimentId && run.status == RunStatus.PENDING
                    }

                println(
                    "Starting experiment '${experiment.name}' with ${pendingRuns.size} pending runs (max parallel: ${experiment.maxParallel})",
                )

                val runJobs =
                    pendingRuns.map { run ->
                        async {
                            executeRun(run)
                        }
                    }

                runJobs.awaitAll()
                println("Experiment '${experiment.name}' completed")
            }

        executors[experimentId] = job
        return true
    }

    suspend fun stopExperiment(experimentId: String): Boolean {
        val job = executors[experimentId] ?: return false

        runs.values
            .filter { run -> run.experimentId == experimentId && run.status == RunStatus.RUNNING }
            .forEach { run ->
                runs[run.id] =
                    run.copy(
                        status = RunStatus.CANCELLED,
                        completedAt = Instant.now().toEpochMilli(),
                    )
            }

        job.cancelAndJoin()
        executors.remove(experimentId)
        println("Experiment $experimentId stopped")
        return true
    }

    private suspend fun executeRun(run: Run) {
        val startTime = Instant.now().toEpochMilli()
        runs[run.id] =
            run.copy(
                status = RunStatus.RUNNING,
                startedAt = startTime,
            )

        println("Running: ${run.command}")

        try {
            val result = executeCommand(run.command)

            val completedRun =
                run.copy(
                    status = if (result.exitCode == 0) RunStatus.COMPLETED else RunStatus.FAILED,
                    completedAt = Instant.now().toEpochMilli(),
                    exitCode = result.exitCode,
                    output = result.output,
                    error = result.error,
                )
            runs[run.id] = completedRun

            val duration = (completedRun.completedAt!! - startTime) / 1000.0
            println("Completed (${duration}s): ${run.command} -> exit code ${result.exitCode}")
        } catch (e: CancellationException) {
            runs[run.id] =
                run.copy(
                    status = RunStatus.CANCELLED,
                    completedAt = Instant.now().toEpochMilli(),
                )
            println("Cancelled: ${run.command}")
            throw e
        } catch (e: Exception) {
            runs[run.id] =
                run.copy(
                    status = RunStatus.FAILED,
                    completedAt = Instant.now().toEpochMilli(),
                    error = e.message,
                )
            println("Failed: ${run.command} -> ${e.message}")
        }
    }

    private suspend fun executeCommand(command: List<String>): CommandResult =
        withContext(Dispatchers.IO) {
            try {
                val processBuilder =
                    ProcessBuilder(command)
                        .redirectErrorStream(false)

                val process = processBuilder.start()

                val outputDeferred =
                    async {
                        process.inputStream.bufferedReader().useLines { lines ->
                            lines.joinToString("\n")
                        }
                    }
                val errorDeferred =
                    async {
                        process.errorStream.bufferedReader().useLines { lines ->
                            lines.joinToString("\n")
                        }
                    }

                val exitCode = process.waitFor()
                val output = outputDeferred.await()
                val error = errorDeferred.await()

                CommandResult(
                    exitCode = exitCode,
                    output = output.takeIf { it.isNotBlank() },
                    error = error.takeIf { it.isNotBlank() },
                )
            } catch (e: Exception) {
                CommandResult(
                    exitCode = -1,
                    output = null,
                    error = "Failed to execute command: ${e.message}",
                )
            }
        }

    fun getExperimentStatus(experimentId: String): ExperimentStatus? {
        val experimentRuns = runs.values.filter { it.experimentId == experimentId }

        return when {
            experimentRuns.isEmpty() -> ExperimentStatus.CREATED
            experimentRuns.any { run -> run.status == RunStatus.RUNNING } -> ExperimentStatus.RUNNING
            experimentRuns.all { run -> run.status in listOf(RunStatus.COMPLETED, RunStatus.FAILED) } -> {
                if (experimentRuns.any { run -> run.status == RunStatus.FAILED }) {
                    ExperimentStatus.FAILED
                } else {
                    ExperimentStatus.COMPLETED
                }
            }
            experimentRuns.any { it.status == RunStatus.CANCELLED } -> ExperimentStatus.CANCELLED
            else -> ExperimentStatus.CREATED
        }
    }

    fun getExperimentResponse(experimentId: String): ExperimentResponse? {
        val experiment = experiments[experimentId] ?: return null
        val experimentRuns = runs.values.filter { it.experimentId == experimentId }
        val statusCounts = experimentRuns.groupBy { it.status }.mapValues { it.value.size }

        return ExperimentResponse(
            id = experiment.id,
            name = experiment.name,
            status = getExperimentStatus(experimentId) ?: ExperimentStatus.CREATED,
            createdAt = experiment.createdAt,
            totalRuns = experimentRuns.size,
            pendingRuns = statusCounts[RunStatus.PENDING] ?: 0,
            runningRuns = statusCounts[RunStatus.RUNNING] ?: 0,
            completedRuns = statusCounts[RunStatus.COMPLETED] ?: 0,
            failedRuns = statusCounts[RunStatus.FAILED] ?: 0,
            cancelledRuns = statusCounts[RunStatus.CANCELLED] ?: 0,
        )
    }

    fun listExperiments(): List<ExperimentResponse> =
        experiments.values.mapNotNull { exp ->
            getExperimentResponse(exp.id)
        }

    fun getExperiment(experimentId: String): Experiment? = experiments[experimentId]

    fun getExperimentRuns(experimentId: String): List<Run> = runs.values.filter { it.experimentId == experimentId }

    suspend fun awaitExperimentCompletion(experimentId: String) {
        executors[experimentId]?.join()
    }

    fun shutdown() {
        scope.cancel()
    }
}

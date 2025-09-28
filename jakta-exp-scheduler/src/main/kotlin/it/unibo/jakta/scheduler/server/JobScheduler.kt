package it.unibo.jakta.scheduler.server

import it.unibo.jakta.scheduler.server.Utils.generateRunHashWithJobId
import it.unibo.jakta.scheduler.server.Utils.loadCache
import it.unibo.jakta.scheduler.server.domain.CommandResult
import it.unibo.jakta.scheduler.server.domain.JobResponse
import it.unibo.jakta.scheduler.server.domain.JobStatus
import it.unibo.jakta.scheduler.server.domain.Run
import it.unibo.jakta.scheduler.server.domain.RunStatus
import it.unibo.jakta.scheduler.server.domain.ScheduledJob
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

class JobScheduler(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) {
    private val jobs = ConcurrentHashMap<String, ScheduledJob>()
    private val jobsCache = ConcurrentHashMap<String, Set<String>>()
    private val runs = ConcurrentHashMap<String, Run>()
    private val executors = ConcurrentHashMap<String, Job>()

    fun cartesianProduct(params: Map<String, Set<String>>): Sequence<Map<String, String>> {
        if (params.isEmpty()) return sequenceOf(emptyMap())

        return params.entries.fold(sequenceOf(emptyMap())) { acc, (key, values) ->
            acc.flatMap { map ->
                values.asSequence().map { value ->
                    map + (key to value)
                }
            }
        }
    }

    fun createJob(job: ScheduledJob): String {
        jobs[job.id] = job
        job.cachePath?.let { p -> jobsCache[job.id] = loadCache(p) }

        var createdRuns = 0
        var cachedRuns = 0
        var failedRunsReset = 0
        val seenIds = mutableSetOf<String>()

        cartesianProduct(job.parameters)
            .forEach { paramCombination ->
                val id = generateRunHashWithJobId(job.name, paramCombination)

                if (seenIds.contains(id)) {
                    println("ERROR: Duplicate ID generated: $id for params: $paramCombination")
                }
                seenIds.add(id)

                // Check if this run already exists with a FAILED status
                val existingRun = runs[id]
                val isFailed = existingRun?.status == RunStatus.FAILED

                // Only consider it cached if it's in cache AND not failed
                val isCached = jobsCache[job.id]?.contains(id) == true && !isFailed

                val command =
                    interpolateCommand(
                        job.commandTemplate.plus("--run-id").plus("{run-id}"),
                        paramCombination.plus("run-id" to id),
                    )

                val initialStatus =
                    when {
                        isFailed -> {
                            failedRunsReset++
                            RunStatus.PENDING // Reset failed runs to PENDING
                        }
                        isCached -> RunStatus.CACHED
                        else -> RunStatus.PENDING
                    }

                val newRun =
                    Run(
                        id = id,
                        jobId = job.id,
                        parameters = paramCombination,
                        command = command,
                        status = initialStatus,
                    )

                runs[id] = newRun

                when (initialStatus) {
                    RunStatus.CACHED -> cachedRuns++
                    RunStatus.PENDING -> createdRuns++
                    else -> {} // Shouldn't happen
                }
            }

        println(
            "Created job '${job.name}' with $createdRuns new runs, $cachedRuns cached runs, and $failedRunsReset failed runs reset to pending",
        )
        return job.id
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

    fun startJob(jobId: String): Boolean {
        val jobToSchedule = jobs[jobId] ?: return false

        if (executors.containsKey(jobId)) {
            println("Experiment $jobId is already running")
            return false
        }

        val pendingRuns =
            runs.values.filter { run ->
                run.jobId == jobId && run.status == RunStatus.PENDING
            }

        if (pendingRuns.isEmpty()) {
            println("Job '${jobToSchedule.name}' completed immediately - all runs were already cached")
            val completedJob =
                scope.launch {
                    // Job completes immediately
                }
            executors[jobId] = completedJob
            return true
        }

        val limitedDispatcher = Dispatchers.Default.limitedParallelism(jobToSchedule.maxParallel)
        val job =
            scope.launch {
                println("Starting job '${jobToSchedule.name}' with ${pendingRuns.size} pending runs")

                val runJobs =
                    pendingRuns.map { run ->
                        async(limitedDispatcher) {
                            executeRun(run)
                        }
                    }

                runJobs.awaitAll()
                println("Job '${jobToSchedule.name}' completed")
            }

        executors[jobId] = job
        return true
    }

    suspend fun stopJob(jobId: String): Boolean {
        val job = executors[jobId] ?: return false

        runs.values
            .filter { run -> run.jobId == jobId && run.status == RunStatus.RUNNING }
            .forEach { run ->
                runs[run.id] =
                    run.copy(
                        status = RunStatus.CANCELLED,
                        completedAt = Instant.now().toEpochMilli(),
                    )
            }

        job.cancelAndJoin()
        executors.remove(jobId)
        println("Job $jobId stopped")
        return true
    }

    private suspend fun executeRun(run: Run) {
        val startTime = Instant.now().toEpochMilli()

        // Use compute to atomically update the run status
        runs.compute(run.id) { _, existingRun ->
            if (existingRun == null) {
                println("ERROR: Run ${run.id} not found in runs map!")
                return@compute null
            }
            if (existingRun.status != RunStatus.PENDING) {
                println("WARNING: Run ${run.id} is not PENDING (current: ${existingRun.status}), skipping")
                return@compute existingRun
            }
            existingRun.copy(
                status = RunStatus.RUNNING,
                startedAt = startTime,
            )
        } ?: return // Run was not found or couldn't be updated

        println("Running: ${run.command}")

        try {
            val result = executeCommand(run.command)

            // Atomic update for completion
            runs.compute(run.id) { _, existingRun ->
                existingRun?.copy(
                    status = if (result.exitCode == 0) RunStatus.COMPLETED else RunStatus.FAILED,
                    completedAt = Instant.now().toEpochMilli(),
                    exitCode = result.exitCode,
                    output = result.output,
                    error = result.error,
                )
            }

            val duration = (Instant.now().toEpochMilli() - startTime) / 1000.0
            println("Completed (${duration}s): ${run.command} -> exit code ${result.exitCode}")
            if (result.error?.isNotEmpty() == true) {
                println("Error: ${result.error}")
            }
        } catch (e: CancellationException) {
            runs.compute(run.id) { _, existingRun ->
                existingRun?.copy(
                    status = RunStatus.CANCELLED,
                    completedAt = Instant.now().toEpochMilli(),
                )
            }
            println("Cancelled: ${run.command}")
            throw e
        } catch (e: Exception) {
            runs.compute(run.id) { _, existingRun ->
                existingRun?.copy(
                    status = RunStatus.FAILED,
                    completedAt = Instant.now().toEpochMilli(),
                    error = e.message,
                )
            }
            println("Failed: ${run.command} -> ${e.message}")
        }
    }

    fun response(jobId: String): JobResponse? {
        val job = jobs[jobId] ?: return null
        val jobRuns = runs.values.filter { it.jobId == jobId }

        val statusCounts = jobRuns.groupBy { it.status }.mapValues { it.value.size }

        // DEBUG: Check for parameter duplicates
        val paramGroups = jobRuns.groupBy { it.parameters }
        val duplicateParams = paramGroups.filter { it.value.size > 1 }
        if (duplicateParams.isNotEmpty()) {
            println("ERROR: Found duplicate parameter combinations:")
            duplicateParams.forEach { (params, runs) ->
                println("  Params $params has ${runs.size} runs: ${runs.map { "${it.id}:${it.status}" }}")
            }
        }

        return JobResponse(
            id = job.id,
            name = job.name,
            status = getJobStatus(jobId) ?: JobStatus.CREATED,
            createdAt = job.createdAt,
            totalRuns = jobRuns.size,
            pendingRuns = statusCounts[RunStatus.PENDING] ?: 0,
            runningRuns = statusCounts[RunStatus.RUNNING] ?: 0,
            completedRuns = statusCounts[RunStatus.COMPLETED] ?: 0,
            failedRuns = statusCounts[RunStatus.FAILED] ?: 0,
            cancelledRuns = statusCounts[RunStatus.CANCELLED] ?: 0,
        )
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

    private fun getJobStatus(jobId: String): JobStatus? {
        val jobRuns = runs.values.filter { it.jobId == jobId }

        return when {
            jobRuns.isEmpty() -> JobStatus.CREATED
            jobRuns.any { run -> run.status == RunStatus.RUNNING } -> JobStatus.RUNNING
            jobRuns.all { run ->
                run.status in listOf(RunStatus.COMPLETED, RunStatus.FAILED, RunStatus.CACHED)
            } -> {
                if (jobRuns.any { run -> run.status == RunStatus.FAILED }) {
                    JobStatus.FAILED
                } else {
                    JobStatus.COMPLETED
                }
            }
            jobRuns.any { it.status == RunStatus.CANCELLED } -> JobStatus.CANCELLED
            else -> JobStatus.CREATED
        }
    }

    fun listJobs(): List<JobResponse> =
        jobs.values.mapNotNull { exp ->
            response(exp.id)
        }

    fun getExperiment(jobId: String): ScheduledJob? = jobs[jobId]

    fun getJobRuns(jobId: String): List<Run> = runs.values.filter { it.jobId == jobId }
}

package it.unibo.jakta.scheduler.server

import it.unibo.jakta.scheduler.server.Utils.generateRunHashWithJobId
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Paths
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

    private fun loadCache(cacheDir: String): Set<String> =
        try {
            val experimentsPath = Paths.get(cacheDir)
            if (!java.nio.file.Files
                    .exists(experimentsPath)
            ) {
                println("Experiments directory '$cacheDir' does not exist, creating it...")
                java.nio.file.Files
                    .createDirectories(experimentsPath)
                emptySet()
            } else {
                java.nio.file.Files
                    .list(experimentsPath)
                    .use { stream ->
                        stream
                            .filter {
                                java.nio.file.Files
                                    .isDirectory(it)
                            }.map { it.fileName.toString() }
                            .collect(
                                java.util.stream.Collectors
                                    .toSet(),
                            )
                    }.also { hashes ->
                        println("Found ${hashes.size} existing experiment directories in '$cacheDir'")
                        if (hashes.isNotEmpty()) {
                            println("Sample existing hashes: ${hashes.take(5).joinToString(", ")}")
                        }
                    }
            }
        } catch (e: Exception) {
            println("Error loading existing hashes: ${e.message}")
            emptySet()
        }

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

    fun createJob(job: ScheduledJob): String {
        jobs[job.id] = job
        job.cachePath?.let { p -> jobsCache[job.id] = loadCache(p) }

        val jobRuns =
            cartesianProduct(job.parameters)
                .mapNotNull { paramCombination ->
                    val id = generateRunHashWithJobId(job.id, paramCombination)

                    // Keep the run only if it is NOT already cached for this job
                    if (jobsCache[job.id]?.contains(id) != true) {
                        // Make sure the run uses the deterministic id
                        val command =
                            interpolateCommand(
                                job.commandTemplate,
                                paramCombination.plus("run-id" to id),
                            )
                        Run(
                            jobId = job.id,
                            parameters = paramCombination,
                            command = command,
                        )
                    } else {
                        null
                    }
                }.toList()

        jobRuns.forEach { run -> runs[run.id] = run }
        println("Created job '${job.name}' with ${jobRuns.size} new runs")
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

        val limitedDispatcher = Dispatchers.Default.limitedParallelism(jobToSchedule.maxParallel)
        val job =
            scope.launch(limitedDispatcher) {
                val pendingRuns =
                    runs.values.filter { run ->
                        run.jobId == jobId && run.status == RunStatus.PENDING
                    }

                println(
                    "Starting job '${jobToSchedule.name}' with ${pendingRuns.size} pending runs (max parallel: ${jobToSchedule.maxParallel})",
                )

                val runJobs =
                    pendingRuns.map { run ->
                        async {
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

    fun getJobStatus(jobId: String): JobStatus? {
        val jobRuns = runs.values.filter { it.jobId == jobId }

        return when {
            jobRuns.isEmpty() -> JobStatus.CREATED
            jobRuns.any { run -> run.status == RunStatus.RUNNING } -> JobStatus.RUNNING
            jobRuns.all { run -> run.status in listOf(RunStatus.COMPLETED, RunStatus.FAILED) } -> {
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

    fun response(jobId: String): JobResponse? {
        val job = jobs[jobId] ?: return null
        val jobRuns = runs.values.filter { it.jobId == jobId }
        val statusCounts = jobRuns.groupBy { it.status }.mapValues { it.value.size }

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

    fun listJobs(): List<JobResponse> =
        jobs.values.mapNotNull { exp ->
            response(exp.id)
        }

    fun getExperiment(jobId: String): ScheduledJob? = jobs[jobId]

    fun getJobRuns(jobId: String): List<Run> = runs.values.filter { it.jobId == jobId }

    suspend fun awaitJobCompletion(jobId: String) {
        executors[jobId]?.join()
    }

    fun shutdown() {
        scope.cancel()
    }
}

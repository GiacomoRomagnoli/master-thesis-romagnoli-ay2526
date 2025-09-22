package it.unibo.jakta.scheduler.client

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
import it.unibo.jakta.scheduler.server.domain.JobResponse
import it.unibo.jakta.scheduler.server.domain.JobStatus
import it.unibo.jakta.scheduler.server.domain.jobRequests.JobRequest
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

class JobClient(
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

    suspend fun createJob(request: JobRequest): String? =
        try {
            val response: CreateJobResponse =
                client
                    .post("$baseUrl/api/jobs") {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }.body()

            println("Created job: ${request.name} (ID: ${response.id})")
            response.id
        } catch (e: Exception) {
            println("Failed to create job: ${e.message}")
            null
        }

    suspend fun startJob(jobId: String): Boolean =
        try {
            client.post("$baseUrl/api/jobs/$jobId/start")
            println("Started job: $jobId")
            true
        } catch (e: Exception) {
            println("Failed to start job: ${e.message}")
            false
        }

    suspend fun stopJob(jobId: String): Boolean =
        try {
            client.post("$baseUrl/api/jobs/$jobId/stop")
            println("Stopped job: $jobId")
            true
        } catch (e: Exception) {
            println("Failed to stop job: ${e.message}")
            false
        }

    suspend fun getJob(jobId: String): JobResponse? =
        try {
            client.get("$baseUrl/api/jobs/$jobId").body()
        } catch (e: Exception) {
            println("Failed to get job: ${e.message}")
            null
        }

    suspend fun listJobs(): List<JobResponse> =
        try {
            client.get("$baseUrl/api/jobs").body()
        } catch (e: Exception) {
            println("Failed to list jobs: ${e.message}")
            emptyList()
        }

    suspend fun waitForJobCompletion(
        jobId: String,
        pollInterval: Long = 2000,
    ) {
        println("Waiting for job $jobId to complete...")

        while (true) {
            val job = getJob(jobId)
            if (job == null) {
                println("Job not found")
                break
            }

            println("Status: ${job.status} (${job.completedRuns}/${job.totalRuns} completed)")

            when (job.status) {
                JobStatus.COMPLETED, JobStatus.FAILED, JobStatus.CANCELLED -> {
                    println("Job finished with status: ${job.status}")
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

package it.unibo.jakta.scheduler.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import it.unibo.jakta.scheduler.server.JobScheduler
import it.unibo.jakta.scheduler.server.domain.ScheduledJob
import it.unibo.jakta.scheduler.server.domain.jobRequests.JobRequest
import java.util.UUID

fun Application.apiRoutes(scheduler: JobScheduler) =
    routing {
        route("/api") {
            get("/jobs") {
                call.respond(scheduler.listJobs())
            }

            get("/jobs/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val experiment = scheduler.response(id) ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(experiment)
            }

            get("/jobs/{id}/runs") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val runs = scheduler.getJobRuns(id)
                call.respond(runs)
            }

            post("/jobs") {
                try {
                    val request = call.receive<JobRequest>()
                    val scheduledJob =
                        ScheduledJob(
                            id = UUID.randomUUID().toString(),
                            name = request.name,
                            commandTemplate = request.commandTemplate,
                            parameters = request.parameters,
                            maxParallel = request.maxParallel,
                            cachePath = request.cachePath,
                        )
                    val jobId = scheduler.createJob(scheduledJob)
                    call.respond(HttpStatusCode.Created, mapOf("id" to jobId))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            post("/jobs/{id}/start") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val success = scheduler.startJob(id)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Job started"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Failed to start job"))
                }
            }

            post("/jobs/{id}/stop") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val success = scheduler.stopJob(id)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Job stopped"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Failed to stop job"))
                }
            }
        }
    }

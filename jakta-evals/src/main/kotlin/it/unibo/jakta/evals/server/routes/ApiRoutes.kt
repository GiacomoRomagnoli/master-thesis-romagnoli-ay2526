package it.unibo.jakta.evals.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import it.unibo.jakta.evals.server.ExperimentScheduler
import it.unibo.jakta.evals.server.domain.Experiment
import it.unibo.jakta.evals.server.domain.ExperimentRequest

fun Application.apiRoutes(scheduler: ExperimentScheduler) =
    routing {
        route("/api") {
            get("/experiments") {
                call.respond(scheduler.listExperiments())
            }

            get("/experiments/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val experiment =
                    scheduler.getExperimentResponse(id)
                        ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(experiment)
            }

            get("/experiments/{id}/runs") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val runs = scheduler.getExperimentRuns(id)
                call.respond(runs)
            }

            post("/experiments") {
                try {
                    val request = call.receive<ExperimentRequest>()
                    val experiment =
                        Experiment(
                            name = request.name,
                            commandTemplate = request.commandTemplate,
                            parameters = request.parameters,
                            maxParallel = request.maxParallel,
                        )
                    val experimentId = scheduler.createExperiment(experiment)
                    call.respond(HttpStatusCode.Created, mapOf("id" to experimentId))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            post("/experiments/{id}/start") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val success = scheduler.startExperiment(id)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Experiment started"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Failed to start experiment"))
                }
            }

            post("/experiments/{id}/stop") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val success = scheduler.stopExperiment(id)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Experiment stopped"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Failed to stop experiment"))
                }
            }
        }
    }

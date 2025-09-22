package it.unibo.jakta.scheduler.server.routes

import io.ktor.server.application.Application
import io.ktor.server.html.respondHtml
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import it.unibo.jakta.scheduler.server.JobScheduler
import it.unibo.jakta.scheduler.server.Utils.formatTimestamp
import it.unibo.jakta.scheduler.server.domain.JobStatus
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.head
import kotlinx.html.li
import kotlinx.html.onClick
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.title
import kotlinx.html.tr
import kotlinx.html.ul
import kotlinx.html.unsafe

fun Application.uiRoutes(scheduler: JobScheduler) =
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title("Experiment Scheduler")
                    style {
                        unsafe {
                            raw(
                                """
                                body { font-family: Arial, sans-serif; margin: 40px; }
                                .experiment { border: 1px solid #ccc; margin: 20px 0; padding: 20px; border-radius: 8px; }
                                .status { padding: 4px 8px; border-radius: 4px; color: white; font-weight: bold; }
                                .CREATED { background-color: #6c757d; }
                                .RUNNING { background-color: #007bff; }
                                .COMPLETED { background-color: #28a745; }
                                .FAILED { background-color: #dc3545; }
                                .CANCELLED { background-color: #ffc107; color: black; }
                                .runs-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
                                .runs-table th, .runs-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                                .runs-table th { background-color: #f2f2f2; }
                                button { padding: 8px 16px; margin: 5px; border: none; border-radius: 4px; cursor: pointer; }
                                .start-btn { background-color: #28a745; color: white; }
                                .stop-btn { background-color: #dc3545; color: white; }
                                .refresh-btn { background-color: #17a2b8; color: white; }
                            """,
                            )
                        }
                    }
                    script {
                        unsafe {
                            raw(
                                """
                                function startExperiment(id) {
                                    fetch('/api/experiments/' + id + '/start', {method: 'POST'})
                                        .then(() => location.reload());
                                }
                                function stopExperiment(id) {
                                    fetch('/api/experiments/' + id + '/stop', {method: 'POST'})
                                        .then(() => location.reload());
                                }
                                function refreshPage() {
                                    location.reload();
                                }
                            """,
                            )
                        }
                    }
                }
                body {
                    h1 { +"Experiment Scheduler Dashboard" }

                    button(classes = "refresh-btn") {
                        onClick = "refreshPage()"
                        +"Refresh"
                    }

                    val experiments = scheduler.listJobs()

                    if (experiments.isEmpty()) {
                        p { +"No experiments found." }
                    } else {
                        experiments.forEach { exp ->
                            div(classes = "experiment") {
                                h2 { +exp.name }
                                p {
                                    +"Status: "
                                    span(classes = "status ${exp.status}") { +exp.status.toString() }
                                }
                                p { +"Created: ${formatTimestamp(exp.createdAt)}" }
                                p { +"Total Runs: ${exp.totalRuns}" }
                                p {
                                    +"Pending: ${exp.pendingRuns}, "
                                    +"Running: ${exp.runningRuns}, "
                                    +"Completed: ${exp.completedRuns}, "
                                    +"Failed: ${exp.failedRuns}, "
                                    +"Cancelled: ${exp.cancelledRuns}"
                                }

                                when (exp.status) {
                                    JobStatus.CREATED -> {
                                        button(classes = "start-btn") {
                                            onClick = "startExperiment('${exp.id}')"
                                            +"Start Experiment"
                                        }
                                    }

                                    JobStatus.RUNNING -> {
                                        button(classes = "stop-btn") {
                                            onClick = "stopExperiment('${exp.id}')"
                                            +"Stop Experiment"
                                        }
                                    }

                                    else -> {}
                                }

                                // Show experiment parameters
                                val experiment = scheduler.getExperiment(exp.id)
                                if (experiment != null) {
                                    h3 { +"Command Template:" }
                                    p { +experiment.commandTemplate.joinToString(" ") }

                                    h3 { +"Parameters:" }
                                    ul {
                                        experiment.parameters.forEach { (key, values) ->
                                            li { +"$key: ${values.joinToString(", ")}" }
                                        }
                                    }
                                }

                                // Show runs table
                                val runs = scheduler.getJobRuns(exp.id)
                                if (runs.isNotEmpty()) {
                                    h3 { +"Runs:" }
                                    table(classes = "runs-table") {
                                        thead {
                                            tr {
                                                th { +"Parameters" }
                                                th { +"Status" }
                                                th { +"Started" }
                                                th { +"Completed" }
                                                th { +"Duration" }
                                                th { +"Exit Code" }
                                            }
                                        }
                                        tbody {
                                            runs.forEach { run ->
                                                tr {
                                                    td {
                                                        +run.parameters
                                                            .map { "${it.key}=${it.value}" }
                                                            .joinToString(", ")
                                                    }
                                                    td {
                                                        span(
                                                            classes = "status ${run.status}",
                                                        ) { +run.status.toString() }
                                                    }
                                                    td { +formatTimestamp(run.startedAt) }
                                                    td { +formatTimestamp(run.completedAt) }
                                                    td {
                                                        if (run.startedAt != null && run.completedAt != null) {
                                                            +"${(run.completedAt - run.startedAt) / 1000.0}s"
                                                        } else {
                                                            +"-"
                                                        }
                                                    }
                                                    td { +(run.exitCode?.toString() ?: "-") }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

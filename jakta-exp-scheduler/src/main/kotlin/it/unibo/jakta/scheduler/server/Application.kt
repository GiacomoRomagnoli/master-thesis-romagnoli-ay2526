package it.unibo.jakta.scheduler.server

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import it.unibo.jakta.scheduler.server.routes.apiRoutes
import it.unibo.jakta.scheduler.server.routes.uiRoutes
import kotlinx.serialization.json.Json

fun Application.module() {
    println("Server available at http://localhost:8080")

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
            },
        )
    }

    val scheduler = JobScheduler()
    apiRoutes(scheduler)
    uiRoutes(scheduler)
}

fun main(args: Array<String>): Unit = EngineMain.main(args)

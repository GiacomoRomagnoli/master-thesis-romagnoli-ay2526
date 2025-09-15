package it.unibo.jakta.playground

import it.unibo.jakta.agents.bdi.dsl.goals.TriggerMetadata.meaning
import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.dsl.DSLExtensions.lmGeneration
import java.io.File
import java.util.Properties
import kotlin.time.Duration.Companion.seconds

fun readTokenFromEnv(): String? {
    val envFile = File(".env")
    return if (!envFile.exists()) {
        println(".env file not found")
        null
    } else {
        val props = Properties()
        envFile.inputStream().use { props.load(it) }
        props.getProperty("API_KEY")
    }
}

fun main() =
    mas {
        lmGeneration {
            model = "" // "deepseek/deepseek-chat-v3-0324:free"
            temperature = 0.1
            url = "http://localhost:8080/v1" // "https://openrouter.ai/api/v1/"
            token = readTokenFromEnv() ?: ""
            maxTokens = 4096
            socketTimeout = 360.seconds
            requestTimeout = 360.seconds
        }
        loggingConfig = LoggingConfig(logToFile = true)
        agent("Printer") {
            goals {
                +achieve("print"(0, 10))

                admissible {
                    +achieve("print_numbers"("start", "end")).meaning {
                        "Print the numbers from ${args[0]} to ${args[1]}"
                    }
                }
            }
            plans {
                +achieve("print"(X, Y)) then {
                    generatePlan("print_numbers"(X, Y))
                }
            }
        }
    }.start()

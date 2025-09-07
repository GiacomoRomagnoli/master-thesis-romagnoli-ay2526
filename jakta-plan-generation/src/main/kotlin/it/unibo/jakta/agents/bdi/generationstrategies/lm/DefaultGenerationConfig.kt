package it.unibo.jakta.agents.bdi.generationstrategies.lm

import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

object DefaultGenerationConfig {
    const val DEFAULT_MODEL_ID = "test"
    const val DEFAULT_TEMPERATURE = 0.5
    const val DEFAULT_TOP_P = 1.0
    const val DEFAULT_MAX_TOKENS = 2048
    const val DEFAULT_LM_SERVER_URL = "http://localhost:8080"
    const val DEFAULT_TOKEN = ""
    val DEFAULT_REQUEST_TIMEOUT = 2.minutes.toLong(DurationUnit.MILLISECONDS)
    val DEFAULT_CONNECT_TIMEOUT = 10.seconds.toLong(DurationUnit.MILLISECONDS)
    val DEFAULT_SOCKET_TIMEOUT = 1.minutes.toLong(DurationUnit.MILLISECONDS)
}

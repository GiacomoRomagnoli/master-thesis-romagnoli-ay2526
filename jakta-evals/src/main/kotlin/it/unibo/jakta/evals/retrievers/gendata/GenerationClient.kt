package it.unibo.jakta.evals.retrievers.gendata

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JaktaJsonComponent
import java.io.Closeable

class GenerationClient(
    private val authToken: String,
    private val urlString: String = DEFAULT_URL,
) : Closeable {
    private val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(JaktaJsonComponent.json)
            }
        }

    suspend fun getGenerationData(generationId: String): GenerationData? =
        try {
            val response: GenerationResponse =
                client
                    .get(urlString) {
                        header("Authorization", "Bearer $authToken")
                        parameter("id", generationId)
                    }.body()

            response.data
        } catch (e: Exception) {
            println("Error fetching generation data: ${e.message}")
            null
        }

    override fun close() {
        client.close()
    }

    companion object {
        const val DEFAULT_URL = "https://openrouter.ai/api/v1/generation"
    }
}

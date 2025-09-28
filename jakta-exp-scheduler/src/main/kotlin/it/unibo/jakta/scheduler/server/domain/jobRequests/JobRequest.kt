package it.unibo.jakta.scheduler.server.domain.jobRequests

import kotlinx.serialization.Serializable

@Serializable
sealed interface JobRequest {
    val name: String
    val commandTemplate: List<String>
    val parameters: Map<String, Set<String>>
    val maxParallel: Int
    val executablePath: String
    val cachePath: String?
}

package it.unibo.jakta.scheduler.server.domain.jobRequests

interface JobRequest {
    val name: String
    val commandTemplate: List<String>
    val parameters: Map<String, List<String>>
    val maxParallel: Int
    val executablePath: String
    val cachePath: String?
}

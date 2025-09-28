package it.unibo.jakta.scheduler.server.domain.jobRequests

import kotlinx.serialization.Serializable

@Serializable
data class EvalJobRequest(
    override val name: String,
    override val parameters: Map<String, Set<String>>,
    override val maxParallel: Int,
    override val executablePath: String,
    override val cachePath: String? = null,
) : JobRequest {
    override val commandTemplate: List<String> =
        buildList {
            executablePath.split(" ").forEach { add(it) }
            buildList {
                add("run-dir")
                add("retrieve-generation-data")
                add("metrics-dir")
                add("auth-token")
            }.map { l ->
                l.let {
                    parameters[it]?.let { _ ->
                        add("--$it")
                        add("{$it}")
                    }
                }
            }
        }
}

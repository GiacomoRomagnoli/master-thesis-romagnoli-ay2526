package it.unibo.jakta.scheduler.server.domain.jobRequests

import kotlinx.serialization.Serializable

// TODO remove code duplication
// CPD-OFF
@Serializable
data class ChatCompletionJobRequest(
    override val name: String,
    override val parameters: Map<String, List<String>>,
    override val maxParallel: Int,
    override val executablePath: String,
    override val cachePath: String? = null,
) : JobRequest {
    override val commandTemplate: List<String> =
        buildList {
            add(executablePath)
            buildList {
                add("run-id")

                add("temperature")
                add("top-p")
                add("reasoning-effort")
                add("max-tokens")

                add("lm-server-url")
                add("lm-server-token")
                add("request-timeout")
                add("connect-timeout")
                add("socket-timeout")

                add("log-level")
                add("log-to-console")
                add("log-to-file")
                add("log-dir")
                add("log-to-server")
                add("log-server-url")

                add("without-admissible-beliefs")
                add("without-admissible-goals")
                add("expected-result-explanation-level")
                add("asl-syntax-explanation-level")
                add("with-bdi-agent-definition")
                add("few-shot")
                add("without-logic-description")
                add("without-nl-description")
                add("prompt-technique")
                add("remarks")
                add("environment-type")
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
// CPD-ON

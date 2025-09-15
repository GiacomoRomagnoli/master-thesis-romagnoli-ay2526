package it.unibo.jakta.exp.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.long
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_CONNECT_TIMEOUT
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_REQUEST_TIMEOUT
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_SOCKET_TIMEOUT
import it.unibo.jakta.exp.Experiment.Companion.urlRegex
import kotlin.text.matches

class LmServerConfig : OptionGroup(name = "Language Model Server Configuration") {
    val lmServerUrl: String by option()
        .default(DefaultGenerationConfig.DEFAULT_LM_SERVER_URL)
        .help("Url of the server with an OpenAI-compliant API.")
        .check("value must be a valid URL") { it.matches(urlRegex) }

    val lmServerToken: String by option(envvar = "API_KEY")
        .default(DefaultGenerationConfig.DEFAULT_TOKEN)
        .help("The secret API key to use for authentication with the server.")

    val requestTimeout: Long by option()
        .long()
        .default(DEFAULT_REQUEST_TIMEOUT)
        .help("Time period required to process an HTTP call: from sending a request to receiving a response.")

    val connectTimeout: Long by option()
        .long()
        .default(DEFAULT_CONNECT_TIMEOUT)
        .help("Time period in which a client should establish a connection with a server.")

    val socketTimeout: Long by option()
        .long()
        .default(DEFAULT_SOCKET_TIMEOUT)
        .help("Maximum time of inactivity between two data packets when exchanging data with a server.")
}

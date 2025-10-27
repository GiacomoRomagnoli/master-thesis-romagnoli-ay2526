package it.unibo.jakta.exp.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.enum
import it.unibo.jakta.exp.Experiment.Companion.urlRegex

class ExpLoggingConfig : OptionGroup(name = "Logging Configuration") {
    val logLevel: Log4jLevel by option()
        .enum<Log4jLevel>()
        .default(DEFAULT_LOG_LEVEL)
        .help("The minimum log level")

    val logToConsole: Boolean by option()
        .boolean()
        .default(true)
        .help("Whether to output log to std output.")

    val logToFile: Boolean by option()
        .boolean()
        .default(false)
        .help("Whether to output logs to the local filesystem.")

    val logDir: String by option()
        .default(DEFAULT_LOG_DIR)
        .help("Directory where the logs should be stored.")

    val logToServer: Boolean by option()
        .boolean()
        .default(false)
        .help("Whether to output logs to a log server.")

    val logServerUrl: String by option()
        .default(DEFAULT_LOG_SERVER_URL)
        .help("Url of the server where logs are sent.")
        .check("value must be a valid URL") { it.matches(urlRegex) }

    companion object {
        const val DEFAULT_LOG_DIR = "logs"
        val DEFAULT_LOG_LEVEL = Log4jLevel.INFO
        const val DEFAULT_LOG_SERVER_URL = "http://localhost:8081"
    }
}

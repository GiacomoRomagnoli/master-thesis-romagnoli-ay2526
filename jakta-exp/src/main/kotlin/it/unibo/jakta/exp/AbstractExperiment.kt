package it.unibo.jakta.exp

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import it.unibo.jakta.agents.bdi.engine.Mas
import it.unibo.jakta.agents.bdi.engine.generation.GenerationStrategy
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.loggers.LoggerFactory
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_CONNECT_TIMEOUT
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_REQUEST_TIMEOUT
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_SOCKET_TIMEOUT
import it.unibo.jakta.exp.explorer.ModuleLoader
import it.unibo.jakta.exp.gridworld.configuration.EnvironmentType
import it.unibo.jakta.exp.gridworld.environment.GridWorldEnvironment
import it.unibo.jakta.exp.prompt.SystemPromptType
import it.unibo.jakta.exp.prompt.UserPromptType
import java.util.UUID
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

abstract class AbstractExperiment : CliktCommand() {
    private val delegate = LoggerFactory.create("ExperimentRunner", "", LoggingConfig())
    private val expRunnerLogger = delegate.logger
    private val urlRegex = "^https?://([\\w.-]+)(:\\d+)?(/.*)?$".toRegex()

    val runId: String? by option()
        .help("The UUID that identifies the experimental run.")

    val modelId: String by option()
        .help("ID of the model to use.")
        .default("test")

    val temperature: Double by option()
        .double()
        .default(DefaultGenerationConfig.DEFAULT_TEMPERATURE)
        .help(
            """
            What sampling temperature to use, between $MIN_TEMPERATURE and $MAX_TEMPERATURE. 
            Higher values like 0.8 will make the output more random, 
            while lower values like 0.2 will make it more focused and deterministic.
            """.trimIndent(),
        ).check("value must be positive number, between $MIN_TEMPERATURE and $MAX_TEMPERATURE") {
            it in MIN_TEMPERATURE..MAX_TEMPERATURE
        }

    val topP: Double by option()
        .double()
        .default(DefaultGenerationConfig.DEFAULT_TOP_P)
        .help(
            """
            This setting limits the model’s choices to a percentage of likely tokens: 
            only the top tokens whose probabilities add up to P. 
            A lower value makes the model’s responses more predictable, 
            while the default setting allows for a full range of token choices. 
            """.trimIndent(),
        ).check("value must be positive number, between $MIN_TOP_P and $MAX_TOP_P") {
            it in MIN_TOP_P..MAX_TOP_P
        }

    val maxTokens: Int by option()
        .int()
        .default(DefaultGenerationConfig.DEFAULT_MAX_TOKENS)
        .help(
            """
            The maximum number of tokens allowed for the generated answer. 
            By default, the number of tokens the model can return will 
            be ${DefaultGenerationConfig.DEFAULT_MAX_TOKENS}.
            """.trimIndent(),
        )

    val systemPromptType: SystemPromptType by option()
        .enum<SystemPromptType>()
        .default(DEFAULT_SYSTEM_PROMPT)
        .help("The type of system prompt to use.")

    val userPromptType: UserPromptType by option()
        .enum<UserPromptType>()
        .default(DEFAULT_USER_PROMPT)
        .help("The type of user prompt to use.")

    val environmentType: EnvironmentType by option()
        .enum<EnvironmentType>()
        .default(DEFAULT_ENV_TYPE)
        .help("The type of environment to use.")

    val lmServerUrl: String by option()
        .default(DefaultGenerationConfig.DEFAULT_LM_SERVER_URL)
        .help("Url of the server with an OpenAI-compliant API.")
        .check("value must be a valid URL") { it.matches(urlRegex) }

    val lmServerToken: String by option(envvar = "API_KEY")
        .default(DefaultGenerationConfig.DEFAULT_TOKEN)
        .help("The secret API key to use for authentication with the server.")

    val logLevel: Log4jLevel by option()
        .enum<Log4jLevel>()
        .default(DEFAULT_LOG_LEVEL)
        .help("The minimum log level")

    val logToConsole: Boolean by option()
        .flag(default = true)
        .help("Whether to output log to std output.")

    val logToFile: Boolean by option()
        .flag()
        .help("Whether to output logs to the local filesystem.")

    val logDir: String by option()
        .default(DEFAULT_LOG_DIR)
        .help("Whether to output log to the local filesystem.")

    val logToServer: Boolean by option()
        .flag()
        .help("Whether to output logs to a log server.")

    val logServerUrl: String by option()
        .default(DefaultGenerationConfig.DEFAULT_LM_SERVER_URL)
        .help("Url of the server where logs are sent.")
        .check("value must be a valid URL") { it.matches(urlRegex) }

    val expTimeoutMillis: Long by option()
        .long()
        .default(DEFAULT_EXP_TIMEOUT)
        .help("Time in milliseconds before the termination of the experiment.")

    val replayExp: Boolean by option()
        .flag()
        .help("Whether to replay a past experiment or create a new one.")

    val expReplayPath: String by option()
        .default(DEFAULT_LOG_DIR)
        .help(
            """
            Provide the path to the directory that holds the experiment log, 
            or to the specific file containing the chat to replay.
            """.trimIndent(),
        )

    val requestTimeout: Long by option()
        .long()
        .default(DEFAULT_REQUEST_TIMEOUT)
        .help("time period required to process an HTTP call: from sending a request to receiving a response.")

    val connectTimeout: Long by option()
        .long()
        .default(DEFAULT_CONNECT_TIMEOUT)
        .help("time period in which a client should establish a connection with a server.")

    val socketTimeout: Long by option()
        .long()
        .default(DEFAULT_SOCKET_TIMEOUT)
        .help("maximum time of inactivity between two data packets when exchanging data with a server.")

    init {
        ModuleLoader.loadModules()
    }

    override fun run() {
        expRunnerLogger.info("Experiment started")

        val runId = runId ?: UUID.randomUUID().toString()
        val logConfig = createLoggingConfig(runId)
        expRunnerLogger.info(logConfig)

        val genStrat = createGenerationStrategy()
        val mas = createMas(logConfig, genStrat, environmentType.config)
        expRunnerLogger.info("Shutting down in $expTimeoutMillis milliseconds")

        mas.start()
        Thread.sleep(expTimeoutMillis)
        mas.stop()
        expRunnerLogger.info("Run id: $runId")

        exitProcess(0)
    }

    abstract fun createMas(
        logConfig: LoggingConfig,
        genStrat: GenerationStrategy?,
        gridWorldEnv: GridWorldEnvironment,
    ): Mas

    abstract fun createLoggingConfig(expName: String): LoggingConfig

    abstract fun createGenerationStrategy(): GenerationStrategy?

    companion object {
        val DEFAULT_EXP_TIMEOUT = 1.seconds.toLong(DurationUnit.MILLISECONDS)
        const val DEFAULT_LOG_DIR = "logs"
        val DEFAULT_SYSTEM_PROMPT = SystemPromptType.PROMPT_WITHOUT_BDI_AGENT_DEFINITION
        val DEFAULT_USER_PROMPT = UserPromptType.PROMPT_WITH_HINTS
        val DEFAULT_ENV_TYPE = EnvironmentType.Standard
        val DEFAULT_LOG_LEVEL = Log4jLevel.INFO
        const val MIN_TEMPERATURE = 0.0
        const val MAX_TEMPERATURE = 2.0
        const val MIN_TOP_P = 0.0
        const val MAX_TOP_P = 1.0
    }
}

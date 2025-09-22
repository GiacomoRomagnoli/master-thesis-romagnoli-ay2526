package it.unibo.jakta.exp

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.long
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.loggers.LoggerFactory
import it.unibo.jakta.exp.options.ExpLoggingConfig
import it.unibo.jakta.exp.options.LmServerConfig
import it.unibo.jakta.exp.options.ModelConfig
import it.unibo.jakta.exp.options.PromptConfig
import org.koin.core.module.Module
import java.util.UUID
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

// TODO remove code duplication
// CPD-OFF
class Experiment(
    val masFactory: MasFactory,
    val loggingConfigFactory: LoggingConfigFactory,
    val genStrategyFactory: GenerationStrategyFactory,
    modulesToLoad: List<Module>,
) : CliktCommand() {
    private val delegate = LoggerFactory.create("ExperimentRunner", LoggingConfig())
    private val expRunnerLogger = delegate.logger

    val modelConfig by ModelConfig()
    val serverConfig by LmServerConfig()
    val expLoggingConfig by ExpLoggingConfig()
    val promptConfig by PromptConfig()

    val runId: String? by option()
        .help("The UUID that identifies the experimental run.")

    val runTimeoutMillis: Long by option()
        .long()
        .default(DEFAULT_RUN_TIMEOUT)
        .help("Time in milliseconds before the termination of the run.")

    val replayExp: Boolean by option()
        .boolean()
        .default(false)
        .help("Whether to replay a past experiment or create a new one.")

    val expReplayPath: String? by option()
        .help(
            """
            Provide the path to the directory that holds the experiment log, 
            or to the specific file containing the chat to replay.
            """.trimIndent(),
        )

    init {
        JaktaKoin.loadAdditionalModules(modulesToLoad)
    }

    override fun run() {
        val runId = runId ?: UUID.randomUUID().toString()
        expRunnerLogger.info("Started experiment with run id: $runId")
        val logConfig = loggingConfigFactory.createLoggingConfig(runId, expLoggingConfig)
        expRunnerLogger.info(logConfig)

        val genStrategy =
            genStrategyFactory.createGenerationStrategy(
                serverConfig,
                modelConfig,
                promptConfig,
                replayExp,
                expReplayPath,
            )
        val mas = masFactory.createMas(logConfig, genStrategy, promptConfig.environmentType.config)
        expRunnerLogger.info("Shutting down in $runTimeoutMillis milliseconds")

        mas.start()
        Thread.sleep(runTimeoutMillis)
        mas.stop()
        expRunnerLogger.info("Terminated experiment with run id: $runId")
        exitProcess(0)
    }

    companion object {
        val urlRegex = "^https?://([\\w.-]+)(:\\d+)?(/.*)?$".toRegex()
        val DEFAULT_RUN_TIMEOUT = 2.minutes.toLong(DurationUnit.MILLISECONDS)
    }
}
// CPD-ON

package it.unibo.jakta.invoker

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.PGPSuccess
import it.unibo.jakta.agents.bdi.engine.generation.PgpID
import it.unibo.jakta.agents.bdi.engine.generation.manager.plangeneration.PlanGenerationResult
import it.unibo.jakta.agents.bdi.engine.goals.Achieve
import it.unibo.jakta.agents.bdi.engine.goals.GeneratePlan
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.loggers.LoggerFactory
import it.unibo.jakta.exp.GenerationStrategyFactory
import it.unibo.jakta.exp.LoggingConfigFactory
import it.unibo.jakta.exp.MasFactory
import it.unibo.jakta.exp.options.ExpLoggingConfig
import it.unibo.jakta.exp.options.LmServerConfig
import it.unibo.jakta.exp.options.ModelConfig
import it.unibo.jakta.exp.options.PromptConfig
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import org.koin.core.module.Module
import java.util.UUID

// TODO remove code duplication
// CPD-OFF
class LMInvoker(
    val masFactory: MasFactory,
    val loggingConfigFactory: LoggingConfigFactory,
    val genStrategyFactory: GenerationStrategyFactory,
    modulesToLoad: List<Module>,
) : CliktCommand() {
    private val delegate = LoggerFactory.create("LMInvoker", LoggingConfig())
    private val invokerLogger = delegate.logger

    val runId: String? by option()
        .help("The UUID that identifies the invocation run.")

    val dryRun: Boolean by option()
        .flag()
        .help("Whether to only log the prompt or also invoke the LM.")

    val modelConfig by ModelConfig()
    val serverConfig by LmServerConfig()
    val loggingConfig by ExpLoggingConfig()
    val promptConfig by PromptConfig()

    val initialGoal = GeneratePlan.of(Achieve.of(Struct.of("achieve", Atom.of("home"))))

    init {
        JaktaKoin.loadAdditionalModules(modulesToLoad)
    }

    override fun run() {
        val runId = runId ?: UUID.randomUUID().toString()
        invokerLogger.info("Started the language model invoker with id $runId")
        val logConfig = loggingConfigFactory.createLoggingConfig(runId, loggingConfig)
        invokerLogger.info(logConfig)

        val genStrategy = genStrategyFactory.createGenerationStrategy(serverConfig, modelConfig, promptConfig)
        val mas = masFactory.createMas(logConfig, genStrategy, promptConfig.environmentType.config)

        val externalActions =
            mas.environment.externalActions.values
                .toList()
        val explorerAgent = mas.agents.firstOrNull()
        val agentContext = explorerAgent?.context

        if (agentContext != null && genStrategy != null) {
            val generationState =
                genStrategy.initializeGeneration(
                    initialGoal,
                    agentContext,
                    externalActions,
                    mas.masID,
                    explorerAgent.agentID,
                    logConfig,
                )
            if (!dryRun) {
                val genResult = genStrategy.requestBlockingGeneration(generationState)
                if (genResult is PlanGenerationResult) {
                    val pgpId = PgpID()
                    explorerAgent.logger?.log {
                        PGPSuccess.GenerationCompleted(
                            pgpId,
                            initialGoal,
                            genResult.generatedPlanLibrary,
                            genResult.generatedAdmissibleGoals,
                            genResult.generatedAdmissibleBeliefs,
                        )
                    }
                }
            }
        }
    }
}
// CPD-ON

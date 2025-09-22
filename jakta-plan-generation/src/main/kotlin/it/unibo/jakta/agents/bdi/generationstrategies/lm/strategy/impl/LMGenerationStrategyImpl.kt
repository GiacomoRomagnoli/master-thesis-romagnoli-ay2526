package it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.impl

import it.unibo.jakta.agents.bdi.engine.AgentID
import it.unibo.jakta.agents.bdi.engine.MasID
import it.unibo.jakta.agents.bdi.engine.actions.ExternalAction
import it.unibo.jakta.agents.bdi.engine.context.AgentContext
import it.unibo.jakta.agents.bdi.engine.generation.GenerationConfig
import it.unibo.jakta.agents.bdi.engine.generation.GenerationResult
import it.unibo.jakta.agents.bdi.engine.generation.GenerationState
import it.unibo.jakta.agents.bdi.engine.generation.GenerationStrategy
import it.unibo.jakta.agents.bdi.engine.generation.PgpID
import it.unibo.jakta.agents.bdi.engine.goals.GeneratePlan
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationFailure
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationState
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.events.LMMessageSent
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.loggers.LMPGPLogger
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.generation.LMPlanGenerator
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.LMGenerationStrategy
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
internal class LMGenerationStrategyImpl(
    override val generator: LMPlanGenerator,
    override val generationConfig: LMGenerationConfig.LMGenerationConfigContainer,
) : LMGenerationStrategy {
    override fun requestBlockingGeneration(generationState: GenerationState): GenerationResult =
        runBlocking {
            val lmState = generationState as? LMGenerationState
            return@runBlocking if (lmState != null) {
                generator.generate(this@LMGenerationStrategyImpl, lmState)
            } else {
                LMGenerationFailure(generationState, LMGenerationStrategy.configErrorMsg(generationState))
            }
        }

    override fun initializeGeneration(
        initialGoal: GeneratePlan,
        context: AgentContext,
        externalActions: List<ExternalAction>,
        masID: MasID?,
        agentID: AgentID?,
        loggingConfig: LoggingConfig?,
    ): GenerationState {
        val systemMsg =
            generationConfig.systemPromptBuilder?.build(
                initialGoal,
                context,
                externalActions,
                generationConfig.contextFilters,
            )
        val userMsg =
            generationConfig.userPromptBuilder.build(
                initialGoal,
                context,
                externalActions,
                generationConfig.contextFilters,
            )

        val name = RandomNameGenerator().randomName()
        val pgpID = PgpID(name = name)
        val logger =
            loggingConfig?.let { cfg ->
                if (masID != null && agentID != null) {
                    LMPGPLogger.create(masID, agentID, pgpID, cfg)
                } else {
                    null
                }
            }

        return LMGenerationState(
            pgpID = pgpID,
            goal = initialGoal,
            logger = logger,
            chatHistory = listOfNotNull(systemMsg, userMsg),
        ).also {
            systemMsg?.let { logger?.log { LMMessageSent(systemMsg) } }
            logger?.log { LMMessageSent(userMsg) }
        }
    }

    override fun updateGenerationConfig(generationConfig: GenerationConfig): GenerationStrategy =
        when (generationConfig) {
            is LMGenerationConfig.LMGenerationConfigUpdate ->
                LMGenerationStrategy.of(generationConfig.patch(this.generationConfig))
            else -> this
        }

    override fun toString(): String =
        "LMGenerationStrategyImpl(" +
            "planGenerator=$generator, " +
            "generationConfig=$generationConfig)"
}

package it.unibo.jakta.agents.bdi.engine.generation.manager.plangeneration.impl

import it.unibo.jakta.agents.bdi.engine.context.AgentContext
import it.unibo.jakta.agents.bdi.engine.environment.Environment
import it.unibo.jakta.agents.bdi.engine.executionstrategies.ExecutionResult
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.PGPSuccess
import it.unibo.jakta.agents.bdi.engine.generation.GenerationFailureResult
import it.unibo.jakta.agents.bdi.engine.generation.GenerationStrategy
import it.unibo.jakta.agents.bdi.engine.generation.PgpID
import it.unibo.jakta.agents.bdi.engine.generation.manager.plangeneration.GeneratePlanStrategy
import it.unibo.jakta.agents.bdi.engine.generation.manager.plangeneration.GenerationResultBuilder
import it.unibo.jakta.agents.bdi.engine.generation.manager.plangeneration.PlanGenerationResult
import it.unibo.jakta.agents.bdi.engine.goals.GeneratePlan
import it.unibo.jakta.agents.bdi.engine.intentions.Intention
import it.unibo.jakta.agents.bdi.engine.logging.LoggingConfig
import it.unibo.jakta.agents.bdi.engine.logging.loggers.AgentLogger

internal class GeneratePlanStrategyImpl(
    private val genResProcessor: GenerationResultBuilder,
    override val loggingConfig: LoggingConfig?,
    override val logger: AgentLogger?,
) : GeneratePlanStrategy {
    private val stateManager = GenerationStateInitializer(loggingConfig, logger)
    private val errorHandler = GenerationErrorHandler(logger)

    override fun generatePlan(
        genGoal: GeneratePlan,
        intention: Intention,
        generationStrategy: GenerationStrategy,
        context: AgentContext,
        environment: Environment,
    ): ExecutionResult {
        val updatedGenerationStrategy =
            genGoal.generationConfig
                ?.let { generationStrategy.updateGenerationConfig(it) }
                ?: generationStrategy

        val generationState =
            stateManager.setupGenerationStart(
                context,
                updatedGenerationStrategy,
                genGoal,
                environment,
            )

        /*
         * Request the plan generation.
         */
        return when (val planGenResult = updatedGenerationStrategy.requestBlockingGeneration(generationState)) {
            is GenerationFailureResult -> errorHandler.handleFailure(intention, context, planGenResult)
            is PlanGenerationResult ->
                processPlanGenerationResult(
                    generationState.pgpID,
                    genGoal,
                    intention,
                    context,
                    planGenResult,
                )
            else -> errorHandler.handleUnknownResult(intention, context)
        }
    }

    private fun processPlanGenerationResult(
        pgpID: PgpID,
        genGoal: GeneratePlan,
        intention: Intention,
        context: AgentContext,
        planGenResult: PlanGenerationResult,
    ): ExecutionResult =
        genResProcessor
            .buildResult(
                genGoal,
                context,
                intention,
                planGenResult,
            ).copy(
                feedback =
                    PGPSuccess.GenerationCompleted(
                        pgpID,
                        genGoal,
                        planGenResult.generatedPlanLibrary,
                        planGenResult.generatedAdmissibleGoals,
                        planGenResult.generatedAdmissibleBeliefs,
                    ),
            )
}

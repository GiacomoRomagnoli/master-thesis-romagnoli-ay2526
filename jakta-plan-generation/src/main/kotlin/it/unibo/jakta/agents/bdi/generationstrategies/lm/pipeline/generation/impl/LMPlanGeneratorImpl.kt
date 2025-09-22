package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.generation.impl

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import it.unibo.jakta.agents.bdi.engine.generation.GenerationResult
import it.unibo.jakta.agents.bdi.engine.goals.GeneratePlan
import it.unibo.jakta.agents.bdi.engine.plans.PartialPlan
import it.unibo.jakta.agents.bdi.engine.plans.Plan
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationFailure
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationResult
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationState
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.events.LMMessageReceived
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.events.LMMessageSent
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.generation.LMPlanGenerator
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.Parser
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.result.ParserFailure
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.result.ParserSuccess
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.RequestHandler
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.result.RequestFailure
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.result.RequestResult
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.result.RequestSuccess
import it.unibo.jakta.agents.bdi.generationstrategies.lm.strategy.LMGenerationStrategy
import kotlin.collections.isNotEmpty

internal class LMPlanGeneratorImpl(
    override val requestHandler: RequestHandler,
    override val responseParser: Parser,
) : LMPlanGenerator {
    override fun handleRequestResult(
        generationStrategy: LMGenerationStrategy,
        requestResult: RequestResult,
        generationState: LMGenerationState,
    ): GenerationResult =
        when (requestResult) {
            is RequestSuccess.NewRequestResult -> {
                val chatMessage = ChatMessage(ChatRole.Assistant, requestResult.parserResult.rawContent)
                val updatedState =
                    generationState
                        .copy(
                            chatHistory = generationState.chatHistory + chatMessage,
                        ).also {
                            generationState.logger?.log {
                                LMMessageReceived(requestResult.chatCompletionId, chatMessage)
                            }
                        }

                when (val parserRes = requestResult.parserResult) {
                    is ParserFailure.EmptyResponse -> handleEmptyResponse(updatedState)
                    is ParserFailure -> handleParserFailure(updatedState)
                    is ParserSuccess.NewResult -> handleNewResult(updatedState, parserRes)
                }
            }
            is RequestFailure.NetworkRequestFailure -> handleRequestFailure(generationState, requestResult)
        }

    private fun handleNewResult(
        generationState: LMGenerationState,
        res: ParserSuccess.NewResult,
    ): GenerationResult {
        val newPlans = res.plans.let { plans -> plans.mapNotNull { handleNewPlan(generationState.goal, it) } }
        return LMGenerationResult(generationState, newPlans, res.admissibleGoals, res.admissibleBeliefs)
    }

    private fun handleNewPlan(
        initialGoal: GeneratePlan,
        res: Plan,
    ): PartialPlan? =
        if (res.goals.isNotEmpty()) {
            PartialPlan.of(
                trigger = res.trigger,
                goals = res.goals,
                guard = res.guard,
                parentGenerationGoal = initialGoal,
            )
        } else {
            null
        }

    private fun handleEmptyResponse(generationState: LMGenerationState): GenerationResult =
        LMGenerationFailure(
            generationState = generationState,
            errorMsg = "Empty response",
        )

    private fun handleRequestFailure(
        generationState: LMGenerationState,
        res: RequestFailure.NetworkRequestFailure,
    ): GenerationResult =
        LMGenerationFailure(
            generationState = generationState,
            errorMsg = res.rawContent,
        )

    fun handleParserFailure(generationState: LMGenerationState): GenerationResult {
        val errorMsg = "Failed parsing"
        val newMessage = ChatMessage(ChatRole.User, errorMsg)
        return LMGenerationFailure(
            generationState =
                generationState.copy(
                    chatHistory = generationState.chatHistory + newMessage,
                ),
            errorMsg = errorMsg,
        ).also {
            generationState.logger?.log { LMMessageSent(newMessage) }
        }
    }
}

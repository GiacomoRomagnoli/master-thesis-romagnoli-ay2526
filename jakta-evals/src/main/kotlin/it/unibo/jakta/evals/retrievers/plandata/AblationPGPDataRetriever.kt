package it.unibo.jakta.evals.retrievers.plandata

import com.aallam.openai.api.chat.ChatMessage
import it.unibo.jakta.agents.bdi.engine.FileUtils.processLog
import it.unibo.jakta.agents.bdi.engine.actions.effects.BeliefChange
import it.unibo.jakta.agents.bdi.engine.beliefs.AdmissibleBelief
import it.unibo.jakta.agents.bdi.engine.context.ContextUpdate
import it.unibo.jakta.agents.bdi.engine.events.AdmissibleGoal
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.PGPSuccess
import it.unibo.jakta.agents.bdi.engine.plans.Plan
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.events.LMGenerationRequested
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.events.LMMessageReceived
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.events.LMMessageSent
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.Parser
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.result.ParserFailure
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.result.ParserSuccess
import java.io.File

class AblationPGPDataRetriever(
    masLogFile: File,
    agentLogFile: File,
    pgpLogFile: File,
    private val pgpId: String,
) : AbstractPGPDataRetriever(masLogFile, agentLogFile, pgpLogFile, pgpId) {
    override fun buildPGPInvocation(
        agentLogStream: File,
        pgpLogStream: File,
    ): PGPInvocation {
        val history = mutableListOf<ChatMessage>()
        val rawMessageContents = mutableListOf<String>()
        var genCfg: LMGenerationConfig? = null
        var chatCompletionId: String? = null

        processLog(pgpLogStream) { logEntry ->
            when (val event = logEntry.message.event) {
                is LMMessageReceived -> {
                    chatCompletionId = event.chatCompletionId
                    event.chatMessage.content?.let { rawMessageContents.add(it) }
                    history.add(event.chatMessage)
                }
                is LMMessageSent -> history.add(event.chatMessage)
                is LMGenerationRequested -> genCfg = event.genConfig
            }
            true
        }

        var plansNotParsed = 0
        var admissibleGoalsNotParsed = 0
        var admissibleBeliefNotParsed = 0
        val parser = Parser.create()
        rawMessageContents.forEach {
            when (val result = parser.parse(it)) {
                is ParserSuccess.NewResult -> {
                    if (result.parsingErrors.isNotEmpty()) {
                        plansNotParsed++
                        result.parsingErrors.forEach { error ->
                            when (error) {
                                is ParserFailure.AdmissibleBeliefParseFailure -> admissibleGoalsNotParsed++
                                is ParserFailure.AdmissibleGoalParseFailure -> admissibleBeliefNotParsed++
                                else -> {}
                            }
                        }
                    }
                }
                else -> plansNotParsed++
            }
        }

        var reachesDestination = false
        var timeOfCompletion: Long? = 0L
        val generatedPlans = mutableListOf<Plan>()
        val generatedAdmissibleGoals = mutableListOf<AdmissibleGoal>()
        val generatedAdmissibleBeliefs = mutableListOf<AdmissibleBelief>()

        processLog(agentLogStream) { logEntry ->
            when (val ev = logEntry.message.event) {
                is PGPSuccess.GenerationCompleted -> {
                    generatedPlans.addAll(ev.plans)
                    generatedAdmissibleGoals.addAll(ev.admissibleGoals)
                    generatedAdmissibleBeliefs.addAll(ev.admissibleBeliefs)
                }
                is BeliefChange -> {
                    if (ev.changeType == ContextUpdate.ADDITION &&
                        ev.belief.rule.head.functor == "reached" &&
                        ev.belief.rule.headArgs
                            .firstOrNull()
                            ?.asAtom()
                            ?.functor == "home"
                    ) {
                        reachesDestination = true
                        timeOfCompletion = logEntry.message.cycleCount
                    }
                }
            }
            true
        }

        return PGPInvocation(
            pgpId = pgpId,
            history = history,
            rawMessageContents = rawMessageContents,
            generatedPlans = generatedPlans,
            generatedAdmissibleGoals = generatedAdmissibleGoals,
            generatedAdmissibleBeliefs = generatedAdmissibleBeliefs,
            plansNotParsed = plansNotParsed,
            admissibleGoalsNotParsed = admissibleGoalsNotParsed,
            admissibleBeliefNotParsed = admissibleBeliefNotParsed,
            completionTime = timeOfCompletion,
            executable = generatedPlans.isNotEmpty(),
            achievesGoal = reachesDestination,
            generationConfig = genCfg,
            chatCompletionId = chatCompletionId,
        )
    }
}

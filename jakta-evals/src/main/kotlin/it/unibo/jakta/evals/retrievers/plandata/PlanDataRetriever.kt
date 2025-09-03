package it.unibo.jakta.evals.retrievers.plandata

import com.aallam.openai.api.chat.ChatMessage
import it.unibo.jakta.agents.bdi.engine.FileUtils.processFile
import it.unibo.jakta.agents.bdi.engine.actions.ActionSignature
import it.unibo.jakta.agents.bdi.engine.actions.effects.AdmissibleBeliefChange
import it.unibo.jakta.agents.bdi.engine.actions.effects.AdmissibleGoalChange
import it.unibo.jakta.agents.bdi.engine.actions.effects.BeliefChange
import it.unibo.jakta.agents.bdi.engine.actions.effects.PlanChange
import it.unibo.jakta.agents.bdi.engine.beliefs.AdmissibleBelief
import it.unibo.jakta.agents.bdi.engine.context.ContextUpdate
import it.unibo.jakta.agents.bdi.engine.events.AdmissibleGoal
import it.unibo.jakta.agents.bdi.engine.executionstrategies.feedback.PGPSuccess
import it.unibo.jakta.agents.bdi.engine.logging.events.ActionEvent
import it.unibo.jakta.agents.bdi.engine.plans.Plan
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.events.LMGenerationRequested
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.events.LMMessageReceived
import it.unibo.jakta.agents.bdi.generationstrategies.lm.logging.events.LMMessageSent
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.Parser
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.result.ParserFailure
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.result.ParserSuccess
import it.unibo.jakta.evals.retrievers.Retriever
import java.io.File

// TODO detect plans not subsumed by others that are never executed (using TrackGoalExecution)
class PlanDataRetriever(
    private val masLogFile: File,
    private val agentLogFile: File,
    private val pgpLogFile: File,
    private val masId: String,
    private val agentId: String,
    private val pgpId: String,
) : Retriever<PlanData> {
    override fun retrieve(): PlanData {
        val invocationContext = buildInvocationContext(masLogFile, agentLogFile)
        val pgpInvocation = buildPGPInvocation(pgpId, agentLogFile, pgpLogFile)
        return PlanData(invocationContext, pgpInvocation)
    }

    private fun buildInvocationContext(
        masLogFile: File,
        agentLogFile: File,
    ): InvocationContext {
        val actions = mutableListOf<ActionSignature>()

        processFile(masLogFile) { logEntry ->
            val event = logEntry.message.event
            if (event is ActionEvent.ActionAddition) {
                event.action?.let { actions.add(it.actionSignature) }
            }
            true
        }

        val plans = mutableListOf<Plan>()
        val admissibleGoals = mutableListOf<AdmissibleGoal>()
        val admissibleBeliefs = mutableListOf<AdmissibleBelief>()

        processFile(agentLogFile) { logEntry ->
            when (val ev = logEntry.message.event) {
                is ActionEvent.ActionAddition -> actions.add(ev.actionSignature)
                is PlanChange if ev.changeType == ContextUpdate.ADDITION -> plans.add(ev.plan)
                is AdmissibleGoalChange if ev.changeType == ContextUpdate.ADDITION -> admissibleGoals.add(ev.goal)
                is AdmissibleBeliefChange if ev.changeType == ContextUpdate.ADDITION -> admissibleBeliefs.add(ev.belief)
            }
            true
        }

        return InvocationContext(
            masId = masId,
            agentId = agentId,
            plans = plans,
            admissibleGoals = admissibleGoals,
            admissibleBeliefs = admissibleBeliefs,
            actions = actions,
        )
    }

    private fun buildPGPInvocation(
        pgpId: String,
        agentLogStream: File,
        pgpLogStream: File,
    ): PGPInvocation {
        val history = mutableListOf<ChatMessage>()
        val rawMessageContents = mutableListOf<String>()
        var genCfg: LMGenerationConfig? = null
        var chatCompletionId: String? = null

        processFile(pgpLogStream) { logEntry ->
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

        processFile(agentLogStream) { logEntry ->
            when (val ev = logEntry.message.event) {
                is PGPSuccess.GenerationCompleted -> {
                    if (ev.pgpId.id == pgpId) {
                        generatedPlans.addAll(ev.plans)
                        generatedAdmissibleGoals.addAll(ev.admissibleGoals)
                        generatedAdmissibleBeliefs.addAll(ev.admissibleBeliefs)
                    }
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
            timeUntilCompletion = timeOfCompletion,
            executable = generatedPlans.isNotEmpty(),
            reachesDestination = reachesDestination,
            generationConfig = genCfg,
            chatCompletionId = chatCompletionId,
        )
    }
}

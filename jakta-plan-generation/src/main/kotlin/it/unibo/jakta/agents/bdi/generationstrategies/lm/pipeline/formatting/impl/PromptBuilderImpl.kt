package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.impl

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import it.unibo.jakta.agents.bdi.engine.actions.ExternalAction
import it.unibo.jakta.agents.bdi.engine.context.AgentContext
import it.unibo.jakta.agents.bdi.engine.goals.GeneratePlan
import it.unibo.jakta.agents.bdi.generationstrategies.lm.Remark
import it.unibo.jakta.agents.bdi.generationstrategies.lm.dsl.AgentContextProperties
import it.unibo.jakta.agents.bdi.generationstrategies.lm.dsl.PromptScope
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering.ContextFilter
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering.ContextFilter.Companion.applyAllTo
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering.ExtendedAgentContext
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.PromptBuilder

internal class PromptBuilderImpl(
    private val role: ChatRole,
    private val block: PromptScope.(AgentContextProperties) -> Unit,
) : PromptBuilder {
    override fun build(
        initialGoal: GeneratePlan,
        context: AgentContext,
        externalActions: List<ExternalAction>,
        contextFilters: List<ContextFilter>,
        remarks: Iterable<Remark>,
    ): ChatMessage {
        val properties = createAgentContextProperties(initialGoal, context, externalActions, contextFilters, remarks)
        val scope = PromptScope().apply { block(properties) }
        return scope.buildAsMessage(role)
    }

    companion object {
        private fun createAgentContextProperties(
            initialGoal: GeneratePlan,
            context: AgentContext,
            externalActions: List<ExternalAction>,
            contextFilters: List<ContextFilter>,
            remarks: Iterable<Remark>,
        ): AgentContextProperties {
            val extendedContext = ExtendedAgentContext(initialGoal, context, externalActions)
            val filtered = contextFilters.applyAllTo(extendedContext)
            return AgentContextProperties(
                internalActions =
                    filtered.context.internalActions.values
                        .toList(),
                externalActions = filtered.externalActions,
                beliefs = filtered.context.beliefBase,
                admissibleBeliefs = filtered.context.admissibleBeliefs,
                admissibleGoals = filtered.context.admissibleGoals,
                goals = filtered.context.planLibrary.plans,
                initialGoal = initialGoal,
                remarks = remarks,
            )
        }
    }
}

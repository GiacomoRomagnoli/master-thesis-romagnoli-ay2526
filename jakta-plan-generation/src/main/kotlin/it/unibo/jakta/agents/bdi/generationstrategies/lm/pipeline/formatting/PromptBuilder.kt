package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting

import com.aallam.openai.api.chat.ChatMessage
import it.unibo.jakta.agents.bdi.engine.actions.ExternalAction
import it.unibo.jakta.agents.bdi.engine.context.AgentContext
import it.unibo.jakta.agents.bdi.engine.goals.GeneratePlan
import it.unibo.jakta.agents.bdi.generationstrategies.lm.Remark
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering.ContextFilter

interface PromptBuilder {
    fun build(
        initialGoal: GeneratePlan,
        context: AgentContext,
        externalActions: List<ExternalAction> = emptyList(),
        contextFilters: List<ContextFilter> = emptyList(),
        remarks: Iterable<Remark> = emptyList(),
    ): ChatMessage

    companion object {
        fun <T> formatAsBulletList(
            items: Iterable<T>,
            formatter: (T) -> String?,
        ): String? {
            val formattedItems = items.mapNotNull(formatter).filter { it.isNotBlank() }
            return formattedItems.takeIf { it.isNotEmpty() }?.joinToString("\n") { "- $it" }
        }
    }
}

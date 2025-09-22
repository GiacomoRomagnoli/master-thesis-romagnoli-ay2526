package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.impl

import com.aallam.openai.api.chat.ChatRole
import it.unibo.jakta.agents.bdi.generationstrategies.lm.dsl.AgentContextProperties
import it.unibo.jakta.agents.bdi.generationstrategies.lm.dsl.PromptScope
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.PromptBuilder
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.SystemPromptBuilder

internal class SystemPromptBuilderImpl(
    block: PromptScope.(AgentContextProperties) -> Unit,
) : SystemPromptBuilder,
    PromptBuilder by PromptBuilderImpl(
        role = ChatRole.System,
        block = block,
    ) {
    companion object {
        fun system(block: PromptScope.(AgentContextProperties) -> Unit): SystemPromptBuilder =
            SystemPromptBuilderImpl(block)
    }
}

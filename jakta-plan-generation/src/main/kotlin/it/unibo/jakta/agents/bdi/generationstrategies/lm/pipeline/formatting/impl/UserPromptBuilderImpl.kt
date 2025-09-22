package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.impl

import com.aallam.openai.api.chat.ChatRole
import it.unibo.jakta.agents.bdi.generationstrategies.lm.dsl.AgentContextProperties
import it.unibo.jakta.agents.bdi.generationstrategies.lm.dsl.PromptScope
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.PromptBuilder
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.UserPromptBuilder

internal class UserPromptBuilderImpl(
    block: PromptScope.(AgentContextProperties) -> Unit,
) : UserPromptBuilder,
    PromptBuilder by PromptBuilderImpl(
        role = ChatRole.User,
        block = block,
    ) {
    companion object {
        fun user(block: PromptScope.(AgentContextProperties) -> Unit): UserPromptBuilder = UserPromptBuilderImpl(block)
    }
}

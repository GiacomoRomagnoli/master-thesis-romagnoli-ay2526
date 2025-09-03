package it.unibo.jakta.exp.prompt

import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.DefaultPromptBuilder
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.SystemPromptBuilder

enum class SystemPromptType(
    val builder: SystemPromptBuilder,
) {
    PROMPT_WITHOUT_BDI_AGENT_DEFINITION(DefaultPromptBuilder.defaultSystemPrompt),
}

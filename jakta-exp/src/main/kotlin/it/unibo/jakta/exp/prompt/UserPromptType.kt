package it.unibo.jakta.exp.prompt

import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.DefaultPromptBuilder
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.UserPromptBuilder

enum class UserPromptType(
    val builder: UserPromptBuilder,
) {
    PROMPT_WITH_HINTS_AND_REMARKS(DefaultPromptBuilder.userPromptWithHintsAndRemarks),
    PROMPT_WITH_HINTS(DefaultPromptBuilder.userPromptWithHints),
    PROMPT_WITHOUT_HINTS(DefaultPromptBuilder.userPromptWithoutHints),
}

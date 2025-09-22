package it.unibo.jakta.agents.bdi.generationstrategies.lm

import com.aallam.openai.api.chat.Effort

/**
 * OpenAI-style reasoning effort levels supported by OpenRouter.
 *
 * See https://openrouter.ai/docs/use-cases/reasoning-tokens#reasoning-effort-level.
 */
enum class ReasoningEffort(
    val effort: Effort,
) {
    High(Effort("high")),
    Medium(Effort("medium")),
    Low(Effort("low")),
    Minimal(Effort("minimal")),
}

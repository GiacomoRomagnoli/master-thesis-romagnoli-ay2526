package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting

import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_EXPLANATION_LEVEL
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_PROMPT_TECHNIQUE
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_SYNTAX_IS_ASL
import it.unibo.jakta.agents.bdi.generationstrategies.lm.ExplanationLevel
import it.unibo.jakta.agents.bdi.generationstrategies.lm.PromptTechnique
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.DefaultPromptBuilderSnippetsPath.DEFAULT_ASL_OUTPUT_FORMAT
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.DefaultPromptBuilderSnippetsPath.DEFAULT_BDI_AGENT_DEF_PROMPT_DIR
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.DefaultPromptBuilderSnippetsPath.DEFAULT_FEW_SHOT_PROMPT_DIR
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.DefaultPromptBuilderSnippetsPath.DEFAULT_PROMPT_DIR
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.DefaultPromptBuilderSnippetsPath.DEFAULT_YAML_OUTPUT_FORMAT
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.impl.SystemPromptBuilderImpl.Companion.system

interface SystemPromptBuilder : PromptBuilder {
    companion object {
        fun createSystemPrompt(
            aslSyntaxExplanationLevel: ExplanationLevel = DEFAULT_EXPLANATION_LEVEL,
            withBdiAgentDefinition: Boolean = false,
            fewShot: Boolean = false,
            promptTechnique: PromptTechnique = DEFAULT_PROMPT_TECHNIQUE,
            useAslSyntax: Boolean = DEFAULT_SYNTAX_IS_ASL,
            promptSnippetsPath: String = DEFAULT_PROMPT_DIR,
        ) = system {
            section("System Message") {
                if (withBdiAgentDefinition) {
                    fromString("You are a Belief-Desire-Intention (BDI) agent that devises plans to pursue goals.")
                } else {
                    fromFile(promptSnippetsPath + DEFAULT_BDI_AGENT_DEF_PROMPT_DIR)
                }

                section("Core Principles") {
                    fromString(
                        """
                        - The more general the plan, the better
                        - Encode beliefs as first-order-logic (FOL) facts
                        - Encode goals as FOL terms
                        - Encode plans as triplets: (event, condition, operation)
                        """.trimIndent(),
                    )
                }

                section("Plan Structure") {
                    fromString(
                        """
                        Plans have the format (event, condition, operation) where:
                        - **event**: the goal to be pursued
                        - **condition**: FOL formula tested against current beliefs
                        - **operation**: list of activities to perform
                        """.trimIndent(),
                    )
                }

                section("Event Types") {
                    fromString(
                        """
                        Events must be prefixed with keywords:
                        - `achieve`: goals the agent should actively work towards (e.g., "achieve reach(home)")
                        """.trimIndent(),
                    )
                }

                section("Operation Types") {
                    fromString(
                        """
                        Operations must be prefixed with keywords:
                        - `execute`: primitive actions that directly interact with environment (e.g., `execute move(north)`)
                        - `achieve`: set new subgoal that triggers another plan (e.g., `achieve reach(rock)`)
                        - `add`: add new belief to belief base (e.g., `add visited(current_location)`)
                        - `remove`: remove existing belief (e.g., `remove obstacle(north)`)
                        - `update`: modify existing belief (e.g., `update position(X, Y)`)
                        """.trimIndent(),
                    )
                }

                // TODO
                section("Output Format") {
                    if (promptTechnique != PromptTechnique.CoTMulti) {
                        if (useAslSyntax) {
                            when (aslSyntaxExplanationLevel) {
                                ExplanationLevel.Standard -> fromFile(promptSnippetsPath + DEFAULT_ASL_OUTPUT_FORMAT)
                                ExplanationLevel.Detailed -> TODO()
                            }
                        } else {
                            fromFile(promptSnippetsPath + DEFAULT_YAML_OUTPUT_FORMAT)
                        }
                    } else {
                        fromString(
                            """
                            Provide your output in the following text format:
                            Step by step reasoning: ...
                            Answer: The final answer is ...
                            """.trimIndent(),
                        )
                    }
                }

                if (fewShot) {
                    section("Example") {
                        fromFile(promptSnippetsPath + DEFAULT_FEW_SHOT_PROMPT_DIR)
                    }
                }

                section("Constraints") {
                    fromString(
                        """
                        - Use FOL syntax with no quantifiers
                        - Be as general and minimal as possible
                        - Use variables instead of constants where appropriate
                        - Reuse patterns across plans
                        - Cannot invent new admissible actions
                        - Must use all invented admissible goals/beliefs
                        - Cannot reference existing admissible goals/beliefs when inventing new ones
                        """.trimIndent(),
                    )
                }
            }
        }
    }
}

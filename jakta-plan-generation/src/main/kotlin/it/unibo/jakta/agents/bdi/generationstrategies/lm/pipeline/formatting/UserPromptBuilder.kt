package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting

import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.actionsFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.admissibleBeliefsFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.admissibleBeliefsFormatterWithoutHints
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.admissibleGoalsFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.beliefsFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.goalFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.triggerFormatter
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_EXPLANATION_LEVEL
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_PROMPT_TECHNIQUE
import it.unibo.jakta.agents.bdi.generationstrategies.lm.ExplanationLevel
import it.unibo.jakta.agents.bdi.generationstrategies.lm.PromptTechnique
import it.unibo.jakta.agents.bdi.generationstrategies.lm.Remark
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.PromptBuilder.Companion.formatAsBulletList
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.impl.UserPromptBuilderImpl.Companion.user

interface UserPromptBuilder : PromptBuilder {
    companion object {
        fun createUserPrompt(
            withoutAdmissibleBeliefs: Boolean = false,
            withoutAdmissibleGoals: Boolean = false,
            withoutLogicDescription: Boolean = false,
            withoutNlDescription: Boolean = false,
            promptTechnique: PromptTechnique = DEFAULT_PROMPT_TECHNIQUE,
            expectedResultExplanationLevel: ExplanationLevel = DEFAULT_EXPLANATION_LEVEL,
            remarks: List<Remark> = emptyList(),
        ) = user { ctx ->
            section("User Message") {
                fromString("Below is your internal state and the specific goal I need you to plan for.")

                section("Agent's internal state") {
                    section("Beliefs") {
                        // TODO
                        if (!withoutAdmissibleBeliefs) {
                            section("Admissible beliefs") {
                                if (withoutNlDescription) {
                                    fromFormatter(ctx.admissibleBeliefs) {
                                        formatAsBulletList(it, admissibleBeliefsFormatterWithoutHints::format)
                                    }
                                } else if (withoutLogicDescription) {
                                    fromFormatter(ctx.admissibleBeliefs) {
                                        formatAsBulletList(it, admissibleBeliefsFormatter::format)
                                    }
                                } else {
                                    fromFormatter(ctx.admissibleBeliefs) {
                                        formatAsBulletList(it, admissibleBeliefsFormatter::format)
                                    }
                                }
                            }
                        }

                        section("Actual beliefs") {
                            // TODO
                            if (withoutNlDescription) {
                                fromFormatter(ctx.beliefs.asIterable().toList()) {
                                    formatAsBulletList(it, beliefsFormatter::format)
                                }
                            } else if (withoutLogicDescription) {
                                fromFormatter(ctx.beliefs.asIterable().toList()) {
                                    formatAsBulletList(it, beliefsFormatter::format)
                                }
                            } else {
                                fromFormatter(ctx.beliefs.asIterable().toList()) {
                                    formatAsBulletList(it, beliefsFormatter::format)
                                }
                            }
                        }
                    }

                    section("Goals") {
                        if (!withoutAdmissibleGoals) {
                            // TODO
                            section("Admissible goals") {
                                if (withoutNlDescription) {
                                    fromFormatter(ctx.admissibleGoals) {
                                        formatAsBulletList(it, admissibleGoalsFormatter::format)
                                    }
                                } else if (withoutLogicDescription) {
                                    fromFormatter(ctx.admissibleGoals) {
                                        formatAsBulletList(it, admissibleGoalsFormatter::format)
                                    }
                                } else {
                                    fromFormatter(ctx.admissibleGoals) {
                                        formatAsBulletList(it, admissibleGoalsFormatter::format)
                                    }
                                }
                            }
                        }

                        section("Actual goals") {
                            // TODO
                            if (withoutNlDescription) {
                                fromFormatter(ctx.goals) { plans ->
                                    val triggers = plans.map { it.trigger }
                                    formatAsBulletList(triggers, triggerFormatter::format)
                                }
                            } else if (withoutLogicDescription) {
                                fromFormatter(ctx.goals) { plans ->
                                    val triggers = plans.map { it.trigger }
                                    formatAsBulletList(triggers, triggerFormatter::format)
                                }
                            } else {
                                fromFormatter(ctx.goals) { plans ->
                                    val triggers = plans.map { it.trigger }
                                    formatAsBulletList(triggers, triggerFormatter::format)
                                }
                            }
                        }
                    }

                    section("Admissible actions") {
                        // TODO
                        if (withoutNlDescription) {
                            fromFormatter(ctx.internalActions + ctx.externalActions) {
                                formatAsBulletList(it, actionsFormatter::format)
                            }
                        } else if (withoutLogicDescription) {
                            fromFormatter(ctx.internalActions + ctx.externalActions) {
                                formatAsBulletList(it, actionsFormatter::format)
                            }
                        } else {
                            fromFormatter(ctx.internalActions + ctx.externalActions) {
                                formatAsBulletList(it, actionsFormatter::format)
                            }
                        }
                    }

                    if (remarks.isNotEmpty()) {
                        section("Remarks") {
                            fromFormatter(ctx.remarks) { r ->
                                formatAsBulletList(r) { it.value }
                            }
                        }
                    }
                }

                // TODO I1 / I4
                section("Expected outcome") {
                    val formattedGoal = goalFormatter.format(ctx.initialGoal.goal)
                    fromString("Create plans to pursue the goal: $formattedGoal.")
                    fromString(
                        """
                        
                        End with an additional YAML block that contains a list of any new admissible goals and beliefs you invented, including their natural language interpretation.
                        """.trimIndent(),
                    )
                }

                // TODO
                when (promptTechnique) {
                    PromptTechnique.CoT ->
                        fromString(
                            """
                            Let's think step by step.
                            """.trimIndent(),
                        )
                    PromptTechnique.NoCoT ->
                        fromString(
                            """
                            Output only the final set of plans with no alternatives or intermediate attempts.
                            """.trimIndent(),
                        )
                    PromptTechnique.CoTMulti -> {}
                }
            }
        }
    }
}

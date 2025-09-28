package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting

import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.actionsFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.actionsFormatterOnlyHints
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.actionsFormatterWithoutHints
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.admissibleBeliefsFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.admissibleBeliefsFormatterOnlyHints
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.admissibleBeliefsFormatterWithoutHints
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.admissibleGoalsFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.admissibleGoalsFormatterOnlyHints
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.admissibleGoalsFormatterWithoutHints
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.beliefsFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.beliefsFormatterOnlyHints
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.beliefsFormatterWithoutHints
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.goalFormatter
import it.unibo.jakta.agents.bdi.engine.formatters.DefaultFormatters.triggerFormatter
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_EXPLANATION_LEVEL
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_PROMPT_TECHNIQUE
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_SYNTAX_IS_ASL
import it.unibo.jakta.agents.bdi.generationstrategies.lm.ExplanationLevel
import it.unibo.jakta.agents.bdi.generationstrategies.lm.PromptTechnique
import it.unibo.jakta.agents.bdi.generationstrategies.lm.Remark
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.PromptBuilder.Companion.formatAsBulletList
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.impl.UserPromptBuilderImpl.Companion.user

interface UserPromptBuilder : PromptBuilder {
    companion object {
        fun createUserPrompt(
            withoutAdmissibleBeliefsAndGoals: Boolean = false,
            withoutLogicDescription: Boolean = false,
            withoutNlDescription: Boolean = false,
            promptTechnique: PromptTechnique = DEFAULT_PROMPT_TECHNIQUE,
            useAslSyntax: Boolean = DEFAULT_SYNTAX_IS_ASL,
            expectedResultExplanationLevel: ExplanationLevel = DEFAULT_EXPLANATION_LEVEL,
            remarks: List<Remark> = emptyList(),
        ) = user { ctx ->
            section("User Message") {
                fromString("Below is your internal state and the specific goal I need you to plan for.")

                section("Agent's internal state") {
                    section("Beliefs") {
                        if (!withoutAdmissibleBeliefsAndGoals) {
                            section("Admissible beliefs") {
                                if (withoutNlDescription) {
                                    fromFormatter(ctx.admissibleBeliefs) {
                                        formatAsBulletList(it, admissibleBeliefsFormatterWithoutHints::format)
                                    }
                                } else if (withoutLogicDescription) {
                                    fromFormatter(ctx.admissibleBeliefs) {
                                        formatAsBulletList(it, admissibleBeliefsFormatterOnlyHints::format)
                                    }
                                } else {
                                    fromFormatter(ctx.admissibleBeliefs) {
                                        formatAsBulletList(it, admissibleBeliefsFormatter::format)
                                    }
                                }
                            }
                        }

                        section("Actual beliefs") {
                            if (withoutNlDescription) {
                                fromFormatter(ctx.beliefs.asIterable().toList()) {
                                    formatAsBulletList(it, beliefsFormatterWithoutHints::format)
                                }
                            } else if (withoutLogicDescription) {
                                fromFormatter(ctx.beliefs.asIterable().toList()) {
                                    formatAsBulletList(it, beliefsFormatterOnlyHints::format)
                                }
                            } else {
                                fromFormatter(ctx.beliefs.asIterable().toList()) {
                                    formatAsBulletList(it, beliefsFormatter::format)
                                }
                            }
                        }
                    }

                    section("Goals") {
                        if (!withoutAdmissibleBeliefsAndGoals) {
                            section("Admissible goals") {
                                if (withoutNlDescription) {
                                    fromFormatter(ctx.admissibleGoals) {
                                        formatAsBulletList(it, admissibleGoalsFormatterWithoutHints::format)
                                    }
                                } else if (withoutLogicDescription) {
                                    fromFormatter(ctx.admissibleGoals) {
                                        formatAsBulletList(it, admissibleGoalsFormatterOnlyHints::format)
                                    }
                                } else {
                                    fromFormatter(ctx.admissibleGoals) {
                                        formatAsBulletList(it, admissibleGoalsFormatter::format)
                                    }
                                }
                            }
                        }

                        section("Actual goals") {
                            fromFormatter(ctx.goals) { plans ->
                                val triggers = plans.map { it.trigger }
                                formatAsBulletList(triggers, triggerFormatter::format)
                            }
                        }
                    }

                    section("Admissible actions") {
                        if (withoutNlDescription) {
                            fromFormatter(ctx.internalActions + ctx.externalActions) {
                                formatAsBulletList(it, actionsFormatterWithoutHints::format)
                            }
                        } else if (withoutLogicDescription) {
                            fromFormatter(ctx.internalActions + ctx.externalActions) {
                                formatAsBulletList(it, actionsFormatterOnlyHints::format)
                            }
                        } else {
                            fromFormatter(ctx.internalActions + ctx.externalActions) {
                                formatAsBulletList(it, actionsFormatter::format)
                            }
                        }
                    }

                    if (remarks.isNotEmpty() || !ctx.remarks.none()) {
                        section("Remarks") {
                            fromFormatter(remarks + ctx.remarks) { r ->
                                formatAsBulletList(r) { it.value }
                            }
                        }
                    }
                }

                section("Expected outcome") {
                    val formattedGoal = goalFormatter.format(ctx.initialGoal.goal)
                    fromString("Create plans to pursue the goal: $formattedGoal.")
                    if (!useAslSyntax) {
                        fromString(
                            """
                            
                            End with an additional YAML block that contains a list of any new admissible goals and beliefs you invented, including their natural language interpretation.
                            """.trimIndent(),
                        )
                    }
                }

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
                    PromptTechnique.CoTMulti -> {} // TODO
                }
            }
        }
    }
}

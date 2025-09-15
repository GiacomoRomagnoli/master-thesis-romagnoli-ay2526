package it.unibo.jakta.exp.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering.DefaultFilters.metaPlanFilter
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering.DefaultFilters.printActionFilter
import it.unibo.jakta.exp.explorer.CustomFilter.beliefBaseAdditionPlanFilter
import it.unibo.jakta.exp.gridworld.configuration.EnvironmentType

class PromptConfig : OptionGroup(name = "Prompt Configuration") {
    val withoutAdmissibleBeliefs: Boolean by option()
        .flag()
        .help("Whether to exclude or not admissible beliefs from the prompt.")

    val withoutAdmissibleGoals: Boolean by option()
        .flag()
        .help("Whether to exclude or not admissible goals from the prompt.")

    val expectedResultExplanationLevel: ExplanationLevel by option()
        .enum<ExplanationLevel>()
        .default(DEFAULT_EXPLANATION_LEVEL)
        .help("The level of detail with which the expected result is explained in the prompt.")

    val aslSyntaxExplanationLevel: ExplanationLevel by option()
        .enum<ExplanationLevel>()
        .default(DEFAULT_EXPLANATION_LEVEL)
        .help("The level of detail with which the AgentSpeak syntax is explained in the prompt.")

    val withBdiAgentDefinition: Boolean by option()
        .flag()
        .help("Whether to include or not a definition of what a BDI agent is in the prompt.")

    val fewShot: Boolean by option()
        .flag()
        .help("Whether to include or not plan generation examples in the prompt.")

    val withoutLogicDescription: Boolean by option()
        .flag()
        .help("Whether to exclude or not logic descriptions of beliefs and goals from the prompt.")

    val withoutNlDescription: Boolean by option()
        .flag()
        .help("Whether to exclude or not natural language descriptions of beliefs and goals from the prompt.")

    val promptTechnique: PromptTechnique by option()
        .enum<PromptTechnique>()
        .default(DEFAULT_PROMPT_TECHNIQUE)
        .help("The kind of technique to use for prompting.")

    val remarks: String? by option()
        .help("Path to the file that stores remarks to include in the prompt.")

    val environmentType: EnvironmentType by option()
        .enum<EnvironmentType>()
        .default(DEFAULT_ENV_TYPE)
        .help("The type of environment to use.")

    val contextFilters =
        listOf(
            metaPlanFilter,
            printActionFilter,
            beliefBaseAdditionPlanFilter,
        )

    companion object {
        val DEFAULT_PROMPT_TECHNIQUE = PromptTechnique.NoCoT
        val DEFAULT_EXPLANATION_LEVEL = ExplanationLevel.Standard
        val DEFAULT_ENV_TYPE = EnvironmentType.Standard
    }
}

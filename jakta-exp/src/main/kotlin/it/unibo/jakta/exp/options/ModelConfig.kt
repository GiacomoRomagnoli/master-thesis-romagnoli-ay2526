package it.unibo.jakta.exp.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_MODEL_ID
import it.unibo.jakta.agents.bdi.generationstrategies.lm.ReasoningEffort

class ModelConfig : OptionGroup(name = "Model Configuration") {
    val modelId: String by option()
        .help("ID of the model to use.")
        .default(DEFAULT_MODEL_ID)

    val temperature: Double by option()
        .double()
        .default(DefaultGenerationConfig.DEFAULT_TEMPERATURE)
        .help(
            """
            What sampling temperature to use, between $MIN_TEMPERATURE and $MAX_TEMPERATURE. 
            Higher values like 0.8 will make the output more random, 
            while lower values like 0.2 will make it more focused and deterministic.
            """.trimIndent(),
        ).check("value must be positive number, between $MIN_TEMPERATURE and $MAX_TEMPERATURE") {
            it in MIN_TEMPERATURE..MAX_TEMPERATURE
        }

    val topP: Double by option()
        .double()
        .default(DefaultGenerationConfig.DEFAULT_TOP_P)
        .help(
            """
            This setting limits the model’s choices to a percentage of likely tokens: 
            only the top tokens whose probabilities add up to P. 
            A lower value makes the model’s responses more predictable, 
            while the default setting allows for a full range of token choices. 
            """.trimIndent(),
        ).check("value must be positive number, between $MIN_TOP_P and $MAX_TOP_P") {
            it in MIN_TOP_P..MAX_TOP_P
        }

    val reasoningEffort: ReasoningEffort? by option()
        .enum<ReasoningEffort>()
        .help("Set the level of reasoning effort for processing.")

    val maxTokens: Int by option()
        .int()
        .default(DefaultGenerationConfig.DEFAULT_MAX_TOKENS)
        .help(
            """
            The maximum number of tokens allowed for the generated answer. 
            By default, the number of tokens the model can return will 
            be ${DefaultGenerationConfig.DEFAULT_MAX_TOKENS}.
            """.trimIndent(),
        )

    companion object {
        const val MIN_TEMPERATURE = 0.0
        const val MAX_TEMPERATURE = 2.0
        const val MIN_TOP_P = 0.0
        const val MAX_TOP_P = 1.0
    }
}

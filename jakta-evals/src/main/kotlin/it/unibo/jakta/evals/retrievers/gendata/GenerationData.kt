package it.unibo.jakta.evals.retrievers.gendata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents generation metadata and usage statistics returned by OpenRouter after a completion request.
 *
 * OpenRouter provides two types of token counts to accommodate different use cases:
 * - **Normalized tokens**: Standardized counts using the GPT-4o tokenizer for consistency across all models
 * - **Native tokens**: Model-specific counts using each model's own tokenizer (e.g., Anthropic's for Claude, OpenAI's for GPT-4)
 *
 * @property id Unique identifier for this generation request
 * @property totalCost Total cost of the request in USD
 * @property latency Time taken to receive the first token in milliseconds
 * @property generationTime Total time taken to complete the generation in milliseconds
 * @property inputTokensNormalized Number of input tokens counted using GPT-4o tokenizer (standardized)
 * @property outputTokensNormalized Number of output tokens counted using GPT-4o tokenizer (standardized)
 * @property outputReasoningTokensNative Number of reasoning tokens in the output using the model's native tokenizer
 * @property inputTokensNative Number of input tokens counted using the model's native tokenizer
 * @property outputTokensNative Number of output tokens counted using the model's native tokenizer
 */
@Serializable
@SerialName("GenerationData")
data class GenerationData(
    val id: String,
    @SerialName("total_cost")
    val totalCost: Double,
    val latency: Int,
    @SerialName("generation_time")
    val generationTime: Int,
    @SerialName("tokens_prompt")
    val inputTokensNormalized: Int,
    @SerialName("tokens_completion")
    val outputTokensNormalized: Int,
    @SerialName("native_tokens_reasoning")
    val outputReasoningTokensNative: Int,
    @SerialName("native_tokens_prompt")
    val inputTokensNative: Int,
    @SerialName("native_tokens_completion")
    val outputTokensNative: Int,
)

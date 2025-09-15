package it.unibo.jakta.agents.bdi.generationstrategies.lm

import com.aallam.openai.api.chat.Effort
import it.unibo.jakta.agents.bdi.engine.generation.GenerationConfig
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_CONNECT_TIMEOUT
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_LM_SERVER_URL
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_MAX_TOKENS
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_MODEL_ID
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_REQUEST_TIMEOUT
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_SOCKET_TIMEOUT
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_TEMPERATURE
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_TOKEN
import it.unibo.jakta.agents.bdi.generationstrategies.lm.DefaultGenerationConfig.DEFAULT_TOP_P
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering.ContextFilter
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering.DefaultFilters.metaPlanFilter
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.DefaultPromptBuilder.defaultSystemPrompt
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.DefaultPromptBuilder.userPromptWithHintsAndRemarks
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.SystemPromptBuilder
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.UserPromptBuilder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

sealed interface LMGenerationConfig : GenerationConfig {
    val modelId: String?
    val temperature: Double?
    val topP: Double?
    val reasoningEffort: Effort?
    val maxTokens: Int?
    val lmServerUrl: String?
    val lmServerToken: String?
    val contextFilters: Iterable<ContextFilter>
    val contextFiltersNames: List<String>
    val systemPromptBuilder: SystemPromptBuilder?
    val systemPromptBuilderName: String?
    val userPromptBuilder: UserPromptBuilder?
    val userPromptBuilderName: String?
    val remarks: Iterable<Remark>?
    val requestTimeout: Long?
    val connectTimeout: Long?
    val socketTimeout: Long?

    @Serializable
    @SerialName("LMGenerationConfigContainer")
    data class LMGenerationConfigContainer(
        override val modelId: String = DEFAULT_MODEL_ID,
        override val temperature: Double = DEFAULT_TEMPERATURE,
        override val topP: Double = DEFAULT_TOP_P,
        override val reasoningEffort: Effort? = null,
        override val maxTokens: Int = DEFAULT_MAX_TOKENS,
        override val lmServerUrl: String = DEFAULT_LM_SERVER_URL,
        @Transient
        override val lmServerToken: String = DEFAULT_TOKEN,
        @Transient
        override val contextFilters: List<ContextFilter> = listOf(metaPlanFilter),
        @Transient
        override val systemPromptBuilder: SystemPromptBuilder? = defaultSystemPrompt,
        @Transient
        override val userPromptBuilder: UserPromptBuilder = userPromptWithHintsAndRemarks,
        override val contextFiltersNames: List<String> = contextFilters.map { it.name },
        override val systemPromptBuilderName: String? = systemPromptBuilder?.name,
        override val userPromptBuilderName: String? = userPromptBuilder.name,
        override val remarks: List<Remark> = emptyList(),
        override val requestTimeout: Long = DEFAULT_REQUEST_TIMEOUT,
        override val connectTimeout: Long = DEFAULT_CONNECT_TIMEOUT,
        override val socketTimeout: Long = DEFAULT_SOCKET_TIMEOUT,
    ) : LMGenerationConfig

    @Serializable
    @SerialName("LMGenerationConfigUpdate")
    data class LMGenerationConfigUpdate(
        override val modelId: String? = null,
        override val temperature: Double? = null,
        override val topP: Double? = null,
        override val reasoningEffort: Effort? = null,
        override val maxTokens: Int? = null,
        override val lmServerUrl: String? = null,
        @Transient
        override val lmServerToken: String? = null,
        @Transient
        override val contextFilters: List<ContextFilter> = emptyList(),
        @Transient
        override val systemPromptBuilder: SystemPromptBuilder? = null,
        @Transient
        override val userPromptBuilder: UserPromptBuilder? = null,
        override val contextFiltersNames: List<String> = emptyList(),
        override val systemPromptBuilderName: String? = null,
        override val userPromptBuilderName: String? = null,
        override val remarks: List<Remark>? = null,
        override val requestTimeout: Long? = null,
        override val connectTimeout: Long? = null,
        override val socketTimeout: Long? = null,
    ) : LMGenerationConfig {
        fun patch(base: LMGenerationConfigContainer): LMGenerationConfigContainer =
            base.copy(
                modelId = modelId ?: base.modelId,
                temperature = temperature ?: base.temperature,
                topP = topP ?: base.topP,
                maxTokens = maxTokens ?: base.maxTokens,
                lmServerUrl = lmServerUrl ?: base.lmServerUrl,
                lmServerToken = lmServerToken ?: base.lmServerToken,
                contextFilters = contextFilters.ifEmpty { base.contextFilters },
                systemPromptBuilder = systemPromptBuilder ?: base.systemPromptBuilder,
                userPromptBuilder = userPromptBuilder ?: base.userPromptBuilder,
                remarks = remarks ?: base.remarks,
                requestTimeout = requestTimeout ?: base.requestTimeout,
                connectTimeout = connectTimeout ?: base.connectTimeout,
                socketTimeout = socketTimeout ?: base.socketTimeout,
                contextFiltersNames = (contextFilters.ifEmpty { base.contextFilters }).map { it.name },
                systemPromptBuilderName = (systemPromptBuilder ?: base.systemPromptBuilder)?.name,
                userPromptBuilderName = (userPromptBuilder ?: base.userPromptBuilder).name,
            )
    }
}

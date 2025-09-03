package it.unibo.jakta.agents.bdi.generationstrategies.lm.dsl

import it.unibo.jakta.agents.bdi.dsl.ScopeBuilder
import it.unibo.jakta.agents.bdi.generationstrategies.lm.LMGenerationConfig.LMGenerationConfigUpdate
import it.unibo.jakta.agents.bdi.generationstrategies.lm.Remark
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.filtering.ContextFilter
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.SystemPromptBuilder
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting.UserPromptBuilder
import kotlin.time.Duration
import kotlin.time.DurationUnit

class LMGenerationConfigScope : ScopeBuilder<LMGenerationConfigUpdate> {
    var model: String? = null
    var temperature: Double? = null
    var topP: Double? = null
    var maxTokens: Int? = null
    var url: String? = null
    var token: String? = null
    var requestTimeout: Duration? = null
    var connectTimeout: Duration? = null
    var socketTimeout: Duration? = null
    var contextFilters: List<ContextFilter> = emptyList()
    var systemPromptBuilder: SystemPromptBuilder? = null
    var userPromptBuilder: UserPromptBuilder? = null

    private val remarks = mutableListOf<Remark>()

    fun remark(remark: String) {
        remarks += Remark(remark)
    }

    fun remarks(vararg remark: String) {
        remarks.addAll(remark.map { Remark(it) })
    }

    fun remarks(remarks: Iterable<Remark>) {
        this.remarks.addAll(remarks)
    }

    override fun build() =
        LMGenerationConfigUpdate(
            model,
            temperature,
            topP,
            maxTokens,
            url,
            token,
            contextFilters,
            systemPromptBuilder,
            userPromptBuilder,
            contextFilters.map { it.name },
            systemPromptBuilder?.name,
            userPromptBuilder?.name,
            remarks,
            requestTimeout?.toLong(DurationUnit.MILLISECONDS),
            connectTimeout?.toLong(DurationUnit.MILLISECONDS),
            socketTimeout?.toLong(DurationUnit.MILLISECONDS),
        )
}

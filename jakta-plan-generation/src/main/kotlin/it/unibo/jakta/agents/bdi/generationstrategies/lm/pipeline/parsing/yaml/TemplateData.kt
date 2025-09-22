package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.parsing.yaml

import com.charleskorn.kaml.YamlContentPolymorphicSerializer
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.yamlMap
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = TemplateSerializer::class)
sealed interface TemplateData {
    val purpose: String
}

@Serializable
@SerialName("goal")
internal data class GoalTemplate(
    val goal: String,
    override val purpose: String,
) : TemplateData

@Serializable
@SerialName("belief")
internal data class BeliefTemplate(
    val belief: String,
    override val purpose: String,
) : TemplateData

internal object TemplateSerializer : YamlContentPolymorphicSerializer<TemplateData>(TemplateData::class) {
    override fun selectDeserializer(node: YamlNode): DeserializationStrategy<TemplateData> =
        when {
            node.yamlMap.getKey("belief") != null -> BeliefTemplate.serializer()
            node.yamlMap.getKey("goal") != null -> GoalTemplate.serializer()
            else -> error("Unknown template")
        }
}

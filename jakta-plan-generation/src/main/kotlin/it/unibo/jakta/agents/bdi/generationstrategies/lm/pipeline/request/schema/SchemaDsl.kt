package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.schema

import com.aallam.openai.api.chat.JsonSchema
import kotlinx.serialization.json.JsonObject

class SchemaDsl {
    private var name: String? = null
    private var strict: Boolean = false
    private var schema: JsonObject = JsonObject(mapOf())

    fun name(name: String) {
        this.name = name
    }

    fun strict() {
        this.strict = true
    }

    fun schema(block: ObjectDsl.() -> Unit) {
        val builder = ObjectDsl()
        builder.block()
        this.schema = builder.build()
    }

    fun schema(schema: JsonObject) {
        this.schema = schema
    }

    fun build(): JsonSchema = JsonSchema(name = name, schema = schema, strict = strict)

    companion object {
        fun jsonSchema(block: SchemaDsl.() -> Unit): JsonSchema {
            val builder = SchemaDsl()
            builder.block()
            return builder.build()
        }
    }
}

package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.schema

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class ObjectDsl {
    private val required = mutableListOf<JsonPrimitive>()
    private val properties: MutableMap<String, JsonObject> = mutableMapOf()
    val string = JsonObject(mapOf(TYPE to STRING))
    val number = JsonObject(mapOf(TYPE to NUMBER))

    fun enum(vararg values: String) =
        JsonObject(
            mapOf(
                TYPE to STRING,
                ENUM to JsonArray(values.map { JsonPrimitive(it) }),
            ),
        )

    fun array(items: JsonObject) = JsonObject(mapOf(TYPE to ARRAY, ITEMS to items))

    fun array(block: ObjectDsl.() -> Unit) = array(schema(block))

    fun required(vararg keys: String) = required.addAll(keys.map { JsonPrimitive(it) })

    fun property(
        key: String,
        value: JsonObject,
        description: String? = null,
    ) = when (description) {
        null -> properties[key] = value
        else -> properties[key] = JsonObject(value + (DESCRIPTION to JsonPrimitive(description)))
    }

    fun property(
        key: String,
        description: String? = null,
        block: ObjectDsl.() -> Unit,
    ) = property(key, schema(block), description)

    fun build(): JsonObject =
        JsonObject(
            mapOf(
                TYPE to OBJECT,
            ).let {
                if (required.isNotEmpty()) it + (REQUIRED to JsonArray(required)) else it
            }.let {
                if (properties.isNotEmpty()) it + (PROPERTIES to JsonObject(properties)) else it
            },
        )

    companion object {
        private const val TYPE = "type"
        private const val ENUM = "enum"
        private const val REQUIRED = "required"
        private const val ITEMS = "items"
        private const val PROPERTIES = "properties"
        private const val DESCRIPTION = "description"
        private val STRING = JsonPrimitive("string")
        private val NUMBER = JsonPrimitive("number")
        private val ARRAY = JsonPrimitive("array")
        private val OBJECT = JsonPrimitive("object")

        fun schema(block: ObjectDsl.() -> Unit): JsonObject {
            val scope = ObjectDsl()
            scope.block()
            return scope.build()
        }
    }
}

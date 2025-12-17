package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.schema

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class ObjectDsl {
    private val required = mutableListOf<JsonPrimitive>()
    private val oneOf = mutableListOf<JsonObject>()
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

    fun array(vararg items: JsonObject) = JsonObject(mapOf(TYPE to ARRAY, ITEMS to obj { oneOf(*items) }))

    fun obj(block: ObjectDsl.() -> Unit): JsonObject {
        val builder = ObjectDsl()
        builder.block()
        return builder.build()
    }

    fun oneOf(vararg schemas: JsonObject) = oneOf.addAll(schemas)

    fun required(vararg keys: String) = required.addAll(keys.map { JsonPrimitive(it) })

    // TODO: introdurre la descrizione come parametro
    fun property(
        key: String,
        value: JsonObject,
    ) {
        properties[key] = value
    }

    fun property(
        key: String,
        vararg value: JsonObject,
    ) {
        properties[key] = obj { oneOf(*value) }
    }

    fun property(
        key: String,
        block: ObjectDsl.() -> Unit,
    ) {
        val builder = ObjectDsl()
        builder.block()
        properties[key] = builder.build()
    }

    fun build(): JsonObject =
        JsonObject(
            mapOf(
                if (oneOf.isEmpty()) (TYPE to OBJECT) else (ONE_OF to JsonArray(oneOf)),
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
        private const val ONE_OF = "oneOf"
        private val STRING = JsonPrimitive("string")
        private val NUMBER = JsonPrimitive("number")
        private val ARRAY = JsonPrimitive("array")
        private val OBJECT = JsonPrimitive("object")

        fun schema(block: ObjectDsl.() -> Unit): JsonObject {
            val builder = ObjectDsl()
            builder.block()
            return builder.build()
        }
    }
}

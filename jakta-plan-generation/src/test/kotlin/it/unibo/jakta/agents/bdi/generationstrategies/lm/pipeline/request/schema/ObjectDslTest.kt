package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.schema

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.request.schema.ObjectDsl.Companion.schema
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class ObjectDslTest :
    FunSpec({
        test("empty schema") {
            val schema = schema { }
            schema["type"] shouldBe primitive("object")
            schema["properties"] shouldBe null
            schema["required"] shouldBe null
        }

        test("single property schema") {
            val schema = schema { property("a", string) }
            schema["type"] shouldBe primitive("object")
            schema["properties"] shouldBe obj("a" to obj("type" to primitive("string")))
            schema["required"] shouldBe null
        }

        test("required fields schema") {
            val schema =
                schema {
                    required("a")
                    property("a", string)
                    property("b", string)
                }
            schema["type"] shouldBe primitive("object")
            schema["properties"] shouldBe
                obj(
                    "a" to obj("type" to primitive("string")),
                    "b" to obj("type" to primitive("string")),
                )
            schema["required"] shouldBe array("a")
        }
    }) {
    companion object {
        fun obj(vararg values: Pair<String, JsonElement>) = JsonObject(mapOf(*values))

        fun primitive(value: String) = JsonPrimitive(value)

        fun array(vararg values: String) = JsonArray(values.map { JsonPrimitive(it) })

        fun array(vararg values: JsonElement) = JsonArray(listOf(*values))
    }
}

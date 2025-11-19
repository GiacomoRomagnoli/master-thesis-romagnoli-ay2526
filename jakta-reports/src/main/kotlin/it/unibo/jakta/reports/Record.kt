package it.unibo.jakta.reports

import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

data class Record(
    val experiment: String,
    val succeeded: Boolean,
    val actionFailed: Int,
    val plansNotParsed: Int,
    val admissibleGoalsNotParsed: Int,
    val admissibleBeliefNotParsed: Int,
) {
    companion object {
        val properties = properties()

        private fun properties(): List<KProperty1<Record, *>> {
            val metadata =
                Record::class.java.getAnnotation(Metadata::class.java) ?: return Record::class.memberProperties.toList()

            val kotlinMetadata = KotlinClassMetadata.readStrict(metadata)
            if (kotlinMetadata !is KotlinClassMetadata.Class) {
                return Record::class.memberProperties.toList()
            }

            val order = kotlinMetadata.kmClass.properties.map { it.name }

            return Record::class
                .memberProperties
                .sortedBy { order.indexOf(it.name) }
        }
    }

    override fun toString(): String =
        properties.joinToString("") {
            var value = it.call(this).toString()
            if (it.name == "experiment") {
                value = "${value.take(5)}...${value.takeLast(5)}"
            }
            value.padEnd(Report.PAD)
        }
}

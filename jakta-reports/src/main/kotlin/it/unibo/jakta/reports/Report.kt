package it.unibo.jakta.reports

class Report(
    val records: List<Record>,
) {
    fun add(record: Record) = Report(records + record)

    override fun toString(): String {
        if (records.isEmpty()) return "Report empty"
        val header = Record.properties.joinToString("") { it.name.padEnd(PAD) }
        val rows = records.joinToString("\n")
        return "$header\n$rows"
    }

    companion object {
        const val PAD = 30
    }
}

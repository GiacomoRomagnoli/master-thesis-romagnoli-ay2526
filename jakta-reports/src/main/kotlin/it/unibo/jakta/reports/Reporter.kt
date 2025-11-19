package it.unibo.jakta.reports

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import it.unibo.jakta.agents.bdi.engine.Jakta.separator
import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.serialization.modules.JaktaJsonComponent
import it.unibo.jakta.evals.AbstractRunEvaluator
import it.unibo.jakta.evals.evaluators.run.RunEvaluation
import it.unibo.jakta.exp.base.BaseExpRunner
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
class Reporter : CliktCommand() {
    init {
        JaktaKoin.loadAdditionalModules(BaseExpRunner.modulesToLoad)
    }

    val metricsDir: String by option()
        .default(DEFAULT_METRICS_DIR)
        .help("The directory where the results of the evaluations are stored.")

    val evaluationFileName: String by option()
        .default(DEFAULT_EVALUATION_FILE_NAME)
        .help("The file name where the results of the evaluations are stored.")

    override fun run() {
        var report = Report(emptyList())
        for (file in File(metricsDir).listFiles() ?: throw IllegalArgumentException("no such metrics dir.")) {
            for (experiment in file.listFiles { it.isDirectory }.orEmpty()) {
                val evaluation = File(experiment, evaluationFileName)
                if (evaluation.exists()) {
                    val pgpInvocation =
                        JaktaJsonComponent
                            .json
                            .decodeFromString<RunEvaluation>(evaluation.readText())
                            .planData
                            ?.pgpInvocation
                    if (pgpInvocation != null) {
                        val record =
                            Record(
                                experiment.name,
                                pgpInvocation.achievesGoal,
                                pgpInvocation.actionFailures,
                                pgpInvocation.plansNotParsed,
                                pgpInvocation.admissibleGoalsNotParsed,
                                pgpInvocation.admissibleBeliefNotParsed,
                            )
                        report = report.add(record)
                    }
                }
            }
        }
        println(report)
    }

    companion object {
        const val DEFAULT_EVALUATION_FILE_NAME = "evaluation_result.json"

        val DEFAULT_METRICS_DIR = "..${separator}jakta-evals${separator}${AbstractRunEvaluator.DEFAULT_METRICS_DIR}"

        @JvmStatic
        fun main(args: Array<String>) {
            Reporter().main(args)
        }
    }
}

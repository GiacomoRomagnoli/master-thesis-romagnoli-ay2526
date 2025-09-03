import java.util.Properties

plugins {
    alias(libs.plugins.kotlinx)
}

dependencies {
    implementation(project(":jakta-evals"))
    implementation(project(":jakta-exp"))
    implementation(project(":jakta-dsl"))
    implementation(project(":jakta-plan-generation"))

    implementation(libs.kotlin.coroutines)
    implementation(libs.ktor.network)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.kotlin.testing)
    implementation(libs.bundles.kotlin.logging)
    implementation(libs.openai)
    implementation(libs.clikt)
    implementation(libs.bundles.koin)
}

tasks.register<JavaExec>("replayExperiment") {
    description = "Run the explorer agent sample by reusing already generated responses."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.playground.explorer.ExplorerExperimentReplayerKt"
}

tasks.register<JavaExec>("runBaseline") {
    description = "Run the explorer agent sample with the baseline plans."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.playground.explorer.BaselineExplorerRunnerKt"
}

tasks.register<JavaExec>("analyzePGP") {
    val environment =
        runCatching {
            val keystoreFile = project.rootProject.file(".env")
            if (!keystoreFile.exists()) return@runCatching emptyMap<String, String>()

            val properties = Properties()
            keystoreFile.inputStream().use { properties.load(it) }
            mapOf("API_KEY" to properties.getProperty("API_KEY"))
        }.getOrElse { exception ->
            println("Warning: Could not load environment: ${exception.message}")
            emptyMap()
        }

    this.environment = environment
    description = "Evaluate each PGP attempt."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.playground.evaluation.AnalyzePGPKt"
}

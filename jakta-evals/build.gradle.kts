import java.util.Properties

plugins {
    alias(libs.plugins.kotlinx)
    alias(libs.plugins.ktor)
}

dependencies {
    implementation(project(":jakta-bdi"))
    implementation(project(":jakta-exp"))
    implementation(project(":jakta-plan-generation"))

    implementation(libs.clikt)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.client)
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(libs.log4j.slf4j)
    implementation(libs.bundles.koin)
    implementation(libs.openai)
    implementation(libs.ktor.serialization.kotlinx.json)
}

application {
    mainClass.set("${project.group}.evals.server.ApplicationKt")
}

tasks.register<JavaExec>("evalRun") {
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
    description = "Evaluate the results of one or more experiments."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.evals.EvalRunKt"
}

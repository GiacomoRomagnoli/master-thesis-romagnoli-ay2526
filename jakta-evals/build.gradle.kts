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

fun loadEnv(): Map<String, String> {
    val envFile = rootProject.file(".env")
    if (!envFile.exists()) return emptyMap()

    return runCatching {
        val props =
            Properties().apply {
                envFile.inputStream().use { load(it) }
            }
        props.entries.associate { it.key.toString() to it.value.toString() }
    }.getOrElse { exception ->
        println("Warning: Could not load environment: ${exception.message}")
        emptyMap()
    }
}

tasks.register<JavaExec>("evalRun") {
    description = "Evaluate the results of one or more experiments."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.evals.AblationEvalRunKt"
}

tasks.register<JavaExec>("ecaiEvalRun") {
    description = "Evaluate the results of one or more experiments."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.evals.EcaiEvalRunKt"
}

listOf("evalRun", "ecaiEvalRun").forEach { taskName ->
    tasks.named<JavaExec>(taskName).configure {
        environment(loadEnv())
    }
}

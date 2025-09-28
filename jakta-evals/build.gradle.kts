import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx)
}

dependencies {
    implementation(project(":jakta-bdi"))
    implementation(project(":jakta-exp"))
    implementation(project(":jakta-plan-generation"))

    implementation(libs.clikt)
    implementation(libs.bundles.kotlin.logging)
    implementation(libs.bundles.koin)
    implementation(libs.openai)
    implementation(libs.bundles.ktor.client)
    implementation(libs.ktor.serialization.kotlinx.json)
}

val appMainClass = "${project.group}.evals.AblationRunEvaluator"

tasks.shadowJar {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to appMainClass,
                "Multi-Release" to "true",
            ),
        )
    }

    transform(Log4j2PluginsCacheFileTransformer())
    mergeServiceFiles()
}

application {
    mainClass.set(appMainClass)
}

tasks.register<JavaExec>("baseEvalRun") {
    description = "Evaluate the results of one or more base experiments."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.evals.BaseExpRunEvaluator"
}

listOf("run", "baseEvalRun", "runShadow").forEach { taskName ->
    tasks.named<JavaExec>(taskName).configure {
        environment(loadEnv())
    }
}

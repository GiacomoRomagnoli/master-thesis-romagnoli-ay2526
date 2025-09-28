import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    application
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlinx)
}

dependencies {
    implementation(project(":jakta-dsl"))
    implementation(project(":jakta-plan-generation"))
    implementation(libs.clikt)
    implementation(libs.bundles.kotlin.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.kotlin.testing)
    implementation(libs.openai)
    implementation(libs.bundles.koin)
    annotationProcessor(libs.log4j.core)
}

val appMainClass = "${project.group}.exp.ablation.AblationExpRunner"

tasks.shadowJar {
    archiveFileName.set("jakta-exp-all.jar")
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

tasks.register<JavaExec>("runBaseExperiment") {
    description = "Run the explorer agent sample with the base experiment."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.exp.base.BaseExpRunner"
}

listOf("run", "runBaseExperiment", "runShadow").forEach { taskName ->
    tasks.named<JavaExec>(taskName).configure {
        environment(loadEnv())
    }
}

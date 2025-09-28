import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx)
}

dependencies {
    implementation(project(":jakta-dsl"))
    implementation(project(":jakta-evals"))
    implementation(project(":jakta-exp"))
    implementation(project(":jakta-plan-generation"))

    implementation(libs.clikt)
    implementation(libs.bundles.kotlin.logging)
    implementation(libs.bundles.koin)
    implementation(libs.openai)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlin.coroutines)
    implementation(libs.ktor.network)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.kotlin.testing)
}

val appMainClass = "${project.group}.invoker.AblationExpLMInvokerKt"

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

tasks.register<JavaExec>("baseLMInvoker") {
    description = "Run the client that invokes LMs for the base experiment."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.invoker.BaseExpLMInvokerKt"
}

listOf("run", "baseLMInvoker", "runShadow").forEach { taskName ->
    tasks.named<JavaExec>(taskName).configure {
        environment(loadEnv())
    }
}

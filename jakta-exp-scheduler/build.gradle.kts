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

val appMainClass = "${project.group}.scheduler.server.ApplicationKt"

application {
    mainClass.set(appMainClass)
}

tasks.register<JavaExec>("runAblationStudy") {
    description = "Run the ablation study."
    group = "application"
    environment = loadEnv()

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.scheduler.AblationStudyScheduler"
}

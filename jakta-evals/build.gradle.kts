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

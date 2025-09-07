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

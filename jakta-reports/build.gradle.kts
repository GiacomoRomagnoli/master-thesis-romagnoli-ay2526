import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    application
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":jakta-bdi"))
    implementation(project(":jakta-evals"))
    implementation(project(":jakta-exp"))

    implementation(libs.clikt)
    implementation(libs.bundles.kotlin.logging)
    implementation(libs.bundles.koin)
    implementation(libs.openai)
    implementation(libs.bundles.ktor.client)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")
}

val appMainClass = "${project.group}.reports.Reporter"

application {
    mainClass.set(appMainClass)
}

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

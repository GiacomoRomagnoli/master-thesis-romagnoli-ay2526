plugins {
    application
    alias(libs.plugins.kotlinx)
    id("org.graalvm.buildtools.native") version "0.11.0"
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

val expMainClass = "${project.group}.exp.explorer.ExplorerRunnerKt"

val enableAgent = project.hasProperty("agent")
val enableDebug = project.hasProperty("debug")

graalvmNative {
    binaries {
        named("main") {
            imageName.set(project.name)
            mainClass.set(expMainClass)
            debug.set(enableDebug)
            buildArgs.addAll(
                "--initialize-at-build-time=kotlinx.serialization",
                "--initialize-at-build-time=com.aallam.openai.api",
                "--initialize-at-build-time=org.slf4j,org.apache.logging.slf4j,org.apache.logging.log4j",
                "--initialize-at-build-time=kotlin",
                "--initialize-at-run-time=org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder",
                "--initialize-at-run-time=org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration",
                "--initialize-at-run-time=kotlin.uuid.SecureRandomHolder",
                "--initialize-at-run-time=kotlin.random.FallbackThreadLocalRandom",
                "--enable-url-protocols=https,http",
            )
            verbose.set(true)
            javaLauncher.set(
                javaToolchains.launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(21))
                    vendor.set(JvmVendorSpec.GRAAL_VM)
                },
            )
        }
    }
    agent {
        enabled.set(enableAgent)
        metadataCopy {
            inputTaskNames.add("run")
            outputDirectories.add("src/main/resources/META-INF/native-image/it.unibo.jakta.exp")
            mergeWithExisting.set(false)
        }
    }
    metadataRepository { enabled.set(true) }
    toolchainDetection = false
}

application {
    mainClass.set(expMainClass)
}

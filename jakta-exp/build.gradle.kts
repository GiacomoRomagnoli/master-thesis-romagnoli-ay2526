import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.graalvm.buildtools.gradle.tasks.NativeRunTask
import java.util.Properties

plugins {
    application
    alias(libs.plugins.kotlinx)
    id("org.graalvm.buildtools.native") version "0.11.0"
    alias(libs.plugins.shadow)
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

val expMainClass = "${project.group}.exp.AblationExpRunner"
val enableAgent = project.hasProperty("agent")
val enableDebug = project.hasProperty("debug")

tasks.shadowJar {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to expMainClass,
                "Multi-Release" to "true",
            ),
        )
    }

    transform(Log4j2PluginsCacheFileTransformer())
    mergeServiceFiles()
}

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
            if (project.hasProperty("args")) {
                runtimeArgs.addAll(project.property("args").toString().split(" "))
            }
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

tasks.register<JavaExec>("runEcaiExperiment") {
    description = "Run the explorer agent sample with the baseline plans."
    group = "application"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "${project.group}.exp.ecai.EcaiExpRunner"
}

listOf("run", "runEcaiExperiment", "runShadow").forEach { taskName ->
    tasks.named<JavaExec>(taskName).configure {
        environment(loadEnv())
    }
}

tasks.withType<NativeRunTask>().configureEach {
    environment.putAll(loadEnv())
}

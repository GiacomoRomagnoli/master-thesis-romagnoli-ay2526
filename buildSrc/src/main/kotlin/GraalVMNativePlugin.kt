import org.graalvm.buildtools.gradle.NativeImagePlugin
import org.graalvm.buildtools.gradle.dsl.GraalVMExtension
import org.graalvm.buildtools.gradle.tasks.NativeRunTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.*

class GraalVMNativePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("java")
        project.plugins.apply(NativeImagePlugin::class.java)

        val javaToolchains = project.extensions.getByType<org.gradle.jvm.toolchain.JavaToolchainService>()
        val extension = project.extensions.create("graalvmNativeConfig", GraalVMNativeExtension::class.java)
        val envVars = project.loadEnv()

        project.tasks.withType<NativeRunTask>().configureEach { environment.putAll(envVars) }

        extension.imageName.convention(envVars["IMAGE_NAME"] ?: "my-image")
        extension.mainClass.convention(envVars["MAIN_CLASS"] ?: "com.example.MainKt")
        extension.enableAgent.convention(project.hasProperty("agent"))
        extension.enableDebug.convention(project.hasProperty("debug"))
        extension.metadataDirectory.convention("src/main/resources/META-INF/native-image/${project.group}")
        extension.javaVersion.convention(21)
        extension.verbose.convention(true)
        extension.toolchainDetection.convention(false)
        extension.additionalBuildArgs.convention(emptyList())

        project.afterEvaluate {
            project.extensions.configure(GraalVMExtension::class.java) {
                binaries.named("main") {
                    imageName.set(extension.imageName)
                    mainClass.set(extension.mainClass)
                    debug.set(extension.enableDebug)

                    // Base build args
                    val baseBuildArgs = listOf(
                        "--initialize-at-build-time=kotlinx.serialization",
                        "--initialize-at-build-time=org.slf4j,org.apache.logging.slf4j,org.apache.logging.log4j",
                        "--initialize-at-build-time=kotlin",
                        "--initialize-at-run-time=org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder",
                        "--initialize-at-run-time=org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration",
                        "--initialize-at-run-time=kotlin.uuid.SecureRandomHolder",
                        "--initialize-at-run-time=kotlin.random.FallbackThreadLocalRandom",
                        "--enable-url-protocols=https,http"
                    )

                    // Combine base args with additional args from extension
                    buildArgs.addAll(baseBuildArgs + extension.additionalBuildArgs.get())

                    verbose.set(extension.verbose)
                    javaLauncher.set(
                        javaToolchains.launcherFor {
                            languageVersion.set(JavaLanguageVersion.of(extension.javaVersion.get()))
                            vendor.set(JvmVendorSpec.GRAAL_VM)
                        },
                    )
                }
                agent {
                    enabled.set(extension.enableAgent)
                    metadataCopy {
                        inputTaskNames.add("run")
                        outputDirectories.add(extension.metadataDirectory)
                        mergeWithExisting.set(false)
                    }
                }
                toolchainDetection = extension.toolchainDetection.get()
            }
        }
    }
}

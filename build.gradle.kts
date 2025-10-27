import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "it.unibo.jakta"

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.publishOnCentral)
    //   alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.taskTree)
    // alias(libs.plugins.multiJvmTesting) %% Version 3.6.0 is issuing problems with Java version
    alias(libs.plugins.kotlin.qa)
}

dependencies {
    implementation(project(":jakta-state-machine"))
    implementation(project(":jakta-bdi"))
    implementation(project(":jakta-dsl"))
    implementation(project(":jakta-plan-generation"))
    implementation(project(":jakta-konsist-test"))
}

val Provider<PluginDependency>.id: String get() = get().pluginId

allprojects {
    group = rootProject.group

    with(rootProject.libs.plugins) {
        apply(plugin = publishOnCentral.id)
        apply(plugin = gitSemVer.id)
//        apply(plugin = dokka.id)
        apply(plugin = kotlin.jvm.id)
        apply(plugin = taskTree.id)
//        apply(plugin = multiJvmTesting.id) %% Version 3.6.0 is issuing problems with Java version
        apply(plugin = kotlin.qa.id)
    }

    repositories {
        mavenCentral()
    }

    gitSemVer {
        versionPrefix.set("v")
        excludeLightweightTags()
        assignGitSemanticVersion()
    }

    dependencies {
        implementation(rootProject.libs.kotlin.stdlib)
        testImplementation(rootProject.libs.bundles.kotlin.testing)
    }

    // ====== COMPILATION TASKS =====
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            allWarningsAsErrors = true
            freeCompilerArgs.add("-Xcontext-receivers")
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            freeCompilerArgs.add("-Xwhen-guards")
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

//    multiJvm {
//        jvmVersionForCompilation.set(21)
//    }

    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
    }

    publishOnCentral {
        projectLongName.set("JaKtA")
        projectDescription.set("A Kotlin internal DSL for the definition of BDI agents")
        val repoOwner = "jakta-bdi"
        scmConnection.set("scm:git:https://github.com/$repoOwner/${rootProject.name}")
        projectUrl.set("https://github.com/$repoOwner/${rootProject.name}")
        publishing {
            publications {
                withType<MavenPublication>().configureEach {
                    pom {
                        developers {
                            developer {
                                id.set("anitvam")
                                name.set("Martina Baiardi")
                                email.set("m.baiardi@unibo.it")
                            }
                        }
                    }
                }
            }
        }
    }

    tasks {
        test {
            useJUnitPlatform()
            testLogging {
                showStandardStreams = true
                showCauses = true
                showStackTraces = true
                events(*TestLogEvent.values())
                exceptionFormat = TestExceptionFormat.FULL
            }
        }
    }

    tasks.detekt {
        onlyIf {
            project.hasProperty("runDetect")
        }
    }

    tasks.detektMain {
        onlyIf {
            project.hasProperty("runDetect")
        }
    }
}

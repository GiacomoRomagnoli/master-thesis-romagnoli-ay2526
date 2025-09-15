package it.unibo.jakta.agents.bdi.engine.depinjection

import it.unibo.jakta.agents.bdi.engine.serialization.modules.JaktaJsonModule
import it.unibo.jakta.agents.bdi.engine.serialization.modules.SerializersModuleProvider
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module

object JaktaKoin {
    val defaultJsonModule =
        module {
            single {
                val providers = getAll<SerializersModuleProvider>()
                val combinedModule =
                    SerializersModule {
                        providers.forEach { provider ->
                            include(provider.modules)
                        }
                    }
                Json {
                    allowStructuredMapKeys = true
                    ignoreUnknownKeys = true
                    serializersModule = combinedModule
                    encodeDefaults = true
                    allowSpecialFloatingPointValues = true
                }
            }
        }

    private val koinApp =
        koinApplication {
            modules(defaultJsonModule)
        }

    internal val koin = koinApp.koin

    val engineJsonModule =
        module {
            single<SerializersModuleProvider>(named("EngineJsonModule")) {
                JaktaJsonModule()
            }
        }

    fun loadAdditionalModules(vararg modules: Module) {
        koin.loadModules(modules.toList())
    }

    fun loadAdditionalModules(modules: List<Module>) {
        koin.loadModules(modules)
    }
}

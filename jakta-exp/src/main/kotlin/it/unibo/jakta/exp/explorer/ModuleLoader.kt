package it.unibo.jakta.exp.explorer

import it.unibo.jakta.agents.bdi.engine.depinjection.JaktaKoin
import it.unibo.jakta.agents.bdi.engine.serialization.modules.SerializersModuleProvider
import it.unibo.jakta.agents.bdi.generationstrategies.lm.serialization.LMPlanGenJsonModule
import org.koin.core.qualifier.named
import org.koin.dsl.module

object ModuleLoader {
    val jsonModule =
        module {
            single<SerializersModuleProvider>(named("LMPlanGenJsonModule")) { LMPlanGenJsonModule() }
        }

    fun loadModules() = JaktaKoin.loadAdditionalModules(JaktaKoin.engineJsonModule, jsonModule)
}

package it.unibo.jakta.exp.ecai.explorer.serialization

import it.unibo.jakta.agents.bdi.engine.logging.events.LogEvent
import it.unibo.jakta.agents.bdi.engine.serialization.modules.SerializersModuleProvider
import it.unibo.jakta.exp.ecai.explorer.logging.MoveActionSuccess
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class ExplorerJsonModule : SerializersModuleProvider {
    override val modules =
        SerializersModule {
            polymorphic(LogEvent::class) {
                subclass(MoveActionSuccess::class)
            }
        }
}

package it.unibo.jakta.exp.base.gridworld.serialization

import it.unibo.jakta.agents.bdi.engine.logging.events.LogEvent
import it.unibo.jakta.agents.bdi.engine.serialization.modules.SerializersModuleProvider
import it.unibo.jakta.exp.base.gridworld.logging.ObjectReachedEvent
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class GridWorldJsonModule : SerializersModuleProvider {
    override val modules =
        SerializersModule {
            polymorphic(LogEvent::class) {
                subclass(ObjectReachedEvent::class)
            }
        }
}

package it.unibo.jakta.scheduler.server.domain

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ScheduledJob(
    val id: String,
    val name: String,
    val commandTemplate: List<String>,
    val parameters: Map<String, Set<String>>,
    val cachePath: String? = null,
    val maxParallel: Int = 1,
    val createdAt: Long = Instant.now().toEpochMilli(),
)

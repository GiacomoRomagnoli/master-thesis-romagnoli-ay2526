package it.unibo.jakta.scheduler.server.domain

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class ScheduledJob(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val commandTemplate: List<String>,
    val parameters: Map<String, List<String>>,
    val cachePath: String? = null,
    val maxParallel: Int = 1,
    val createdAt: Long = Instant.now().toEpochMilli(),
)

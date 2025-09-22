package it.unibo.jakta.scheduler.client

import kotlinx.serialization.Serializable

@Serializable
data class CreateJobResponse(
    val id: String,
)

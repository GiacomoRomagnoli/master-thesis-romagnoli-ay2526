package it.unibo.jakta.scheduler.server.domain

enum class JobStatus {
    CREATED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
}

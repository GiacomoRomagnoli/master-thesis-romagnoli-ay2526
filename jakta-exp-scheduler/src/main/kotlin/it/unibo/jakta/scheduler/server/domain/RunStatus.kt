package it.unibo.jakta.scheduler.server.domain

enum class RunStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
    CACHED,
}

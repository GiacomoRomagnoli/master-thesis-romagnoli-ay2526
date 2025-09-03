package it.unibo.jakta.evals.retrievers

interface Retriever<T> {
    fun retrieve(): T
}

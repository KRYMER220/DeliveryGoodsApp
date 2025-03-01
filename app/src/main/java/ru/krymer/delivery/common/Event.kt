package ru.krymer.delivery.common

interface EventHandler<E> {

    fun obtainEvent(event: E)
}
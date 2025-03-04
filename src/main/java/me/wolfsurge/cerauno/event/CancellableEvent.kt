package me.wolfsurge.cerauno.event

/**
 * A basic cancellable event that extends off of the [Event] class
 *
 * @author Wolfsurge
 */
open class CancellableEvent : Event() {

    private var cancelled = false

    fun cancel() {
        cancelled = true
    }

    fun isCancelled() = cancelled

}
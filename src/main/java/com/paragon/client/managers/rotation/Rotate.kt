package com.paragon.client.managers.rotation

/**
 * @author Wolfsurge
 * @since 23/03/22
 */
enum class Rotate {
    /**
     * Rotate with a packet
     */
    PACKET,

    /**
     * Move the players head
     */
    LEGIT,

    /**
     * Don't rotate
     */
    NONE
}
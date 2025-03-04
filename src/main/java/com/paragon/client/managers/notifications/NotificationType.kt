package com.paragon.client.managers.notifications

import java.awt.Color

/**
 * @author Wolfsurge
 */
enum class NotificationType(val colour: Int) {

    INFO(Color.GREEN.rgb),
    WARNING(Color.ORANGE.rgb),
    ERROR(Color.RED.rgb)

}
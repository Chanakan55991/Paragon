package com.paragon.client.managers.notifications

import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.ITextRenderer
import com.paragon.client.systems.module.hud.impl.Notifications
import com.paragon.client.ui.animation.Animation
import com.paragon.client.ui.animation.Easing

/**
 * @author SooStrator1136
 */
class Notification(val message: String, val type: NotificationType) :
    ITextRenderer {

    val animation: Animation = Animation({ 500f }, false, { Easing.EXPO_IN_OUT })
    private var started = false
    private var reachedFirst = false
    private var renderTicks = 0

    fun render(y: Float) {
        if (!started) {
            animation.state = true
            started = true
        }

        val width = getStringWidth(message) + 10
        val x = Notifications.INSTANCE.x

        RenderUtil.startGlScissor(Notifications.INSTANCE.x + (150 - 150) * animation.getAnimationFactor(), y.toDouble(), 300 * animation.getAnimationFactor(), 45.0)
        RenderUtil.drawRect(x + 150 - width / 2f, y, width, 30f, -0x70000000)
        renderCenteredString(message, x + 150, y + 15f, -1, true)
        RenderUtil.drawRect(x + 150 - width / 2f, y, width, 1f, type.colour)
        RenderUtil.endGlScissor()

        if (animation.getAnimationFactor() == 1.0 && !reachedFirst) {
            reachedFirst = true
        }
        if (reachedFirst) {
            renderTicks++
        }
        if (renderTicks == 300) {
            animation.state = false
        }
    }

    fun hasFinishedAnimating() = animation.getAnimationFactor() == 0.0 && reachedFirst

}
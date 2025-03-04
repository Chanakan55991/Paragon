package com.paragon.client.systems.module.hud.impl

import com.paragon.api.setting.Setting
import com.paragon.client.systems.module.hud.HUDModule
import java.awt.Color

/**
 * @author Wolfsurge
 */
object CustomText : HUDModule("CustomText", "Display custom text of your choice!") {

    private val text = Setting("Text", "Paragon on top!")
        .setDescription("The text to display")

    private val textColour = Setting("TextColour", Color.WHITE)
        .setDescription("The colour of the text")

    override fun render() {
        renderText(text.value, x, y, textColour.value.rgb)
    }

    override fun getWidth() = getStringWidth(text.value)

    override fun getHeight() = fontHeight

}
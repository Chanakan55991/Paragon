package com.paragon.client.managers.alt

import com.paragon.Paragon
import com.paragon.asm.mixins.accessor.IMinecraft
import com.paragon.client.ui.alt.AltManagerGUI
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator
import net.minecraft.client.Minecraft
import net.minecraft.util.Session
import net.minecraft.util.text.TextFormatting

/**
 * @author Wolfsurge, SooStrator1136
 */
class Alt(val email: String, val password: String) {

    private var session: Session? = null

    /**
     * Logs the user into an alt account
     */
    fun login(): Boolean {
        if (session == null) {
            val auth = MicrosoftAuthenticator() //Authenticator
            Paragon.INSTANCE.logger.info("logging in with $email")
            try {
                val result = auth.loginWithCredentials(email, password) //Get auth result
                session = Session(result.profile.name, result.profile.id, result.accessToken, "legacy") //Set alt session
            } catch (e: MicrosoftAuthenticationException) {
                e.printStackTrace()
            }
        }

        //Return false if the session is null
        if (session == null) {
            AltManagerGUI.renderString = TextFormatting.RED.toString() + "Unsuccessful Login!"
            return false
        }

        //Set Minecraft session
        (Minecraft.getMinecraft() as IMinecraft).session = session
        AltManagerGUI.renderString = TextFormatting.GREEN.toString() + "Successful Login!"

        return true
    }

}
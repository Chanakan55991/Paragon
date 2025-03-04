package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.event.player.TravelEvent;
import com.paragon.api.util.player.PlayerUtil;
import com.paragon.api.util.string.StringUtil;
import com.paragon.asm.mixins.accessor.IMinecraft;
import com.paragon.asm.mixins.accessor.ITimer;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.client.CPacketEntityAction;

/**
 * @author Wolfsurge
 */
public class ElytraFlight extends Module {

    public static ElytraFlight INSTANCE;
    
    // Mode for elytra flight
    public static Setting<Mode> mode = new Setting<>("Mode", Mode.CONTROL)
            .setDescription("The mode to use");

    // Strict settings
    public static Setting<Float> ascendPitch = new Setting<>("AscendPitch", -45f, -90f, 90f, 1f)
            .setDescription("What value to set your pitch to when ascending")
            .setParentSetting(mode)
            .setVisibility(() -> mode.getValue().equals(Mode.STRICT));

    public static Setting<Float> descendPitch = new Setting<>("DescendPitch", 45f, -90f, 90f, 1f)
            .setDescription("What value to set your pitch to when descending")
            .setParentSetting(mode)
            .setVisibility(() -> mode.getValue().equals(Mode.STRICT));

    public static Setting<Boolean> lockPitch = new Setting<>("LockPitch", true)
            .setDescription("Lock your pitch when you are not ascending or descending")
            .setParentSetting(mode)
            .setVisibility(() -> mode.getValue().equals(Mode.STRICT));

    public static Setting<Float> lockPitchVal = new Setting<>("LockedPitch", 0f, -90f, 90f, 1f)
            .setDescription("The pitch to lock you to when you are not ascending or descending")
            .setParentSetting(mode)
            .setVisibility(() -> mode.getValue().equals(Mode.STRICT));

    // Boost settings
    public static Setting<Boolean> cancelMotion = new Setting<>("CancelMotion", false)
            .setDescription("Stop motion when not moving")
            .setParentSetting(mode)
            .setVisibility(() -> mode.getValue().equals(Mode.BOOST));

    // Global settings
    public static Setting<Float> flySpeed = new Setting<>("FlySpeed", 1f, 0.1f, 2f, 0.1f)
            .setDescription("The speed to fly at");

    public static Setting<Float> ascend = new Setting<>("AscendSpeed", 1f, 0.1f, 2f, 0.1f)
            .setDescription("How fast to ascend")
            .setVisibility(() -> !mode.getValue().equals(Mode.BOOST));

    public static Setting<Float> descend = new Setting<>("DescendSpeed", 1f, 0.1f, 2f, 0.1f)
            .setDescription("How fast to descend")
            .setVisibility(() -> !mode.getValue().equals(Mode.BOOST));

    public static Setting<Float> fallSpeed = new Setting<>("FallSpeed", 0f, 0f, 0.1f, 0.01f)
            .setDescription("How fast to fall");

    // Takeoff settings
    public static Setting<Boolean> takeOff = new Setting<>("Takeoff", false)
            .setDescription("Automatically take off when you enable the module");

    public static Setting<Float> takeOffTimer = new Setting<>("Timer", 0.2f, 0.1f, 1f, 0.1f)
            .setDescription("How long a tick lasts for")
            .setParentSetting(takeOff);

    public ElytraFlight() {
        super("ElytraFlight", Category.MOVEMENT, "Allows for easier flight with an elytra");

        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            return;
        }

        if (takeOff.getValue()) {
            // Make sure we aren't elytra flying
            if (!mc.player.isElytraFlying()) {
                // Make the game slower
                ((ITimer) ((IMinecraft) mc).getTimer()).setTickLength(50 / takeOffTimer.getValue());

                if (mc.player.onGround) {
                    // Jump if we're on the ground
                    mc.player.jump();
                } else {
                    // Make us fly if we are off the ground
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                }
            }
        }
    }

    @Override
    public void onDisable() {
        // Set us back to normal speed
        ((ITimer) ((IMinecraft) mc).getTimer()).setTickLength(50);
    }

    @Listener
    public void onTravel(TravelEvent travelEvent) {
        if (nullCheck()) {
            return;
        }

        if (mc.player.isElytraFlying()) {
            // Set us to normal speed if we are flying
            ((ITimer) ((IMinecraft) mc).getTimer()).setTickLength(50);

            if (mode.getValue() != Mode.BOOST) {
                // Cancel motion
                travelEvent.cancel();

                // Make us fall
                PlayerUtil.stopMotion(-fallSpeed.getValue());
            } else {
                if (cancelMotion.getValue()) {
                    // Cancel motion
                    travelEvent.cancel();

                    // Make us fall
                    PlayerUtil.stopMotion(-fallSpeed.getValue());
                }
            }

            switch (mode.getValue()) {
                case CONTROL:
                    // Move
                    PlayerUtil.move(flySpeed.getValue());

                    // Handle moving up and down
                    handleControl();
                    break;
                case STRICT:
                    // Move
                    PlayerUtil.move(flySpeed.getValue());

                    // Handle moving up and down
                    handleStrict();
                    break;
                case BOOST:
                    if (mc.gameSettings.keyBindForward.isKeyDown() && !(mc.player.posX - mc.player.lastTickPosX > flySpeed.getValue() || mc.player.posZ - mc.player.lastTickPosZ > flySpeed.getValue())) {
                        // Move forward
                        PlayerUtil.propel(flySpeed.getValue() * (cancelMotion.getValue() ? 1 : 0.015f));
                    }
                    break;
            }

            // Lock our limbs
            PlayerUtil.lockLimbs();
        }
    }

    public void handleControl() {
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            // Increase Y
            mc.player.motionY = ascend.getValue();
        }

        if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            // Decrease Y
            mc.player.motionY = -descend.getValue();
        }
    }

    public void handleStrict() {
        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            // Increase pitch
            mc.player.rotationPitch = ascendPitch.getValue();

            // Increase Y
            mc.player.motionY = ascend.getValue();
        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            // Decrease pitch
            mc.player.rotationPitch = descendPitch.getValue();

            // Decrease Y
            mc.player.motionY = -descend.getValue();
        } else {
            if (lockPitch.getValue()) {
                // Set pitch if we aren't moving
                mc.player.rotationPitch = lockPitchVal.getValue();
            }
        }
    }

    @Override
    public String getData() {
        return " " + StringUtil.getFormattedText(mode.getValue());
    }

    public enum Mode {
        /**
         * Lets you fly without idle gliding
         */
        CONTROL,

        /**
         * Lets you fly on strict servers
         */
        STRICT,

        /**
         * Boost yourself when using an elytra
         */
        BOOST
    }

}

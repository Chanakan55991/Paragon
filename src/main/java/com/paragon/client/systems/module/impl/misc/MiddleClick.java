package com.paragon.client.systems.module.impl.misc;

import com.paragon.Paragon;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.client.managers.social.Player;
import com.paragon.client.managers.social.Relationship;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

/**
 * @author Wolfsurge
 */
public class MiddleClick extends Module {

    public static MiddleClick INSTANCE;

    public static Setting<Boolean> friend = new Setting<>("Friend", true)
            .setDescription("Add a friend when you middle click on an player");

    public static Setting<Boolean> pearl = new Setting<>("Pearl", true)
            .setDescription("Throw an ender pearl when you miss an entity");

    // To prevent excessive spam
    private boolean hasClicked = false;

    public MiddleClick() {
        super("MiddleClick", Category.MISC, "Allows you to perform actions when you middle click");

        INSTANCE = this;
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (nullCheck()) {
            return;
        }

        // Check that middle click button is pressed, and we haven't just clicked
        if (Mouse.isButtonDown(2)) {
            RayTraceResult.Type result = mc.objectMouseOver.typeOfHit;

            if (!hasClicked) {
                // If the type of hit is a player
                if (result.equals(RayTraceResult.Type.ENTITY) && mc.objectMouseOver.entityHit instanceof EntityPlayer && friend.getValue()) {
                    // Create new player object
                    Player player = new Player(mc.objectMouseOver.entityHit.getName(), Relationship.FRIEND);

                    if (Paragon.INSTANCE.getSocialManager().isFriend(player.getName())) {
                        // Remove player from social list
                        Paragon.INSTANCE.getSocialManager().removePlayer(player.getName());
                        Paragon.INSTANCE.getCommandManager().sendClientMessage(TextFormatting.RED + "Removed player " + TextFormatting.GRAY + player.getName() + TextFormatting.RED + " from your socials list!", false);
                    } else {
                        // Add player to social list
                        Paragon.INSTANCE.getSocialManager().addPlayer(player);
                        Paragon.INSTANCE.getCommandManager().sendClientMessage(TextFormatting.GREEN + "Added player " + TextFormatting.GRAY + player.getName() + TextFormatting.GREEN + " to your friends list!", false);
                    }
                }

                else if (pearl.getValue()) {
                    // The last slot we were on
                    int prevSlot = mc.player.inventory.currentItem;

                    // Switch to pearl, if we can
                    if (InventoryUtil.switchToItem(Items.ENDER_PEARL, false)) {
                        // Throw pearl
                        mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                        // Switch back to old slot
                        InventoryUtil.switchToSlot(prevSlot, false);
                    }
                }
            }

            // We have clicked
            this.hasClicked = true;
        } else {
            // Reset hasClicked
            this.hasClicked = false;
        }
    }
}

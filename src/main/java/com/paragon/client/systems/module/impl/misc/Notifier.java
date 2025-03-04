package com.paragon.client.systems.module.impl.misc;

import com.paragon.Paragon;
import com.paragon.api.event.client.ModuleToggleEvent;
import com.paragon.api.event.combat.PlayerDeathEvent;
import com.paragon.api.event.combat.TotemPopEvent;
import com.paragon.client.managers.notifications.Notification;
import com.paragon.client.managers.notifications.NotificationType;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import com.paragon.client.systems.module.hud.HUDModule;
import me.wolfsurge.cerauno.listener.Listener;

/**
 * @author Wolfsurge
 */
public class Notifier extends Module {

    public static Notifier INSTANCE;

    public static Setting<Boolean> moduleEnabled = new Setting<>("ModuleToggle", false)
            .setDescription("Notifies you when you toggle a module");

    public static Setting<Boolean> pop = new Setting<>("Pop", true)
            .setDescription("Notifies you when a player pops a totem");

    public static Setting<Boolean> death = new Setting<>("Death", true)
            .setDescription("Notifies you when a player dies");

    public static Setting<Boolean> noPops = new Setting<>("NoPops", true)
            .setDescription("Notifies you even if the player hasn't popped any totems")
            .setParentSetting(death);

    public Notifier() {
        super("Notifier", Category.MISC, "Notifies you when events happen");

        INSTANCE = this;
    }

    @Listener
    public void onModuleToggle(ModuleToggleEvent moduleToggleEvent) {
        if (moduleEnabled.getValue()) {
            if (!moduleToggleEvent.getModule().isIgnored() && !(moduleToggleEvent.getModule() instanceof HUDModule)) {
                Paragon.INSTANCE.getNotificationManager().addNotification(new Notification(moduleToggleEvent.getModule().getName() + " was " + (moduleToggleEvent.getModule().isEnabled() ? "Enabled" : "Disabled"), NotificationType.INFO));
            }
        }
    }

    @Listener
    public void onTotemPop(TotemPopEvent event) {
        if (pop.getValue()) {
            Paragon.INSTANCE.getNotificationManager().addNotification(new Notification(event.getPlayer().getName() + " has popped " + Paragon.INSTANCE.getPopManager().getPops(event.getPlayer()) + " totems!", NotificationType.INFO));
        }
    }

    @Listener
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (death.getValue()) {
            if (!noPops.getValue() && event.getPops() == 0) {
                return;
            }

            Paragon.INSTANCE.getNotificationManager().addNotification(new Notification(event.getEntityPlayer().getName() + " has died after popping " + event.getPops() + " totems!", NotificationType.INFO));
        }
    }

}

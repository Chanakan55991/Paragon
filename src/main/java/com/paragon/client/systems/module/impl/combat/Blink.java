package com.paragon.client.systems.module.impl.combat;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.util.calculations.Timer;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.KeybindSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Blink extends Module {

    private final Queue<CPacketPlayer> packetQueue = new LinkedList<>();
    private EntityOtherPlayerMP fakePlayer;

    public Blink() {
        super("Blink", ModuleCategory.COMBAT, "Cancels sending packets for a length of time");
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            return;
        }

        fakePlayer = new EntityOtherPlayerMP(mc.world, mc.getSession().getProfile());
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        mc.world.addEntityToWorld(-351352, fakePlayer);
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            return;
        }

        while (!packetQueue.isEmpty()) {
            mc.player.connection.sendPacket(packetQueue.poll());
        }

        if (mc.player != null) {
            mc.world.removeEntityFromWorld(-351352);
            fakePlayer = null;
        }
    }

    @Listener
    public void onPacketSent(PacketEvent.PreSend preSend) {
        if (preSend.getPacket() instanceof CPacketPlayer) {
            preSend.cancel();
            packetQueue.add((CPacketPlayer) preSend.getPacket());
        }
    }

}

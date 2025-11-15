package com.escape.banishmentmod;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(BanishmentMod.MODID)
public class BanishmentMod {

    public static final String MODID = "banishmentmod";

    public BanishmentMod() {
        System.out.println("[BanishmentMod] Loaded!");

        // REGISTER THIS CLASS TO THE NEOFORGE GLOBAL BUS
        NeoForge.EVENT_BUS.register(this);
    }

    // This MUST be non-static for NeoForge to detect it when registered via instance
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        System.out.println("[BanishmentMod] Registering commands...");
        BanishCommand.register(event.getDispatcher());
    }
}

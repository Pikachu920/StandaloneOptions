package com.pikachu.standaloneoptions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class StandaloneOptions extends JavaPlugin {

    private static SkriptAddon addonInstance;
    private static StandaloneOptions instance;

    @Override
    public void onEnable() {
        instance = this;
        try {
            getAddonInstance().loadClasses("com.pikachu.standaloneoptions", "skript");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SkriptAddon getAddonInstance() {
        if (addonInstance == null) {
            addonInstance = Skript.registerAddon(getInstance());
        }
        return addonInstance;
    }

    public static StandaloneOptions getInstance() {
        if (instance == null) {
             instance = new StandaloneOptions();
        }
        return instance;
    }

}

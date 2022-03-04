package me.maanraj514;

import org.bukkit.plugin.java.JavaPlugin;

public final class LepvpRemastered extends JavaPlugin {
    private static LepvpRemastered instance;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
    }

    public static LepvpRemastered getInstance() {
        return instance;
    }
}

package me.maanraj514.game;

import dev.nova.gameapi.game.base.GameBase;
import dev.nova.gameapi.game.logger.GameLog;
import dev.nova.gameapi.game.logger.GameLogger;
import dev.nova.gameapi.game.logger.LogLevel;
import dev.nova.gameapi.utils.Files;
import me.maanraj514.LepvpRemastered;
import me.maanraj514.game.instance.messages.KillMessageCollection;
import me.maanraj514.game.instance.solo.LepvpRemasteredSoloInstance;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class LepvpRemasteredGameBase extends GameBase {
    private final HashMap<String, KillMessageCollection> messages;

    public File killMessagesFolder;

    public LepvpRemasteredGameBase() {
        super(LepvpRemastered.getInstance(), "lepvp", "Lepvp", new Class[] { LepvpRemasteredSoloInstance.class }, ChatColor.GREEN, false);
        this.messages = new HashMap<>();
    }

    public boolean onInitialize() {
        this.killMessagesFolder = new File(Files.getGameFolder(getCodeName()), "kill-messages");
        this.killMessagesFolder.mkdirs();
        File defaultKillMessages = new File(this.killMessagesFolder, "default.yml");
        try {
            defaultKillMessages.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        YamlConfiguration defaultConfiguration = new YamlConfiguration();
        defaultConfiguration.set("data.name", "Default");
        defaultConfiguration.set("data.rarity", "DEFAULT");
        defaultConfiguration.set("messages.void.accidental", "%player% &7fell into the void!");
        defaultConfiguration.set("messages.void.with-killer", "%player% &7was hit into the void by %killer%!");
        defaultConfiguration.set("messages.kill.accidental", "%player% &7died!");
        defaultConfiguration.set("messages.kill.with-killer", "%player% &7was killed by %killer%!");
        defaultConfiguration.set("messages.burn.accidental", "%player% &7burned to death!");
        defaultConfiguration.set("messages.burn.with-killer", "%victim% &7was thrown into a pit of lava by %killer%!");
        defaultConfiguration.set("messages.arrow.accidental", "%player% &7had a mysterious arrow fall on their head!");
        defaultConfiguration.set("messages.arrow.with-killer", "%victim% &7was shot by %killer%!");
        defaultConfiguration.set("messages.explosion.accidental", "%player% &7exploded!");
        defaultConfiguration.set("messages.explosion.with-killer", "%victim% &7was boomed by %killer%!");
        try {
            defaultConfiguration.save(defaultKillMessages);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadKillMessages(this.killMessagesFolder);
        return true;
    }

    private void loadKillMessages(File killMessagesFolder) {
        GameLogger.log(new GameLog(this, LogLevel.INFO, "Loading kill messages at: " + killMessagesFolder.getPath(), true));
        for (File file : killMessagesFolder.listFiles()) {
            if (file.isDirectory()) {
                loadKillMessages(file);
            } else if (file.getName().endsWith(".yml") && !file.getName().startsWith("-")) {
                loadKillMessagesFile(file);
            }
        }
    }

    private void loadKillMessagesFile(File file) {
        try {
            YamlConfiguration configuration = new YamlConfiguration();
            configuration.load(file);
            if (!configuration.contains("data") || !configuration.contains("messages"))
                return;
            this.messages.put(configuration.getString("data.name"), new KillMessageCollection(configuration.getConfigurationSection("messages"), KillMessageCollection.Rarity.valueOf(configuration.getString("data.rarity").toUpperCase())));
            GameLogger.log(new GameLog(this, LogLevel.INFO, "Kill Message Collection: " + configuration.getString("data.name") + " has been loaded.", true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public KillMessageCollection getDefaultKillMessages() {
        return this.messages.get("Default");
    }
}

package me.maanraj514.game.instance.messages;

import dev.nova.gameapi.game.player.GamePlayer;
import me.maanraj514.game.instance.LepvpRemasteredBaseInstance;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class KillMessageCollection {
    private final ConfigurationSection section;

    private final Rarity rarity;

    public enum Rarity {
        COMMON, RARE, EPIC, LEGENDARY, DEFAULT;
    }

    public KillMessageCollection(ConfigurationSection section, Rarity rarity) {
        this.section = section;
        this.rarity = rarity;
    }

    public Rarity getRarity() {
        return this.rarity;
    }

    public ConfigurationSection getSection() {
        return this.section;
    }

    public String getAccidentalMessage(String key, LepvpRemasteredBaseInstance instance, GamePlayer player) {
        if (!this.section.contains(key + ".accidental"))
            return getAccidentalMessage("kill", instance, player);
        String raw = ChatColor.translateAlternateColorCodes('&', this.section.getString(key + ".accidental"));
        raw = raw.replaceAll("%player%", "" + instance.getTeam(player).getColor() + instance.getTeam(player).getColor());
        raw = raw.replaceAll("%player-team%", "" + instance.getTeam(player).getColor() + instance.getTeam(player).getColor());
        if (key.equals("fall"))
            raw = raw.replaceAll("%distance%", String.valueOf((int)player.getPlayer().getFallDistance()));
        return raw;
    }

    public String getKillerMessage(String key, LepvpRemasteredBaseInstance instance, GamePlayer player, GamePlayer killer) {
        if (!this.section.contains(key + ".with-killer"))
            return getAccidentalMessage("kill", instance, player);
        String raw = ChatColor.translateAlternateColorCodes('&', this.section.getString(key + ".with-killer"));
        raw = raw.replaceAll("%player%", "" + instance.getTeam(player).getColor() + instance.getTeam(player).getColor());
        raw = raw.replaceAll("%player-team%", "" + instance.getTeam(player).getColor() + instance.getTeam(player).getColor());
        raw = raw.replaceAll("%killer%", "" + instance.getTeam(killer).getColor() + instance.getTeam(killer).getColor());
        raw = raw.replaceAll("%killer-team%", "" + instance.getTeam(killer).getColor() + instance.getTeam(killer).getColor());
        if (key.equals("arrow"))
            raw = raw.replaceAll("%distance%", String.valueOf((int)killer.getPlayer().getLocation().distance(player.getPlayer().getLocation())));
        return raw;
    }
}

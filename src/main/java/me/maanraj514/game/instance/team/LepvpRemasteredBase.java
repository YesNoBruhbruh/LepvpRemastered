package me.maanraj514.game.instance.team;

import dev.nova.gameapi.game.map.options.GameOption;
import me.maanraj514.game.instance.LepvpRemasteredBaseInstance;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class LepvpRemasteredBase {
    private final LepvpRemasteredBaseInstance instance;

    private final LepvpRemasteredTeam team;

    private final Location spawn;

    public LepvpRemasteredBase(LepvpRemasteredBaseInstance instance, LepvpRemasteredTeam team, ConfigurationSection config) {
        this.instance = instance;
        this.team = team;
        this.spawn = GameOption.convert(config.getLocation("spawn"), instance.getMap());
    }

    public Location getSpawn() {
        return this.spawn;
    }

    public LepvpRemasteredBaseInstance getInstance() {
        return this.instance;
    }

    public LepvpRemasteredTeam getTeam() {
        return this.team;
    }
}

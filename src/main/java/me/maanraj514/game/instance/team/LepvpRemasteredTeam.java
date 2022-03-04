package me.maanraj514.game.instance.team;

import dev.nova.gameapi.game.base.instance.team.Team;
import dev.nova.gameapi.game.map.options.OptionType;
import dev.nova.gameapi.game.player.GamePlayer;
import me.maanraj514.game.instance.LepvpRemasteredBaseInstance;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

public class LepvpRemasteredTeam extends Team {
    private final LepvpRemasteredBaseInstance instance;

    private final LepvpRemasteredBase base;

    private final HashMap<GamePlayer, Integer> scores;

    public LepvpRemasteredBaseInstance getInstance() {
        return this.instance;
    }

    public LepvpRemasteredTeam(int teamSize, ChatColor color, LepvpRemasteredBaseInstance instance) {
        super((color != ChatColor.LIGHT_PURPLE) ? ((color == ChatColor.DARK_GRAY) ? "Gray" : color.name().toLowerCase()) : "Pink", teamSize, color);
        this.instance = instance;
        this.scores = new HashMap<>();
        this.base = getBaseData();
    }

    public HashMap<GamePlayer, Integer> getScores() {
        return this.scores;
    }

    public LepvpRemasteredBase getBase() {
        return this.base;
    }

    public String getProperTeamName() {
        String str = getName().replaceAll("_", " ");
        return str.substring(0, 1).toUpperCase() + str.substring(0, 1).toUpperCase();
    }

    private LepvpRemasteredBase getBaseData() {
        ConfigurationSection section = this.instance.getMap().loadOption("base." + getColor().name().toLowerCase(), OptionType.CONFIGURATION_SECTION, true).getAsConfigurationSection();
        return new LepvpRemasteredBase(this.instance, this, section);
    }

    public String getSymbol() {
        return "" + getColor() + getColor();
    }
}

package me.maanraj514.game.instance.solo;

import dev.nova.gameapi.game.map.GameMap;
import me.maanraj514.game.instance.LepvpRemasteredBaseInstance;

public class LepvpRemasteredSoloInstance extends LepvpRemasteredBaseInstance {
    public static final String code = "solo";

    public LepvpRemasteredSoloInstance(String gameBase, GameMap map) {
        super("Solo", gameBase, map, 1);
    }
}

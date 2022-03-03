package me.maanraj514.game.instance;

import dev.nova.gameapi.game.base.instance.controller.GameController;
import dev.nova.gameapi.game.base.instance.team.Team;
import dev.nova.gameapi.game.base.instance.team.TeamGameInstance;
import dev.nova.gameapi.game.map.GameMap;
import dev.nova.gameapi.game.map.options.OptionType;
import dev.nova.gameapi.game.player.GamePlayer;
import me.maanraj514.game.LepvpRemasteredGameBase;
import me.maanraj514.game.instance.messages.KillMessageCollection;
import me.maanraj514.game.instance.team.LepvpRemasteredTeam;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class LepvpRemasteredBaseInstance extends TeamGameInstance {
    private static final Random LEPVP_RANDOM = ThreadLocalRandom.current();

    protected String[] leaveWinMessages = new String[] { "I think someone left lmao" };

    private final ArrayList<GamePlayer> died;

    private final KillMessageCollection killMessages;

    private final int maximumPoints;

    protected ChatColor[] teams = new ChatColor[] { ChatColor.BLUE, ChatColor.RED };

    public LepvpRemasteredTeam getTeam(GamePlayer player) {
        return (LepvpRemasteredTeam)super.getTeam(player);
    }

    public LepvpRemasteredGameBase getGameBase() {
        return (LepvpRemasteredGameBase) super.getGameBase();
    }

    public void point(GamePlayer killer, LepvpRemasteredTeam victim){
        int point = getTeam(killer).getScores().getOrDefault(killer, Integer.valueOf(0)).intValue() + 1;
        if (sumPoints(getTeam(killer)) > this.maximumPoints)
            return;
        getTeam(killer).getScores().put(killer, Integer.valueOf(point));
        LepvpRemasteredTeam team = getTeam(killer);
        getUtils().sendMessage("" + team.getColor() + team.getColor() + killer.getPlayer().getName() + " scored! (" + ChatColor.GRAY + "/" + sumPoints(team) + ") (" + this.maximumPoints + point + " Goal)");
        for (GamePlayer player : getPlayers())
            resetPlayerLepvp(player);
    }

    private int sumPoints(LepvpRemasteredTeam team) {
        int sum = 0;
        for (Integer integer : team.getScores().values()) {
            int score = integer.intValue();
            sum += score;
        }
        return sum;
    }

    public LepvpRemasteredBaseInstance(String displayName, String gameBase, GameMap map, int teamSize) {
        super(displayName, gameBase, map, new String[] { "Fight in a crystal pvp 1v1" }, "because you want to practice crystal pvp", true, new GameController[] { GameController.PLACE_BLOCKS, GameController.BREAK_BLOCKS, GameController.ITEM_PICKUP }, false);
        this.maximumPoints = this.map.loadOption("maximum_points", OptionType.INTEGER, true).getAsInt();
        this.died = new ArrayList<>();
        this.killMessages = getGameBase().getDefaultKillMessages();
        for (ChatColor color : this.teams)
            addTeam(new LepvpRemasteredTeam(teamSize, color, this));
    }

    public void onEvent(Event event){
        Event event2 = event;
        if (event2 instanceof PlayerDeathEvent deathEvent){
            if (deathEvent.getPlayer().getKiller() != null){
                Player victim = deathEvent.getPlayer();
                Player killer = victim.getKiller();
                if (victim.isDead()){
                    if (!this.players.contains(GamePlayer.getPlayer(victim)))
                        return;
                    for (Team teamRaw : getTeams()) {
                        LepvpRemasteredTeam team = (LepvpRemasteredTeam)teamRaw;
                        if (team == getTeam(GamePlayer.getPlayer(victim)))
                            continue;
                        if (!this.died.contains(GamePlayer.getPlayer(victim))) {
                            this.died.add(GamePlayer.getPlayer(victim));
                            point(GamePlayer.getPlayer(killer), team);
                            break;
                        }
                    }
                    getUtils().sendMessage(this.killMessages.getKillerMessage("kill", this, GamePlayer.getPlayer(victim), GamePlayer.getPlayer(killer)));
                    this.died.add(GamePlayer.getPlayer(victim));
                    resetPlayerLepvp(GamePlayer.getPlayer(victim));
                }
            }else{
                //deathEvent.getPlayer() and victim is the same thing I used getPlayer because its inside an else
                if (!this.died.contains(GamePlayer.getPlayer(deathEvent.getPlayer()))){
                    getUtils().sendMessage(this.killMessages.getAccidentalMessage("kill", this, GamePlayer.getPlayer(deathEvent.getPlayer())));

                    this.died.add(GamePlayer.getPlayer(deathEvent.getPlayer()));
                    resetPlayerLepvp(GamePlayer.getPlayer(deathEvent.getPlayer()));
                }
            }
        }
        event2 = event;
        if (event2 instanceof EntityDamageEvent damageEvent){
            if (!(damageEvent.getEntity() instanceof Player)){
                return;
            }
            Player victim = ((Player) damageEvent.getEntity());
            Player killer = victim.getKiller();

            if (!this.players.contains(GamePlayer.getPlayer(victim)))
                return;
            if (damageEvent.getFinalDamage() >= victim.getHealth() + victim.getAbsorptionAmount()){
                String cause = damageEvent.getCause().name().toLowerCase();
                if (damageEvent.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                    cause = "arrow";
                } else if (damageEvent.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) || damageEvent.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
                    cause = "explosion";
                } else if (damageEvent.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) || damageEvent.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
                    cause = "kill";
                }
                if (killer == null) {
                    getUtils().sendMessage(this.killMessages.getAccidentalMessage(cause, this, GamePlayer.getPlayer(victim)));
                } else {
                    getUtils().sendMessage(this.killMessages.getKillerMessage(cause, this, GamePlayer.getPlayer(victim), GamePlayer.getPlayer(killer)));
                }
                resetPlayerLepvp(GamePlayer.getPlayer(victim));
                damageEvent.setCancelled(true);
            }
        }
        Event event1 = event;
        if (event1 instanceof CraftItemEvent) {
            CraftItemEvent craftEvent = (CraftItemEvent)event1;
            craftEvent.setCancelled(true);
        }
        event1 = event;
        if (event1 instanceof InventoryClickEvent inventoryClickEvent) {
            if (inventoryClickEvent.getClickedInventory() instanceof org.bukkit.inventory.PlayerInventory && (
                    inventoryClickEvent.getSlot() == 36 || inventoryClickEvent.getSlot() == 37 || inventoryClickEvent
                            .getSlot() == 38 || inventoryClickEvent.getSlot() == 39)) {
                inventoryClickEvent.setCancelled(true);
                return;
            }
        }
        event1 = event;
        if (event1 instanceof EntityDamageByEntityEvent damageEvent) {
            if (!(damageEvent instanceof Player)){
                return;
            }
            Player damagerBukkit = (Player)damageEvent.getDamager(), playerBukkit = (Player)damageEvent.getEntity();

            GamePlayer player = GamePlayer.getPlayer(playerBukkit);
            GamePlayer damager = GamePlayer.getPlayer(damagerBukkit);
            if (getTeam(player) == getTeam(damager))
                damageEvent.setCancelled(true);
        }
        super.onEvent(event);
    }

    public void onStart() {
        super.onStart();
        for (GamePlayer player : getPlayers())
            resetPlayerLepvp(player);
    }

    protected boolean resetSpectator(GamePlayer gamePlayer) {
        gamePlayer.getPlayer().teleport(this.spectatorLocation);
        return true;
    }

    public void tick() {
        if (!this.ended) {
            ArrayList<Team> toRemove = new ArrayList<>();
            for (Team team : super.teams) {
                if (team.getPlayers().size() == 0) {
                    eliminate(team);
                    toRemove.add(team);
                }
            }
            super.teams.removeAll(toRemove);
            toRemove.clear();
            if (super.teams.size() == 1) {
                String message = (this.leaveWinMessages.length == 0 || this.leaveWinMessages.length == 1) ? "wow!" : this.leaveWinMessages[LEPVP_RANDOM.nextInt(this.leaveWinMessages.length)];
                win((LepvpRemasteredTeam) super.teams.get(0), message);
            } else {
                for (Team team : super.teams) {
                    LepvpRemasteredTeam bridgeTeam = (LepvpRemasteredTeam) team;
                    if (sumPoints(bridgeTeam) == this.maximumPoints) {
                        win(bridgeTeam, makeScoreMessage());
                        break;
                    }
                }
            }
        }
    }

    private String makeScoreMessage() {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Team t : super.teams) {
            LepvpRemasteredTeam team = (LepvpRemasteredTeam) t;
            builder.append(team.getColor()).append(ChatColor.BOLD).append(sumPoints(team)).append((super.teams.indexOf(team) != super.teams.size() - 1) ? ("" + ChatColor.RESET + ChatColor.RESET + " - ") : "");
            i++;
        }
        return builder.toString();
    }

    public void win(LepvpRemasteredTeam team, String message) {
        getUtils().sendTitle("" + team.getColor() + team.getColor() + ChatColor.BOLD + " WINS!", message, 0, 360, 0);
        for (GamePlayer player : getPlayers())
            resetPlayerLepvp(player);
        onEnd(false);
    }

    public void resetPlayerLepvp(GamePlayer player) {
        normalReset(player, GameMode.SURVIVAL);
        player.getPlayer().teleport(getTeam(player).getBase().getSpawn());
        this.died.remove(player);
        giveKit(player);
    }

    public boolean resetPlayer(GamePlayer player) {
        player.getPlayer().teleport(getTeam(player).getBase().getSpawn());
        this.died.remove(player);
        return true;
    }

    private void giveKit(GamePlayer player) {
        player.getPlayer().getInventory().setHelmet(netheriteHelmet());
        player.getPlayer().getInventory().setChestplate(netheriteChestPlate());
        player.getPlayer().getInventory().setLeggings(netheriteLeggings());
        player.getPlayer().getInventory().setBoots(netheriteBoots());

        player.getPlayer().getInventory().setItem(0, netheriteSword());
        player.getPlayer().getInventory().setItem(1, new ItemStack(Material.ENDER_PEARL, 16));
        player.getPlayer().getInventory().setItem(4, new ItemStack(Material.GOLDEN_APPLE, 64));
        player.getPlayer().getInventory().setItem(2, new ItemStack(Material.OBSIDIAN, 64));
        player.getPlayer().getInventory().setItem(3, new ItemStack(Material.END_CRYSTAL, 64));
        player.getPlayer().getInventory().setItem(5, Crossbow());
        player.getPlayer().getInventory().setItem(6, new ItemStack(Material.TOTEM_OF_UNDYING));
        Potion speed = new Potion(PotionType.SPEED, 2);
        speed.setSplash(true);
        ItemStack item = speed.toItemStack(1);
        player.getPlayer().getInventory().setItem(7, item);
        ItemStack result = new ItemStack(Material.TIPPED_ARROW, 64);
        PotionMeta resultMeta = (PotionMeta) result.getItemMeta();
        resultMeta.setBasePotionData(new PotionData(PotionType.SLOW_FALLING));
        resultMeta.addCustomEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 5, 1), true);
        result.setItemMeta(resultMeta);
        Potion strength = new Potion(PotionType.STRENGTH, 2);
        strength.setSplash(true);
        ItemStack strengthItem = strength.toItemStack(1);
        player.getPlayer().getInventory().setItem(8, strengthItem);
        player.getPlayer().getInventory().setItem(9, result);
        player.getPlayer().getInventory().setItem(10, new ItemStack(Material.EXPERIENCE_BOTTLE, 64));
        player.getPlayer().getInventory().setItem(11, netheritePicAxe());
        player.getPlayer().getInventory().setItem(12, item);
        player.getPlayer().getInventory().setItem(13, item);
        player.getPlayer().getInventory().setItem(14, item);
        player.getPlayer().getInventory().setItem(15, item);
        player.getPlayer().getInventory().setItem(16, item);
        player.getPlayer().getInventory().setItem(17, item);
        player.getPlayer().getInventory().setItem(18, new ItemStack(Material.ENDER_PEARL, 16));
        player.getPlayer().getInventory().setItem(19, new ItemStack(Material.ENDER_PEARL, 16));
        player.getPlayer().getInventory().setItem(20, new ItemStack(Material.TOTEM_OF_UNDYING));
        player.getPlayer().getInventory().setItem(21, strengthItem);
        player.getPlayer().getInventory().setItem(22, strengthItem);
        player.getPlayer().getInventory().setItem(23, strengthItem);
        player.getPlayer().getInventory().setItem(24, strengthItem);
        player.getPlayer().getInventory().setItem(25, strengthItem);
        player.getPlayer().getInventory().setItem(26, strengthItem);
        player.getPlayer().getInventory().setItem(27, new ItemStack(Material.ENDER_PEARL, 16));
        player.getPlayer().getInventory().setItem(28, new ItemStack(Material.ENDER_PEARL, 16));
        player.getPlayer().getInventory().setItem(29, new ItemStack(Material.TOTEM_OF_UNDYING));
        player.getPlayer().getInventory().setItem(30, item);
        player.getPlayer().getInventory().setItem(31, strengthItem);
        player.getPlayer().getInventory().setItem(32, item);
        player.getPlayer().getInventory().setItem(33, strengthItem);
        Potion turtle = new Potion(PotionType.TURTLE_MASTER, 2);
        turtle.setSplash(true);
        ItemStack turtleItem = turtle.toItemStack(1);
        player.getPlayer().getInventory().setItem(34, turtleItem);
        player.getPlayer().getInventory().setItem(35, turtleItem);
    }

    private ItemStack netheriteSword() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Netherite Sword");
        List<String> lore = new ArrayList<>();
        lore.add("it's just a sword >:)");
        meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
        meta.addEnchant(Enchantment.DAMAGE_ALL, 5, true);
        meta.addEnchant(Enchantment.SWEEPING_EDGE, 3, true);
        meta.addEnchant(Enchantment.DURABILITY, 3, true);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack netheritePicAxe() {
        ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Netherite Pickaxe");
        List<String> lore = new ArrayList<>();
        lore.add("it's just a pickaxe >:)");
        meta.addEnchant(Enchantment.DIG_SPEED, 5, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addEnchant(Enchantment.DURABILITY, 3, true);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack netheriteLeggings() {
        ItemStack item = new ItemStack(Material.NETHERITE_LEGGINGS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Netherite Leggings");
        List<String> lore = new ArrayList<>();
        lore.add("it's just a leggings >:)");
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4, true);
        meta.addEnchant(Enchantment.DURABILITY, 3, true);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack netheriteHelmet() {
        ItemStack item = new ItemStack(Material.NETHERITE_HELMET);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Netherite Helmet");
        List<String> lore = new ArrayList<>();
        lore.add("it's just a helmet >:)");
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
        meta.addEnchant(Enchantment.DURABILITY, 3, true);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack netheriteChestPlate() {
        ItemStack item = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Netherite Chestplate");
        List<String> lore = new ArrayList<>();
        lore.add("it's just a chestplate >:)");
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
        meta.addEnchant(Enchantment.DURABILITY, 3, true);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack netheriteBoots() {
        ItemStack item = new ItemStack(Material.NETHERITE_BOOTS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Netherite Boots");
        List<String> lore = new ArrayList<>();
        lore.add("it's just a boots >:)");
        meta.addEnchant(Enchantment.DEPTH_STRIDER, 3, true);
        meta.addEnchant(Enchantment.PROTECTION_FALL, 4, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
        meta.addEnchant(Enchantment.DURABILITY, 3, true);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack Crossbow() {
        ItemStack item = new ItemStack(Material.CROSSBOW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Crossbow");
        List<String> lore = new ArrayList<>();
        lore.add("it's just a crossbow >:)");
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addEnchant(Enchantment.MULTISHOT, 1, true);
        meta.addEnchant(Enchantment.QUICK_CHARGE, 3, true);
        meta.addEnchant(Enchantment.DURABILITY, 3, true);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
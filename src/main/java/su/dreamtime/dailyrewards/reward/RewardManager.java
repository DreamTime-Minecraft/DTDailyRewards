package su.dreamtime.dailyrewards.reward;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import ru.sgk.dreamtimeapi.gui.GUIInventory;
import ru.sgk.dreamtimeapi.gui.GUIItem;
import ru.sgk.dreamtimeapi.gui.GUIManager;
import su.dreamtime.dailyrewards.DailyRewards;
import su.dreamtime.dailyrewards.util.DRTimeUnit;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardManager {
    private static Map<String, PlayerReward> players = new HashMap<>();
    private static Map<String, Reward> rewards = new HashMap<>(); // Key - reward id; value - reward obj
    private static GUIManager guiManager;
    private static DailyRewards plugin;
    private static final String inventoryTitle = ChatColor.translateAlternateColorCodes('&', "&eЕжедневные награды");

    public static void init(DailyRewards plugin) {
        RewardManager.plugin = plugin;
        FileConfiguration config = plugin.getMainConfig().getConfig();
        loadRewards();
        loadPlayers();
        List<String> gui = config.getStringList("gui");
        guiManager = parseGUI(gui);
    }

    private static synchronized void loadRewards() {
        rewards = new HashMap<>();
        ConfigurationSection rewardsSection = plugin.getMainConfig().getConfig().getConfigurationSection("rewards");
        for (Map.Entry<String, Object> value : rewardsSection.getValues(false).entrySet()) {
            String rewardId = value.getKey();
            String title = ChatColor.translateAlternateColorCodes('&', rewardsSection.getString(rewardId + ".title"));
            String permission = rewardsSection.getString(rewardId+".permission");
            long coolDown = rewardsSection.getLong(rewardId+".cooldown");
            DRTimeUnit tUnit = DRTimeUnit.valueOf(rewardsSection.getString(rewardId+".time-unit"));
            List<String> commands = rewardsSection.getStringList(rewardId+".commands");
            char guiChar = rewardsSection.getString(rewardId+".gui-char").charAt(0);
            String materialString = rewardsSection.getString(rewardId+".gui-item");
            String[] data = materialString.split(":");

            Material material = Material.matchMaterial(data[0]);
            MaterialData md = new MaterialData(material);
            if (data.length > 1) {
                md.setData(Byte.parseByte(data[1]));
            }

            List<String> tmp = rewardsSection.getStringList(rewardId + ".description");
            List<String> description = new ArrayList<>();
            for (String s : tmp) {
                description.add(ChatColor.translateAlternateColorCodes('&', s));
            }
            Reward reward = new Reward(title, permission, coolDown, tUnit, commands, guiChar, material, description, md);
            rewards.put(rewardId, reward);
        }
    }

    private static synchronized void loadPlayers() {
        players = new HashMap<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            addPlayer(p);
        }
    }

    public static Map<String, PlayerReward> getPlayers() {
        return players;
    }

    public static Map<String, Reward> getRewards() {
        return rewards;
    }

    public static synchronized void reload()
    {
        if (plugin == null) {
            Bukkit.getLogger().info(ChatColor.RED + "Плагин не может быть перезагружен, т.к. он не был инициализирован!");
            return;
        }
        plugin.getMainConfig().reloadConfig();
        plugin.setMainConfigDefaults();

        for (PlayerReward playerReward : players.values()) {
            savePlayer(playerReward.getPlayer());
        }
        plugin.getPlayerConfig().forceSave();
        players.clear();
        rewards.clear();

        loadRewards();
        loadPlayers();

    }

    public static synchronized void unload() {
        for (PlayerReward playerReward : players.values()) {
            savePlayer(playerReward.getPlayer(), true);
        }
        plugin.getPlayerConfig().forceSave();
        players.clear();
        rewards.clear();
    }

    private static GUIManager parseGUI(List<String> guiChars) {
        GUIManager manager = new GUIManager(plugin);
        GUIInventory inv = manager.addInventory(inventoryTitle, guiChars.size());
        ConfigurationSection otherItemsSect = plugin.getMainConfig().getConfig().getConfigurationSection("items");
        Map<Character, MaterialData> otherItems = new HashMap<>();
        for (String s : otherItemsSect.getValues(false).keySet()) {
            char guiChar = otherItemsSect.getString(s + ".gui-char").charAt(0);
            String matStr = otherItemsSect.getString(s + ".gui-item");

            String[] data = matStr.split(":");

            MaterialData material = new MaterialData(Material.matchMaterial(data[0]));
            if (data.length > 1) {
                material.setData(Byte.parseByte(data[1]));
            }

            otherItems.put(guiChar, material );
        }
        int x = 1;
        int y = 1;
        for (String s : guiChars) {
            if (y > 5) break;
            for (char c : s.toCharArray()) {
                if (y > 9) break;
                boolean cont = false;
                for (Map.Entry<Character, MaterialData> entry : otherItems.entrySet()) {
                    if (entry.getKey() == c) {
                        Material mat = entry.getValue().getItemType();
                        ItemStack is = new ItemStack(mat, 1, (short)0, entry.getValue().getData());
                        GUIItem item = inv.addItem(is, x, y);
                        item.setTitle("");
                        item.setLore(null);
                        item.createHandler(e -> e.setCancelled(true));
                    }
                }
                if (cont) continue;
                for (Map.Entry<String, Reward> entry: rewards.entrySet()) {
                    Reward r = entry.getValue();
                    if (c == r.getGuiChar()) {

                        Material mat = entry.getValue().getMatData().getItemType();
                        ItemStack is = new ItemStack(mat, 1, (short)0, entry.getValue().getMatData().getData());

                        GUIItem item = inv.addItem(is, x, y);
                        item.setTitle(entry.getKey());
                        item.setLore(r.getDescription());
                        item.createHandler((event) -> {
                            event.setCancelled(true);
                            Player p = (Player) event.getView().getPlayer();
                            String uuid = p.getUniqueId().toString();
                            PlayerReward playerReward = players.get(uuid);
                            if (playerReward.giveReward(entry.getKey())) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aНаграда выдана. Вы сможете получить награду снова через: " + getTimeString(entry.getValue().getCoolDownMills())));
                                event.getView().close();
                            }
                        });
                        break;
                    }
                }
                x++;
            }
            x = 1;
            y++;
        }
        inv.setOpenHandler((event, inventory)->{
            for (GUIItem i : inventory.getItems()) {

                ItemStack item = i.getItem().clone();
                ItemMeta meta = item.getItemMeta();
                String itemName = item.getItemMeta() != null  && item.getItemMeta().hasDisplayName() ? ChatColor.stripColor(item.getItemMeta().getDisplayName()) : null;
                Reward reward = rewards.get(itemName);
                if (reward != null) {
                    PlayerReward rPlayer = players.get(event.getView().getPlayer().getUniqueId().toString());
                    long time = 0;
                    if (rPlayer.getLastTakeTime().get(itemName) != null) {
                        time = rPlayer.getLastTakeTime().get(itemName).getTime() + reward.getCoolDownMills() - System.currentTimeMillis();
                    }
                    if (!rPlayer.getPlayer().hasPermission(reward.getPermission())) {
                        List<String> lore = item.getItemMeta().getLore() == null ? new ArrayList<>() : item.getItemMeta().getLore();
                        lore.add(ChatColor.translateAlternateColorCodes('&', "&r"));
                        lore.add(ChatColor.translateAlternateColorCodes('&', "&cУ вас нет прав на эту награду!"));
                        meta.setLore(lore);
                    }
                    else if (time > 0) {
                        List<String> lore = item.getItemMeta().getLore() == null ? new ArrayList<>() : item.getItemMeta().getLore();
                        lore.add(ChatColor.translateAlternateColorCodes('&', "&r"));
                        lore.add(ChatColor.translateAlternateColorCodes('&', "&cВы уже взяли эту награду!"));
                        lore.add(ChatColor.translateAlternateColorCodes('&', "&cСледующую сможете взять через:"));
                        lore.add(ChatColor.translateAlternateColorCodes('&', "&r" + getTimeString(time)));
                        meta.setLore(lore);
                    }
                    meta.setDisplayName(reward.getTitle());
                }

                event.getInventory().getItem(i.getIndex()).setItemMeta(meta);
            }
        });

        return manager;
    }

    public static GUIInventory getInv() {
        return guiManager.getInventory(inventoryTitle);
    }

    public static void addPlayer(Player player) {

        Map<String, Timestamp> map = new HashMap<>();
        PlayerReward playerReward = null;
        String uuid = player.getUniqueId().toString();
        FileConfiguration config = plugin.getPlayerConfig().getConfig();
        String path = "players."+uuid;
        if (config.contains(path)) {

            ConfigurationSection sect = config.getConfigurationSection(path);

            for (Map.Entry<String, Object> playerSect : sect.getValues(false).entrySet()) {
                map.put(playerSect.getKey(), new Timestamp((Long) playerSect.getValue()));
            }

            playerReward = new PlayerReward(map, player);
        } else {
            playerReward = new PlayerReward(player);
        }
        players.put(player.getUniqueId().toString(), playerReward);
    }

    public static synchronized void savePlayer(Player player) {
        savePlayer(player, false);
    }
    public static synchronized void savePlayer(Player player, boolean force) {
        String uuid = player.getUniqueId().toString();
        plugin.getPlayerConfig().getConfig().set("players."+uuid, null);
        PlayerReward reward = players.get(uuid);
        if (reward != null) {
            if (reward.getLastTakeTime() != null && reward.getLastTakeTime().size() != 0) {
                for (Map.Entry<String, Timestamp> entry : reward.getLastTakeTime().entrySet()) {
                    plugin.getPlayerConfig().getConfig().set("players."+uuid+"."+entry.getKey(), entry.getValue().getTime());
                }
            } else if (plugin.getPlayerConfig().getConfig().contains("players."+uuid)) {
                plugin.getPlayerConfig().getConfig().set("players." + uuid, null);
            }
        }
        if (force) {
            plugin.getPlayerConfig().forceSave();
        } else {
            plugin.getPlayerConfig().saveConfig();
        }

    }

    public static synchronized void removePlayer(Player player) {
        savePlayer(player);
        players.remove(player.getUniqueId().toString());
    }

    public static synchronized void resetPlayer(String uuid) {
        PlayerReward player = players.get(uuid);
        if (player != null) {
            players.get(uuid).getLastTakeTime().clear();
            savePlayer(players.get(uuid).getPlayer());
        } else {
            plugin.getPlayerConfig().getConfig().set("players." + uuid, null);
            plugin.getPlayerConfig().saveConfig();
        }
    }

    private static String getTimeString(long mills) {
        if (mills <= 0) {
            mills = 0;
        }
        long time = mills/1000;
//        604800
        long sec = time % 60; // 0
        time /= 60; // 10080
        long min = time % 60;// 0
        time /= 60; // 168
        long hour = time % 24; // 48
        time /= 24; // 24
        long days = time;

        List<String> timeStringList = new ArrayList<>();
        if (days != 0) {
            timeStringList.add("&e" + days + " &c" + "д.");
        }
        if (hour != 0) {
            timeStringList.add("&e" + hour + " &c" + "ч.");
        }
        if (min != 0) {
            timeStringList.add("&e" + min + " &c" + "мин.");
        }
        if (sec != 0) {
            timeStringList.add("&e" + sec + " &c" + "с.");
        }

        return String.join(", ", timeStringList.toArray(new String[] {}));
    }
}

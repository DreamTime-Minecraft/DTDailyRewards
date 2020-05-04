package su.dreamtime.dailyrewards;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sgk.dreamtimeapi.io.ConfigWrapper;
import su.dreamtime.dailyrewards.command.DailyRewardsCommand;
import su.dreamtime.dailyrewards.events.RewardEvents;
import su.dreamtime.dailyrewards.reward.RewardManager;
import su.dreamtime.dailyrewards.util.DRTimeUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DailyRewards extends JavaPlugin
{
    private ConfigWrapper mainConfig;
    private ConfigWrapper playerConfig;

    @Override
    public void onEnable() {
        mainConfig = new ConfigWrapper(this, null, "config.yml");
        mainConfig.createNewFile(null);
        setMainConfigDefaults();
        playerConfig = new ConfigWrapper(this, null, "userdata.yml");
        playerConfig.createNewFile(null);
        playerConfig.setSaveCoolDown(10000);
        RewardManager.init(this);

        getCommand("dailyrewards").setExecutor(new DailyRewardsCommand());

        getServer().getPluginManager().registerEvents(new RewardEvents(), this);
    }

    @Override
    public void onDisable() {
        RewardManager.unload();
        playerConfig.forceSave();
    }

    public void setMainConfigDefaults()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("rewards.default.title", "&aDefault");
        map.put("rewards.default.permission", "dailyrewards.reward.default");
        map.put("rewards.default.cooldown", 1440);
        map.put("rewards.default.time-unit", DRTimeUnit.MINUTE.toString());
        map.put("rewards.default.gui-char", 'A');
        map.put("rewards.default.gui-item", "DIRT");

        List<String> desc = new ArrayList<>();
        desc.add("&eНаграда:");
        desc.add("&a - 100 коинов");
        map.put("rewards.default.description", desc);

        List<String> commands = new ArrayList<>();
        commands.add("money %target% add 100");
        map.put("rewards.default.commands", commands);


        map.put("items.any-name.gui-char", 'X');
        map.put("items.any-name.gui-item", "purple_stained_glass_pane");

        List<String> gui =new ArrayList<>();
        gui.add("XXXXXXXXX");
        gui.add("XOAOBOCOX");
        gui.add("XDOEOFOHX");
        gui.add("XIOGOJORX");
        gui.add("XXXXXXXXX");

        map.put("gui", gui);
        mainConfig.setDefaults(map);
        mainConfig.loadConfig(null);
    }

    public ConfigWrapper getMainConfig() {
        return mainConfig;
    }

    public ConfigWrapper getPlayerConfig() {
        return playerConfig;
    }
}

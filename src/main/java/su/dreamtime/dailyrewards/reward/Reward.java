package su.dreamtime.dailyrewards.reward;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import su.dreamtime.dailyrewards.util.DRTimeUnit;

import java.util.ArrayList;
import java.util.List;

public class Reward {
    private String title;
    private String permission;
    private long coolDown;
    private DRTimeUnit timeUnit;
    private List<String> commands;
    private char guiChar;
    private Material material;
    private List<String> description = new ArrayList<>();

    public Reward(String title, String permission, long coolDown, DRTimeUnit timeUnit, List<String> commands, char guiChar, Material material, List<String> description) {
        this.title = title;
        this.permission = permission;
        this.coolDown = coolDown;
        this.timeUnit = timeUnit;
        this.commands = commands;
        this.guiChar = guiChar;
        this.material = material;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getPermission() {
        return permission;
    }

    public long getCoolDown() {
        return coolDown;
    }

    public DRTimeUnit getTimeUnit() {
        return timeUnit;
    }

    public List<String> getCommands() {
        return commands;
    }

    public char getGuiChar() {
        return guiChar;
    }

    public Material getMaterial() {
        return material;
    }

    public List<String> getDescription() {
        return description;
    }

    public boolean give(Player p) {
        if (p.hasPermission(permission)) {
            for (String command : commands) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%target%", p.getName()));
            }
            return true;
        }
        return false;
    }

    public long getCoolDownMills() {
        long cd = coolDown;
        cd *= timeUnit.getFactor();
        return cd;
    }
}

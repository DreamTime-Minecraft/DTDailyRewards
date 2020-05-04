package su.dreamtime.dailyrewards.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.dreamtime.dailyrewards.reward.RewardManager;

public class DailyRewardsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("dailyrewards")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission("dailyrewards.reload")) {
                        RewardManager.reload();
                    }
                } else if (args[0].equalsIgnoreCase("reset")) {

                    if (sender.hasPermission("dailyrewards.reset")) {
                        if (args.length >= 2) {
                            OfflinePlayer off = Bukkit.getOfflinePlayer(args[1]);
                            RewardManager.resetPlayer(off.getUniqueId().toString());
                            sender.sendMessage(ChatColor.GREEN + "Время на награду у игрока " + off.getName() + " было сброшено");
                        } else {
                            sender.sendMessage(ChatColor.RED + "usage: /" + label + " reset <playerName>");
                        }
                    }
                }
            } else {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Вы должны быть игроком");
                    return true;
                }
                Player p = (Player) sender;
                RewardManager.getInv().open(p);
            }
        }

        return true;
    }
}

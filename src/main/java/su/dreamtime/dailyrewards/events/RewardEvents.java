package su.dreamtime.dailyrewards.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import su.dreamtime.dailyrewards.reward.RewardManager;

public class RewardEvents implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        RewardManager.addPlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        RewardManager.removePlayer(e.getPlayer());
    }

}

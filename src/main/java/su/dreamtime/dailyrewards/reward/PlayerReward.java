package su.dreamtime.dailyrewards.reward;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class PlayerReward {
    private Map<String, Timestamp> lastTakeTime;
    private Player player;

    public PlayerReward(Map<String, Timestamp> lastTakeTime, Player player) {
        this.lastTakeTime = lastTakeTime;
        this.player = player;
    }

    public PlayerReward(Player player) {
        this.lastTakeTime = new HashMap<>();
        this.player = player;
    }

    /**
     *
     * @param rewardId
     * @return Имеет ли игрок право на ревард
     */
    public boolean giveReward(String rewardId) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Reward reward = RewardManager.getRewards().get(rewardId);
        if (reward == null) {
            throw new IllegalArgumentException("unknown reward");
        }
        if (player.hasPermission(reward.getPermission())) {
            Timestamp last = lastTakeTime.get(rewardId);
            if (last == null || last.getTime() - now.getTime() > reward.getCoolDownMills()) {
                reward.give(player);
                lastTakeTime.put(rewardId, now);
                RewardManager.savePlayer(player);
                return true;
            }
        } else {
            player.sendMessage(ChatColor.RED + "У вас недостаточно прав!");
        }
        return false;
    }

    public Map<String, Timestamp> getLastTakeTime() {
        return lastTakeTime;
    }

    public Player getPlayer() {
        return player;
    }
}

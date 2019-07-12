package net.azisaba.digformoney;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MoneyAddTask extends BukkitRunnable {

    private final DigForMoney plugin;
    private HashMap<UUID, Double> moneyMap = new HashMap<UUID, Double>();
    @Getter
    private long lastUpdated;

    @Override
    public void run() {

        if ( moneyMap.size() <= 0 ) {
            return;
        }

        Economy econ = DigForMoney.getEconomy();

        for ( UUID uuid : moneyMap.keySet() ) {
            EconomyResponse response = econ.depositPlayer(Bukkit.getOfflinePlayer(uuid), moneyMap.get(uuid));

            if ( !response.transactionSuccess() ) {
                plugin.getLogger().warning(uuid.toString() + " へのお金追加でエラー発生: " + response.errorMessage);
                continue;
            }
        }

        moneyMap.clear();
        lastUpdated = System.currentTimeMillis();
    }

    public void addMoneyToTask(UUID uuid, double value) {
        moneyMap.put(uuid, moneyMap.getOrDefault(uuid, 0d) + value);
    }
}

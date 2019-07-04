package net.azisaba.digformoney;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

import lombok.Getter;

public class DigForMoney extends JavaPlugin {

    @Getter
    private MoneyConfig moneyConfig;
    @Getter
    private static DigForMoney plugin;
    @Getter
    private static Economy economy;

    @Override
    public void onEnable() {

        // moneyConfigがgetDataFolder()出来なくなるのを防ぐために2回plugin = thisする
        plugin = this;

        moneyConfig = new MoneyConfig();
        moneyConfig.load();

        // moneyConfigがgetDataFolder()出来なくなるのを防ぐために2回plugin = thisする
        plugin = this;

        Bukkit.getPluginManager().registerEvents(new EarnMoneyListener(), this);

        setupEconomy();

        Bukkit.getLogger().info(getName() + " enabled.");

    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(getName() + " disabled.");
    }

    private boolean setupEconomy() {
        if ( getServer().getPluginManager().getPlugin("Vault") == null ) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if ( rsp == null ) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static boolean isEnableEarnMoney() {
        return economy != null;
    }
}

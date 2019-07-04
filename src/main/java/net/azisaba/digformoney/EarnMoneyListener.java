package net.azisaba.digformoney;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class EarnMoneyListener implements Listener {

    private DigForMoney plugin;

    private final HashMap<Player, ScoreboardDisplayer> boardMap = new HashMap<>();

    private final List<Location> placeLocList = new ArrayList<>();
    private final HashMap<Player, List<Location>> breakLocMap = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {

        if ( !DigForMoney.isEnableEarnMoney() ) {
            return;
        }

        Player p = e.getPlayer();
        World world = e.getBlock().getWorld();

        if ( !DigForMoney.getPlugin().getNormalConfig().isEnabledWorld(world) ) {
            return;
        }

        if ( p.getGameMode() == GameMode.CREATIVE ) {
            return;
        }

        double value = DigForMoney.getPlugin().getMoneyConfig().getValue(e.getBlock().getType());

        if ( value <= 0 ) {
            return;
        }

        if ( isPlacedByPlayer(p, e.getBlock()) ) {
            return;
        }

        if ( breakLocMap.containsKey(p) && breakLocMap.get(p).contains(e.getBlock().getLocation()) ) {
            return;
        }

        if ( !p.hasPermission("DigForMoney.earnmoney") ) {
            return;
        }

        boolean success = addMoney(p, value);

        ScoreboardDisplayer disp;
        if ( boardMap.containsKey(p) ) {
            disp = boardMap.get(p);
        } else {
            disp = new ScoreboardDisplayer(p);
        }

        if ( success ) {
            disp.addMoney(value);
        } else {
            disp.addError();
        }
        disp.update();

        if ( !boardMap.containsKey(p) ) {
            boardMap.put(p, disp);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void addBreakBlock(BlockBreakEvent e) {

        if ( !DigForMoney.isEnableEarnMoney() ) {
            return;
        }

        if ( !DigForMoney.getPlugin().getNormalConfig().isEnabledWorld(e.getBlock().getWorld()) ) {
            return;
        }

        Player p = e.getPlayer();

        if ( breakLocMap.containsKey(p) ) {
            List<Location> locList = breakLocMap.get(p);
            locList.add(0, e.getBlock().getLocation());

            if ( locList.size() >= 50 ) {
                locList.remove(locList.size() - 1);
            }

            breakLocMap.put(p, locList);
        } else {
            breakLocMap.put(p, new ArrayList<>(Arrays.asList(e.getBlock().getLocation())));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void addPlaceBlock(BlockPlaceEvent e) {

        if ( !DigForMoney.isEnableEarnMoney() ) {
            return;
        }

        if ( !DigForMoney.getPlugin().getNormalConfig().isEnabledWorld(e.getBlock().getWorld()) ) {
            return;
        }

        placeLocList.add(0, e.getBlock().getLocation());

        if ( placeLocList.size() >= 200 ) {
            placeLocList.remove(placeLocList.size() - 1);
        }
    }

    private boolean isPlacedByPlayer(Player p, Block b) {
        // TODO 砂などの落下するブロックが対策できない
        return placeLocList.contains(b.getLocation()) || breakLocMap.getOrDefault(p, new ArrayList<>()).contains(b.getLocation());
    }

    private boolean addMoney(Player p, double value) {
        Economy econ = DigForMoney.getEconomy();
        EconomyResponse r = econ.depositPlayer(p, null, value);

        if ( !r.transactionSuccess() ) {
            plugin.getLogger().warning(p.getName() + "へのお金追加でエラー発生: " + r.errorMessage);
            return false;
        }

        return true;
    }
}

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

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EarnMoneyListener implements Listener {

    private final DigForMoney plugin;

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

        e.setDropItems(false);

        double value = DigForMoney.getPlugin().getMoneyConfig().getValue(e.getBlock().getType());

        if ( value <= 0 ) {
            return;
        }

        if ( isModifiedByPlayer(p, e.getBlock()) ) {
            return;
        }

        if ( !p.hasPermission("digformoney.earnmoney") ) {
            return;
        }

        plugin.getMoneyAddTask().addMoneyToTask(p.getUniqueId(), value);

        ScoreboardDisplayer disp;
        if ( boardMap.containsKey(p) ) {
            disp = boardMap.get(p);
        } else {
            disp = new ScoreboardDisplayer(p);
        }

        disp.addMoney(value);
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

        if ( !plugin.getNormalConfig().isEnabledWorld(e.getBlock().getWorld()) ) {
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

        if ( !plugin.getNormalConfig().isEnabledWorld(e.getBlock().getWorld()) ) {
            return;
        }

        placeLocList.add(0, e.getBlock().getLocation());

        if ( placeLocList.size() >= 200 ) {
            placeLocList.remove(placeLocList.size() - 1);
        }
    }

    private boolean isModifiedByPlayer(Player p, Block b) {
        // TODO 砂などの落下するブロックが対策できない
        return placeLocList.contains(b.getLocation()) || breakLocMap.getOrDefault(p, new ArrayList<>()).contains(b.getLocation());
    }
}

package net.azisaba.digformoney;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class ScoreboardDisplayer {

    private final Player player;

    private final Scoreboard board;
    private Objective obj = null;
    private Objective obj2 = null;

    private int current = 1;

    private final List<Double> moneyList = new ArrayList<>();
    private final List<Long> milliList = new ArrayList<>();

    private long lastUpdate = -1;

    public ScoreboardDisplayer(Player player) {
        this.player = player;

        board = Bukkit.getScoreboardManager().getNewScoreboard();
        obj = board.registerNewObjective("seichi", "dummy");
        obj2 = board.registerNewObjective("seichi2", "dummy");

        obj.setDisplayName(ChatColor.YELLOW + "掘削ボード");
        obj2.setDisplayName(ChatColor.YELLOW + "掘削ボード");

        updateTask();
    }

    public void update() {

        if ( player == null ) {
            return;
        }

        Objective updateObj = obj;
        if ( current == 0 ) {
            updateObj = obj2;
        }

        if ( current == 0 ) {
            current = 1;
        } else {
            current = 0;
        }

        if ( player.getScoreboard() != board ) {
            for ( Objective obj : player.getScoreboard().getObjectives() ) {
                if ( obj.getDisplaySlot() == DisplaySlot.SIDEBAR ) {
                    return;
                }
            }
        }

        updateObjective(updateObj);
        player.setScoreboard(board);
    }

    public void addMoney(double value) {

        lastUpdate = System.currentTimeMillis();

        moneyList.add(0, value);
        milliList.add(0, System.currentTimeMillis());

        if ( moneyList.size() >= 50 ) {
            moneyList.remove(moneyList.size() - 1);
            milliList.remove(milliList.size() - 1);
        }
    }

    public void addError() {
        lastUpdate = System.currentTimeMillis();

        moneyList.add(0, -1d);
        milliList.add(0, System.currentTimeMillis());

        if ( moneyList.size() >= 50 ) {
            moneyList.remove(moneyList.size() - 1);
            milliList.remove(milliList.size() - 1);
        }
    }

    private void updateObjective(Objective obj) {

        resetScores();

        int i = 0;
        for ( ; i < moneyList.size() && i < 10; i++ ) {

            double value = moneyList.get(i);
            String str = ChatColor.GREEN + "+" + value + "円";

            if ( value <= -1d ) {
                str = ChatColor.RED + "Error";
            }

            int count = 0;
            while ( obj.getScore(str).isScoreSet() ) {

                if ( count > 100 ) {
                    break;
                }

                str = str + " ";

                count++;
            }

            obj.getScore(str).setScore(i);
        }

        double perSecond = 0;

        for ( int i2 = 0; i2 < moneyList.size(); i2++ ) {
            if ( milliList.get(i2) + 1000 > System.currentTimeMillis() && moneyList.get(i2) > 0 ) {
                perSecond += moneyList.get(i2);
            }
        }

        obj.getScore("毎秒: " + perSecond + "円").setScore(i);

        Economy econ = DigForMoney.getEconomy();

        if ( econ == null ) {
            return;
        }
        double balance = econ.getBalance(Bukkit.getOfflinePlayer(player.getUniqueId()));

        obj.getScore(ChatColor.RED + "所持金: " + (int) balance + "円").setScore(i + 1);

        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    private void resetScores() {
        for ( String ent : board.getEntries() ) {
            board.resetScores(ent);
        }
    }

    BukkitTask task = null;

    private void updateTask() {
        task = new BukkitRunnable() {
            @Override
            public void run() {

                if ( lastUpdate + 5000 < System.currentTimeMillis() ) {

                    if ( player.getScoreboard() == board ) {
                        player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
                    }

                    milliList.clear();
                    moneyList.clear();
                    return;
                } else {
                    obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                }

                update();
            }
        }.runTaskTimer(DigForMoney.getPlugin(), 0, 5);
    }

    public void disableTask() {
        if ( task != null ) {
            task.cancel();
        }
    }
}

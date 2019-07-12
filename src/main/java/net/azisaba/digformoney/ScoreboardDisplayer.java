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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class ScoreboardDisplayer {

    private final Player player;

    private final Scoreboard board;
    private Objective obj = null;
    private Objective obj2 = null;

    private int currentBoard = 1;

    private List<MoneyData> dataList = new ArrayList<MoneyData>();

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
        if ( currentBoard == 0 ) {
            updateObj = obj2;
        }

        if ( currentBoard == 0 ) {
            currentBoard = 1;
        } else {
            currentBoard = 0;
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
        dataList.add(0, new MoneyData(System.currentTimeMillis(), value));

        int count = 0;

        for ( int i = dataList.size() - 1; i > 10 && dataList.get(i).getTime() < DigForMoney.getPlugin().getMoneyAddTask().getLastUpdated(); i-- ) {
            count++;
        }

        if ( count > 0 ) {
            dataList = dataList.subList(0, dataList.size() - count + 1);
        }
    }

    public void addError() {
        dataList.add(0, new MoneyData(System.currentTimeMillis(), -1d));

        int count = 0;

        for ( int i = dataList.size() - 1; i > 10 && dataList.get(i).getTime() < DigForMoney.getPlugin().getMoneyAddTask().getLastUpdated(); i-- ) {
            count++;
        }

        dataList = dataList.subList(0, dataList.size() - count + 1);
    }

    private void updateObjective(Objective obj) {

        resetScores();

        int i = 0;
        for ( ; i < dataList.size() && i < 10; i++ ) {

            MoneyData data = dataList.get(i);

            String str = ChatColor.GREEN + "+" + data.getValue() + "円";

            if ( data.getValue() <= -1d ) {
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

        for ( int i2 = 0; i2 < dataList.size(); i2++ ) {
            if ( dataList.get(i2).getTime() + 1000 > System.currentTimeMillis() && dataList.get(i2).getValue() > 0 ) {
                perSecond += dataList.get(i2).getValue();
            }
        }

        obj.getScore("毎秒: " + perSecond + "円").setScore(i);

        Economy econ = DigForMoney.getEconomy();

        if ( econ == null ) {
            return;
        }
        double balance = getTotalBalance();

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

                long lastUpdated = 0;
                if ( dataList.size() > 0 ) {
                    lastUpdated = dataList.get(0).getTime();
                }

                if ( lastUpdated + 5000 < System.currentTimeMillis() ) {

                    if ( player.getScoreboard() == board ) {
                        player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
                    }

                    dataList.clear();
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

    private double getTotalBalance() {
        Economy econ = DigForMoney.getEconomy();

        if ( econ == null ) {
            return -1d;
        }

        double balance = econ.getBalance(Bukkit.getOfflinePlayer(player.getUniqueId()));
        long lastUpdateTotal = DigForMoney.getPlugin().getMoneyAddTask().getLastUpdated();

        if ( dataList.size() > 0 ) {
            for ( int i = 0; i < dataList.size() && dataList.get(i).getTime() > lastUpdateTotal; i++ ) {
                balance += dataList.get(i).getValue();
            }
        }

        return balance;
    }

    @Getter
    @RequiredArgsConstructor
    private class MoneyData {
        private final long time;
        private final double value;
    }
}

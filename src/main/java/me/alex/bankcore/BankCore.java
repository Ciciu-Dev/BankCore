package me.alex.bankcore;

import org.bukkit.plugin.java.JavaPlugin;

public final class BankCore extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("BankCore has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BankCore has been disabled!");
    }
}

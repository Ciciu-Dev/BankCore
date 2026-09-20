package me.alex.bankcore;

import me.alex.bankcore.commands.BalanceCommand;
import me.alex.bankcore.services.AccountService;
import org.bukkit.plugin.java.JavaPlugin;

public final class BankCore extends JavaPlugin {

    private AccountService accountService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        double startingBalance = getConfig().getDouble("starting-balance", 1000.0);

        accountService = new AccountService(startingBalance);

        getCommand("balance").setExecutor(new BalanceCommand(accountService));

        getLogger().info("BankCore has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BankCore has been disabled!");
    }
}

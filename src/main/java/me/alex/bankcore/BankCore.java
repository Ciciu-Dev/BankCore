package me.alex.bankcore;

import me.alex.bankcore.commands.BalanceCommand;
import me.alex.bankcore.commands.PayCommand;
import me.alex.bankcore.database.DatabaseManager;
import me.alex.bankcore.services.AccountService;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Objects;

public final class BankCore extends JavaPlugin {

    private AccountService accountService;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        double startingBalance = getConfig().getDouble("starting-balance", 1000.0);

        databaseManager = new DatabaseManager(this);

        try {
            databaseManager.connect();
            getLogger().info("Connected to SQLite database.");
        } catch (SQLException exception) {
            getLogger().severe("Could not connect to the BankCore database!");
            exception.printStackTrace();

            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        accountService = new AccountService(databaseManager, startingBalance);

        Objects.requireNonNull(getCommand("balance"))
                .setExecutor(new BalanceCommand(accountService));

        Objects.requireNonNull(getCommand("pay"))
                .setExecutor(new PayCommand(accountService));

        getLogger().info("BankCore has been enabled!");
    }

    @Override
    public void onDisable() {

        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("BankCore has been disabled!");
    }
}

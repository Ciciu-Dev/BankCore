package me.alex.bankcore.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private final String customJdbcUrl;

    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.customJdbcUrl = null;
    }

    public DatabaseManager(String jdbcUrl) {
        this.plugin = null;
        this.customJdbcUrl = jdbcUrl;
    }

    public void connect() throws SQLException {

        String jdbcUrl;

        if (customJdbcUrl != null) {
            jdbcUrl = customJdbcUrl;
        } else {

            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            File databaseFile =
                    new File(plugin.getDataFolder(), "bankcore.db");

            jdbcUrl = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
        }

        connection = DriverManager.getConnection(jdbcUrl);

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS accounts (
                    uuid TEXT PRIMARY KEY,
                    balance REAL NOT NULL
                )
            """);
        }
    }

    public synchronized double getBalance(
            UUID uuid,
            double startingBalance
    ) throws SQLException {

        createAccount(uuid, startingBalance);

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             "SELECT balance FROM accounts WHERE uuid = ?"
                     )) {

            statement.setString(1, uuid.toString());

            try (ResultSet results = statement.executeQuery()) {

                if (results.next()) {
                    return results.getDouble("balance");
                }
            }
        }

        return startingBalance;
    }

    private void createAccount(
            UUID uuid,
            double startingBalance
    ) throws SQLException {

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             "INSERT OR IGNORE INTO accounts(uuid, balance) VALUES(?, ?)"
                     )) {

            statement.setString(1, uuid.toString());
            statement.setDouble(2, startingBalance);

            statement.executeUpdate();
        }
    }

    public synchronized void setBalance(
            UUID uuid,
            double amount,
            double startingBalance
    ) throws SQLException {

        createAccount(uuid, startingBalance);

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             "UPDATE accounts SET balance = ? WHERE uuid = ?"
                     )) {

            statement.setDouble(1, amount);
            statement.setString(2, uuid.toString());

            statement.executeUpdate();
        }
    }

    public synchronized void addBalance(
            UUID uuid,
            double amount,
            double startingBalance
    ) throws SQLException {

        createAccount(uuid, startingBalance);

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             "UPDATE accounts SET balance = balance + ? WHERE uuid = ?"
                     )) {

            statement.setDouble(1, amount);
            statement.setString(2, uuid.toString());

            statement.executeUpdate();
        }
    }

    public synchronized boolean transfer(
            UUID sender,
            UUID receiver,
            double amount,
            double startingBalance
    ) throws SQLException {

        connection.setAutoCommit(false);

        try {

            createAccount(sender, startingBalance);
            createAccount(receiver, startingBalance);

            double senderBalance =
                    getBalance(sender, startingBalance);

            if (senderBalance < amount) {
                connection.rollback();
                return false;
            }

            try (
                    PreparedStatement remove =
                            connection.prepareStatement(
                                    "UPDATE accounts SET balance = balance - ? WHERE uuid = ?"
                            );

                    PreparedStatement add =
                            connection.prepareStatement(
                                    "UPDATE accounts SET balance = balance + ? WHERE uuid = ?"
                            )
            ) {

                remove.setDouble(1, amount);
                remove.setString(2, sender.toString());
                remove.executeUpdate();

                add.setDouble(1, amount);
                add.setString(2, receiver.toString());
                add.executeUpdate();
            }

            connection.commit();
            return true;

        } catch (SQLException exception) {

            connection.rollback();
            throw exception;

        } finally {

            connection.setAutoCommit(true);
        }
    }

    public void close() {

        if (connection == null) {
            return;
        }

        try {
            connection.close();

        } catch (SQLException exception) {

            if (plugin != null) {
                plugin.getLogger().severe(
                        "Failed to close database: "
                                + exception.getMessage()
                );
            }
        }
    }
}

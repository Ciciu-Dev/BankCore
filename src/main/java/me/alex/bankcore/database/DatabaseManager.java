package me.alex.bankcore.database;

import me.alex.bankcore.models.TransactionRecord;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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

            File file = new File(plugin.getDataFolder(), "bankcore.db");
            jdbcUrl = "jdbc:sqlite:" + file.getAbsolutePath();
        }

        connection = DriverManager.getConnection(jdbcUrl);

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                CREATE TABLE IF NOT EXISTS accounts (
                    uuid TEXT PRIMARY KEY,
                    balance REAL NOT NULL
                )
            """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_uuid TEXT NOT NULL,
                    type TEXT NOT NULL,
                    amount REAL NOT NULL,
                    counterparty_uuid TEXT,
                    timestamp INTEGER NOT NULL
                )
            """);
        }
    }

    private void createAccount(UUID uuid, double startingBalance)
            throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR IGNORE INTO accounts(uuid, balance) VALUES(?, ?)"
        )) {
            statement.setString(1, uuid.toString());
            statement.setDouble(2, startingBalance);
            statement.executeUpdate();
        }
    }

    public synchronized double getBalance(UUID uuid, double startingBalance)
            throws SQLException {

        createAccount(uuid, startingBalance);

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT balance FROM accounts WHERE uuid = ?"
        )) {

            statement.setString(1, uuid.toString());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getDouble("balance");
                }
            }
        }

        return startingBalance;
    }

    private void logTransaction(
            UUID player,
            String type,
            double amount,
            UUID counterparty
    ) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(
                """
                INSERT INTO transactions
                (player_uuid, type, amount, counterparty_uuid, timestamp)
                VALUES (?, ?, ?, ?, ?)
                """
        )) {

            statement.setString(1, player.toString());
            statement.setString(2, type);
            statement.setDouble(3, amount);

            if (counterparty == null) {
                statement.setNull(4, Types.VARCHAR);
            } else {
                statement.setString(4, counterparty.toString());
            }

            statement.setLong(5, System.currentTimeMillis());
            statement.executeUpdate();
        }
    }

    public synchronized void setBalance(
            UUID uuid,
            double amount,
            double startingBalance
    ) throws SQLException {

        createAccount(uuid, startingBalance);

        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE accounts SET balance = ? WHERE uuid = ?"
        )) {

            statement.setDouble(1, amount);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        }

        logTransaction(uuid, "ADMIN_SET", amount, null);
    }

    public synchronized void addBalance(
            UUID uuid,
            double amount,
            double startingBalance
    ) throws SQLException {

        createAccount(uuid, startingBalance);

        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE accounts SET balance = balance + ? WHERE uuid = ?"
        )) {

            statement.setDouble(1, amount);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        }

        logTransaction(uuid, "ADMIN_ADD", amount, null);
    }

    public synchronized boolean removeBalance(
            UUID uuid,
            double amount,
            double startingBalance
    ) throws SQLException {

        double current = getBalance(uuid, startingBalance);

        if (current < amount) {
            return false;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE accounts SET balance = balance - ? WHERE uuid = ?"
        )) {

            statement.setDouble(1, amount);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        }

        logTransaction(uuid, "ADMIN_REMOVE", amount, null);

        return true;
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

            if (getBalance(sender, startingBalance) < amount) {
                connection.rollback();
                return false;
            }

            try (
                    PreparedStatement remove = connection.prepareStatement(
                            "UPDATE accounts SET balance = balance - ? WHERE uuid = ?"
                    );
                    PreparedStatement add = connection.prepareStatement(
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

            logTransaction(sender, "PAY_SENT", amount, receiver);
            logTransaction(receiver, "PAY_RECEIVED", amount, sender);

            connection.commit();
            return true;

        } catch (SQLException exception) {

            connection.rollback();
            throw exception;

        } finally {
            connection.setAutoCommit(true);
        }
    }

    public synchronized List<TransactionRecord> getHistory(UUID uuid, int limit)
            throws SQLException {

        List<TransactionRecord> history = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(
                """
                SELECT type, amount, counterparty_uuid, timestamp
                FROM transactions
                WHERE player_uuid = ?
                ORDER BY id DESC
                LIMIT ?
                """
        )) {

            statement.setString(1, uuid.toString());
            statement.setInt(2, limit);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    String rawCounterparty =
                            result.getString("counterparty_uuid");

                    UUID counterparty = rawCounterparty == null
                            ? null
                            : UUID.fromString(rawCounterparty);

                    history.add(new TransactionRecord(
                            result.getString("type"),
                            result.getDouble("amount"),
                            counterparty,
                            result.getLong("timestamp")
                    ));
                }
            }
        }

        return history;
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
                        "Failed to close database: " + exception.getMessage()
                );
            }
        }
    }
}

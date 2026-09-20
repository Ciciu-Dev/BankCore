package me.alex.bankcore.services;

import me.alex.bankcore.database.DatabaseManager;
import me.alex.bankcore.models.TransactionRecord;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class AccountService {

    private final DatabaseManager database;
    private final double startingBalance;

    public AccountService(DatabaseManager database, double startingBalance) {
        this.database = database;
        this.startingBalance = startingBalance;
    }

    public double getBalance(UUID uuid) throws SQLException {
        return database.getBalance(uuid, startingBalance);
    }

    public boolean transfer(UUID sender, UUID receiver, double amount)
            throws SQLException {

        return database.transfer(
                sender,
                receiver,
                amount,
                startingBalance
        );
    }

    public void setBalance(UUID uuid, double amount)
            throws SQLException {

        database.setBalance(uuid, amount, startingBalance);
    }

    public void addBalance(UUID uuid, double amount)
            throws SQLException {

        database.addBalance(uuid, amount, startingBalance);
    }

    public boolean removeBalance(UUID uuid, double amount)
            throws SQLException {

        return database.removeBalance(
                uuid,
                amount,
                startingBalance
        );
    }

    public List<TransactionRecord> getHistory(UUID uuid)
            throws SQLException {

        return database.getHistory(uuid, 10);
    }
}

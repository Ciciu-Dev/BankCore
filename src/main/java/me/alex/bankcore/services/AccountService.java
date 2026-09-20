package me.alex.bankcore.services;

import me.alex.bankcore.database.DatabaseManager;

import java.sql.SQLException;
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
}

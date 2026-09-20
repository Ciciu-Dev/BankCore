package me.alex.bankcore.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AccountService {

    private final Map<UUID, Double> balances = new HashMap<>();
    private final double startingBalance;

    public AccountService(double startingBalance) {
        this.startingBalance = startingBalance;
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, startingBalance);
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, amount);
    }
}

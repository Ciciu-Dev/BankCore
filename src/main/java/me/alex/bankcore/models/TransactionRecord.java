package me.alex.bankcore.models;

import java.util.UUID;

public record TransactionRecord(
        String type,
        double amount,
        UUID counterpartyUuid,
        long timestamp
) {
}

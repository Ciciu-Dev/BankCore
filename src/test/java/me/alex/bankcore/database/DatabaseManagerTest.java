package me.alex.bankcore.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    private static final double STARTING_BALANCE = 1000.0;

    @Test
    void createsNewAccountWithStartingBalance() throws Exception {

        DatabaseManager database =
                new DatabaseManager("jdbc:sqlite::memory:");

        database.connect();

        UUID player = UUID.randomUUID();

        assertEquals(
                1000.0,
                database.getBalance(player, STARTING_BALANCE),
                0.001
        );

        database.close();
    }

    @Test
    void transfersMoneyBetweenPlayers() throws Exception {

        DatabaseManager database =
                new DatabaseManager("jdbc:sqlite::memory:");

        database.connect();

        UUID sender = UUID.randomUUID();
        UUID receiver = UUID.randomUUID();

        boolean result = database.transfer(
                sender,
                receiver,
                250.0,
                STARTING_BALANCE
        );

        assertTrue(result);

        assertEquals(
                750.0,
                database.getBalance(sender, STARTING_BALANCE),
                0.001
        );

        assertEquals(
                1250.0,
                database.getBalance(receiver, STARTING_BALANCE),
                0.001
        );

        database.close();
    }

    @Test
    void rejectsTransferWithInsufficientFunds() throws Exception {

        DatabaseManager database =
                new DatabaseManager("jdbc:sqlite::memory:");

        database.connect();

        UUID sender = UUID.randomUUID();
        UUID receiver = UUID.randomUUID();

        boolean result = database.transfer(
                sender,
                receiver,
                5000.0,
                STARTING_BALANCE
        );

        assertFalse(result);

        assertEquals(
                1000.0,
                database.getBalance(sender, STARTING_BALANCE),
                0.001
        );

        assertEquals(
                1000.0,
                database.getBalance(receiver, STARTING_BALANCE),
                0.001
        );

        database.close();
    }

    @Test
    void balancePersistsAfterDatabaseRestart(
            @TempDir Path tempDirectory
    ) throws Exception {

        Path databaseFile =
                tempDirectory.resolve("bankcore-test.db");

        String jdbcUrl =
                "jdbc:sqlite:" + databaseFile;

        UUID sender = UUID.randomUUID();
        UUID receiver = UUID.randomUUID();

        DatabaseManager firstSession =
                new DatabaseManager(jdbcUrl);

        firstSession.connect();

        assertTrue(
                firstSession.transfer(
                        sender,
                        receiver,
                        300.0,
                        STARTING_BALANCE
                )
        );

        firstSession.close();

        DatabaseManager secondSession =
                new DatabaseManager(jdbcUrl);

        secondSession.connect();

        assertEquals(
                700.0,
                secondSession.getBalance(
                        sender,
                        STARTING_BALANCE
                ),
                0.001
        );

        assertEquals(
                1300.0,
                secondSession.getBalance(
                        receiver,
                        STARTING_BALANCE
                ),
                0.001
        );

        secondSession.close();
    }
}

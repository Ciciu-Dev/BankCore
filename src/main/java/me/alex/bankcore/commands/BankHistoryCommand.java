package me.alex.bankcore.commands;

import me.alex.bankcore.models.TransactionRecord;
import me.alex.bankcore.services.AccountService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BankHistoryCommand implements CommandExecutor {

    private final AccountService accountService;

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd/MM HH:mm");

    public BankHistoryCommand(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        try {

            List<TransactionRecord> history =
                    accountService.getHistory(player.getUniqueId());

            player.sendMessage("§6§lBankCore History");

            if (history.isEmpty()) {
                player.sendMessage("§7No transactions yet.");
                return true;
            }

            for (TransactionRecord record : history) {

                String time = FORMAT.format(
                        Instant.ofEpochMilli(record.timestamp())
                                .atZone(ZoneId.systemDefault())
                );

                String entry = switch (record.type()) {
                    case "ADMIN_SET" ->
                            "§eBalance set to §f$"
                                    + String.format("%.2f", record.amount());

                    case "ADMIN_ADD" ->
                            "§a+$" + String.format("%.2f", record.amount())
                                    + " §7Admin adjustment";

                    case "ADMIN_REMOVE" ->
                            "§c-$" + String.format("%.2f", record.amount())
                                    + " §7Admin adjustment";

                    case "PAY_SENT" ->
                            "§c-$" + String.format("%.2f", record.amount())
                                    + " §7Payment sent";

                    case "PAY_RECEIVED" ->
                            "§a+$" + String.format("%.2f", record.amount())
                                    + " §7Payment received";

                    default -> "§7Unknown transaction";
                };

                player.sendMessage(
                        "§8[" + time + "] §r" + entry
                );
            }

        } catch (SQLException exception) {

            player.sendMessage("§cCould not load transaction history.");
            exception.printStackTrace();
        }

        return true;
    }
}

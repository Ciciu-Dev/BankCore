package me.alex.bankcore.commands;

import me.alex.bankcore.services.AccountService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class BankAdminCommand implements CommandExecutor {

    private final AccountService accountService;

    public BankAdminCommand(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        if (args.length != 3) {
            sender.sendMessage(
                    "§cUsage: /bankadmin <set|add|remove> <player> <amount>"
            );
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            sender.sendMessage("§cThat player is not online.");
            return true;
        }

        double amount;

        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage("§cPlease enter a valid amount.");
            return true;
        }

        if (!Double.isFinite(amount) || amount < 0) {
            sender.sendMessage("§cAmount must be zero or greater.");
            return true;
        }

        amount = Math.round(amount * 100.0) / 100.0;

        try {

            switch (args[0].toLowerCase()) {

                case "set" -> {
                    accountService.setBalance(
                            target.getUniqueId(),
                            amount
                    );

                    sender.sendMessage(
                            "§aSet §f" + target.getName()
                                    + "§a's balance to §f$"
                                    + String.format("%.2f", amount)
                    );
                }

                case "add" -> {
                    accountService.addBalance(
                            target.getUniqueId(),
                            amount
                    );

                    sender.sendMessage(
                            "§aAdded §f$"
                                    + String.format("%.2f", amount)
                                    + " §ato §f" + target.getName()
                    );
                }

                case "remove" -> {

                    boolean success =
                            accountService.removeBalance(
                                    target.getUniqueId(),
                                    amount
                            );

                    if (!success) {
                        sender.sendMessage(
                                "§cThat player does not have enough money."
                        );
                        return true;
                    }

                    sender.sendMessage(
                            "§aRemoved §f$"
                                    + String.format("%.2f", amount)
                                    + " §afrom §f" + target.getName()
                    );
                }

                default -> sender.sendMessage(
                        "§cUsage: /bankadmin <set|add|remove> <player> <amount>"
                );
            }

        } catch (SQLException exception) {

            sender.sendMessage(
                    "§cSomething went wrong while updating the account."
            );

            exception.printStackTrace();
        }

        return true;
    }
}

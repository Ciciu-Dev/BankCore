package me.alex.bankcore.commands;

import me.alex.bankcore.services.AccountService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class PayCommand implements CommandExecutor {

    private final AccountService accountService;

    public PayCommand(AccountService accountService) {
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
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage("§cUsage: /pay <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            player.sendMessage("§cThat player is not online.");
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cYou cannot pay yourself.");
            return true;
        }

        double amount;

        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException exception) {
            player.sendMessage("§cPlease enter a valid amount.");
            return true;
        }

        if (!Double.isFinite(amount) || amount <= 0) {
            player.sendMessage("§cAmount must be greater than $0.");
            return true;
        }

        amount = Math.round(amount * 100.0) / 100.0;

        try {

            boolean success = accountService.transfer(
                    player.getUniqueId(),
                    target.getUniqueId(),
                    amount
            );

            if (!success) {
                player.sendMessage("§cYou do not have enough money.");
                return true;
            }

            player.sendMessage(
                    "§aYou paid §f" + target.getName()
                            + " §a$" + String.format("%.2f", amount)
            );

            target.sendMessage(
                    "§aYou received §f$"
                            + String.format("%.2f", amount)
                            + " §afrom §f" + player.getName()
            );

        } catch (SQLException exception) {

            player.sendMessage(
                    "§cSomething went wrong while processing the transaction."
            );

            exception.printStackTrace();
        }

        return true;
    }
}

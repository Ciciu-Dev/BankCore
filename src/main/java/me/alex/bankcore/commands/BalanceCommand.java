package me.alex.bankcore.commands;

import me.alex.bankcore.services.AccountService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class BalanceCommand implements CommandExecutor {

    private final AccountService accountService;

    public BalanceCommand(AccountService accountService) {
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

        try {
            double balance = accountService.getBalance(player.getUniqueId());

            player.sendMessage(
                    "§aBank Balance: §f$" + String.format("%.2f", balance)
            );

        } catch (SQLException exception) {
            player.sendMessage(
                    "§cSomething went wrong while accessing your bank account."
            );

            exception.printStackTrace();
        }

        return true;
    }
}

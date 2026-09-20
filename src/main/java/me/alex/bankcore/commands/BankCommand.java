package me.alex.bankcore.commands;

import me.alex.bankcore.gui.BankMenu;
import me.alex.bankcore.services.AccountService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BankCommand implements CommandExecutor {

    private final AccountService accountService;

    public BankCommand(AccountService accountService) {
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

        new BankMenu(accountService).open(player);
        return true;
    }
}

package me.alex.bankcore.gui;

import me.alex.bankcore.services.AccountService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.List;

public class BankMenu implements InventoryHolder {

    private final Inventory inventory;
    private final AccountService accountService;

    public BankMenu(AccountService accountService) {
        this.accountService = accountService;

        this.inventory = Bukkit.createInventory(
                this,
                27,
                Component.text("BankCore")
        );
    }

    public void open(Player player) {

        inventory.clear();

        addBalanceItem(player);
        addPaymentItem();
        addCloseItem();

        player.openInventory(inventory);
    }

    private void addBalanceItem(Player player) {

        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();

        try {
            double balance =
                    accountService.getBalance(player.getUniqueId());

            meta.displayName(
                    Component.text("Your Balance")
            );

            meta.lore(List.of(
                    Component.text(
                            "$" + String.format("%.2f", balance)
                    ),
                    Component.empty(),
                    Component.text("Stored securely in BankCore")
            ));

        } catch (SQLException exception) {

            meta.displayName(
                    Component.text("Unable to load balance")
            );
        }

        item.setItemMeta(meta);

        inventory.setItem(13, item);
    }

    private void addPaymentItem() {

        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("Send Money")
        );

        meta.lore(List.of(
                Component.text("Use /pay <player> <amount>"),
                Component.text("to transfer money.")
        ));

        item.setItemMeta(meta);

        inventory.setItem(11, item);
    }

    private void addCloseItem() {

        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("Close")
        );

        item.setItemMeta(meta);

        inventory.setItem(15, item);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

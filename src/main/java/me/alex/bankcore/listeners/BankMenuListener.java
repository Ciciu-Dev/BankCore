package me.alex.bankcore.listeners;

import me.alex.bankcore.gui.BankMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BankMenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getInventory().getHolder() instanceof BankMenu)) {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() == 15) {
            event.getWhoClicked().closeInventory();
        }
    }
}

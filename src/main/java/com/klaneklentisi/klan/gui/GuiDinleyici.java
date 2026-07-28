package com.klaneklentisi.klan.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class GuiDinleyici implements Listener {

    @EventHandler
    public void onTikla(InventoryClickEvent olay) {
        if (!(olay.getInventory().getHolder() instanceof MenuTutucu tutucu)) return;
        olay.setCancelled(true);
        // Oyuncunun kendi envanterine tıklaması (alt envanter) engellenmeli, sadece üst menü işlensin.
        if (olay.getClickedInventory() == null) return;
        if (!olay.getClickedInventory().equals(olay.getView().getTopInventory())) return;
        tutucu.getMenu().tikla(olay);
    }

    @EventHandler
    public void onSurukle(InventoryDragEvent olay) {
        if (olay.getInventory().getHolder() instanceof MenuTutucu) {
            olay.setCancelled(true);
        }
    }
}

package com.klaneklentisi.klan.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Açılan her klan GUI'sinin arkasındaki Menu nesnesini taşır.
 * GuiDinleyici bu sayede tıklamayı doğru menüye yönlendirir.
 */
public class MenuTutucu implements InventoryHolder {

    private final Menu menu;
    private Inventory envanter;

    public MenuTutucu(Menu menu) {
        this.menu = menu;
    }

    public void envanterAta(Inventory envanter) {
        this.envanter = envanter;
    }

    public Menu getMenu() {
        return menu;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return envanter;
    }
}

package com.klaneklentisi.klan.gui;

import com.klaneklentisi.klan.KlanEklentisi;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/**
 * Tüm klan GUI'lerindeki tıklama/sürükleme olaylarını yakalar.
 * Güvenlik notu: getInventory() yerine daima getView().getTopInventory() üzerinden
 * kontrol yapılır - aksi halde oyuncunun kendi envanterinden shift-tık/numara tuşu
 * ile GUI'ye eşya sokması engellenemez.
 */
public class GuiDinleyici implements Listener {

    private final KlanEklentisi eklenti;

    public GuiDinleyici(KlanEklentisi eklenti) {
        this.eklenti = eklenti;
    }

    @EventHandler
    public void onTikla(InventoryClickEvent olay) {
        if (!(olay.getView().getTopInventory().getHolder() instanceof MenuTutucu tutucu)) return;

        boolean ustEnvanterdeTiklandi = olay.getClickedInventory() != null
                && olay.getClickedInventory().equals(olay.getView().getTopInventory());

        boolean izinliSlot = ustEnvanterdeTiklandi
                && !olay.isShiftClick()
                && tutucu.getMenu().izinliSlotMu(olay.getSlot());

        if (!izinliSlot) {
            olay.setCancelled(true);
        }

        if (ustEnvanterdeTiklandi) {
            try {
                tutucu.getMenu().tikla(olay);
            } catch (Exception hata) {
                eklenti.getLoglayici().hataKaydet("GUI tıklama (" + tutucu.getMenu().getClass().getSimpleName() + ")", hata);
                var oyuncu = olay.getWhoClicked();
                if (oyuncu instanceof org.bukkit.entity.Player p) {
                    p.closeInventory();
                }
            }
        }
    }

    @EventHandler
    public void onSurukle(InventoryDragEvent olay) {
        if (olay.getView().getTopInventory().getHolder() instanceof MenuTutucu) {
            olay.setCancelled(true);
        }
    }

    @EventHandler
    public void onKapat(InventoryCloseEvent olay) {
        if (olay.getInventory().getHolder() instanceof MenuTutucu tutucu) {
            tutucu.getMenu().kapandi(olay);
        }
    }
}

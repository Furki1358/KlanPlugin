package com.klaneklentisi.klan.gui;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.model.Klan;
import com.klaneklentisi.klan.model.Rutbe;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SembolAyarlaMenu extends Menu {

    private static final int SEMBOL_SLOTU = 4;

    private final Klan klan;

    public SembolAyarlaMenu(KlanEklentisi eklenti, Player oyuncu, Klan klan) {
        super(eklenti, oyuncu);
        this.klan = klan;
    }

    @Override
    protected int boyut() {
        return 9;
    }

    @Override
    protected String baslik() {
        return mesajlar.baslik("menu.sembol.baslik");
    }

    @Override
    protected void doldur() {
        for (int i = 0; i < boyut(); i++) {
            if (i == SEMBOL_SLOTU) continue;
            envanter.setItem(i, Esya.doldurucu());
        }
        ItemStack mevcut = klan.getSembol();
        envanter.setItem(SEMBOL_SLOTU, mevcut != null ? mevcut.clone() : null);
    }

    @Override
    public boolean izinliSlotMu(int slot) {
        return slot == SEMBOL_SLOTU;
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        // Sadece slot 4'e izin veriliyor (GuiDinleyici tarafından); burada ekstra bir işlem gerekmiyor,
        // eşya yerleşimi/alımı normal şekilde gerçekleşir. Kaydetme işlemi envanter kapanınca yapılır.
    }

    @Override
    public void kapandi(InventoryCloseEvent olay) {
        Rutbe rutbe = klan.getRutbe(oyuncu.getUniqueId());
        if (rutbe == null || rutbe.getSeviye() < Rutbe.YONETICI.getSeviye()) {
            return; // yetkisi yoksa hiçbir değişiklik kaydedilmez
        }
        ItemStack slotEsyasi = olay.getInventory().getItem(SEMBOL_SLOTU);
        if (slotEsyasi != null && slotEsyasi.getType() != Material.AIR) {
            ItemStack kopya = slotEsyasi.clone();
            kopya.setAmount(1);
            klan.setSembol(kopya);
            yonetici.kaydet(klan);
            oyuncu.sendMessage(mesajlar.al("menu.sembol.ayarlandi"));
        }
    }
}

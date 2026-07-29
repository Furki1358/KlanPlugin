package com.klaneklentisi.klan.gui.admin;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.gui.Esya;
import com.klaneklentisi.klan.gui.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class AdminAnaMenu extends Menu {

    public AdminAnaMenu(KlanEklentisi eklenti, Player oyuncu) {
        super(eklenti, oyuncu);
    }

    @Override
    protected int boyut() {
        return 27;
    }

    @Override
    protected String baslik() {
        return mesajlar.baslik("admin.ana.baslik");
    }

    @Override
    protected void doldur() {
        for (int i = 0; i < boyut(); i++) {
            envanter.setItem(i, Esya.doldurucu());
        }
        envanter.setItem(11, Esya.olustur(Material.MAP, mesajlar.baslik("admin.ana.tum-klanlar")));
        envanter.setItem(13, Esya.olustur(Material.COMPARATOR, mesajlar.baslik("admin.ana.ayarlar")));
        envanter.setItem(15, Esya.olustur(Material.EMERALD, mesajlar.baslik("admin.ana.yenile")));
        envanter.setItem(22, Esya.olustur(Material.BARRIER, mesajlar.baslik("admin.ana.kapat")));
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        switch (olay.getSlot()) {
            case 11 -> new AdminKlanListesiMenu(eklenti, oyuncu, 0).ac();
            case 13 -> new AdminAyarlarMenu(eklenti, oyuncu).ac();
            case 15 -> {
                eklenti.reloadConfig();
                mesajlar.yukle();
                oyuncu.sendMessage(mesajlar.al("yonetim.yenilendi"));
                yenile();
            }
            case 22 -> oyuncu.closeInventory();
            default -> {}
        }
    }
}

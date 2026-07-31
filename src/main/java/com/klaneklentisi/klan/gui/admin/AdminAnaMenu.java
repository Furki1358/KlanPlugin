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
        return 54;
    }

    @Override
    protected String baslik() {
        return mesajlar.baslik("admin.ana.baslik");
    }

    private void dolduruCam(int... slotlar) {
        for (int s : slotlar) {
            envanter.setItem(s, Esya.olustur(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
    }

    @Override
    protected void doldur() {
        kenarCiz();
        dolduruCam(10, 16, 19, 20, 21, 23, 24, 25, 28, 29, 30, 32, 33, 34);
        envanter.setItem(12, Esya.olustur(Material.MAP, mesajlar.baslik("admin.ana.tum-klanlar")));
        envanter.setItem(14, Esya.olustur(Material.COMPARATOR, mesajlar.baslik("admin.ana.ayarlar")));
        envanter.setItem(22, Esya.olustur(Material.GOLD_INGOT, mesajlar.baslik("admin.ana.yenile")));
        envanter.setItem(31, Esya.olustur(Material.BARRIER, mesajlar.baslik("admin.ana.kapat")));
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        switch (olay.getSlot()) {
            case 12 -> new AdminKlanListesiMenu(eklenti, oyuncu, 0).ac();
            case 14 -> new AdminAyarlarMenu(eklenti, oyuncu).ac();
            case 22 -> {
                eklenti.reloadConfig();
                mesajlar.yukle();
                eklenti.getKomutAyarlari().yukle();
                oyuncu.sendMessage(mesajlar.al("yonetim.yenilendi"));
                yenile();
            }
            case 31 -> oyuncu.closeInventory();
            default -> {}
        }
    }
}

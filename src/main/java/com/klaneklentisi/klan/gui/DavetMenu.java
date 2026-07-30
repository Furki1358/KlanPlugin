package com.klaneklentisi.klan.gui;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.manager.KlanYoneticisi;
import com.klaneklentisi.klan.model.Klan;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;

public class DavetMenu extends Menu {

    private final Klan davetEdenKlan;

    public DavetMenu(KlanEklentisi eklenti, Player oyuncu, Klan davetEdenKlan) {
        super(eklenti, oyuncu);
        this.davetEdenKlan = davetEdenKlan;
    }

    @Override
    protected int boyut() {
        return 27;
    }

    @Override
    protected String baslik() {
        return mesajlar.baslik("menu.davet.baslik", Map.of("klan", davetEdenKlan.getIsim()));
    }

    @Override
    protected void doldur() {
        for (int i = 0; i < boyut(); i++) {
            envanter.setItem(i, Esya.doldurucu());
        }
        envanter.setItem(11, Esya.olustur(Material.LIME_WOOL, mesajlar.baslik("menu.davet.kabul")));
        envanter.setItem(15, Esya.olustur(Material.RED_WOOL, mesajlar.baslik("menu.davet.reddet")));
        envanter.setItem(22, Esya.olustur(Material.ARROW, mesajlar.baslik("menu.davet.geri")));
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        switch (olay.getSlot()) {
            case 11 -> {
                if (!izinVarMi("KABUL")) return;
                KlanYoneticisi.Sonuc sonuc = yonetici.davetKabulEt(oyuncu);
                if (sonuc == KlanYoneticisi.Sonuc.BASARILI) {
                    oyuncu.sendMessage(mesajlar.al("kabul.basarili", Map.of("klan", davetEdenKlan.getIsim())));
                    for (var uid : davetEdenKlan.getUyeler().keySet()) {
                        if (uid.equals(oyuncu.getUniqueId())) continue;
                        var alici = eklenti.getServer().getPlayer(uid);
                        if (alici != null) {
                            alici.sendMessage(mesajlar.al("kabul.duyuru", Map.of("oyuncu", oyuncu.getName())));
                        }
                    }
                    oyuncu.closeInventory();
                } else {
                    oyuncu.sendMessage(mesajlar.al("kabul.davet-yok"));
                    oyuncu.closeInventory();
                }
            }
            case 15 -> {
                if (!izinVarMi("REDDET")) return;
                yonetici.davetReddet(oyuncu);
                oyuncu.sendMessage(mesajlar.al("reddet.basarili"));
                oyuncu.closeInventory();
            }
            case 22 -> new AnaMenu(eklenti, oyuncu).ac();
            default -> {}
        }
    }
}

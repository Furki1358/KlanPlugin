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
        return 54;
    }

    @Override
    protected String baslik() {
        return mesajlar.baslik("menu.davet.baslik", Map.of("klan", davetEdenKlan.getIsim()));
    }

    private void dolduruCam(int... slotlar) {
        for (int s : slotlar) {
            envanter.setItem(s, Esya.olustur(Material.WHITE_STAINED_GLASS_PANE, " "));
        }
    }

    @Override
    protected void doldur() {
        kenarCiz();
        dolduruCam(10, 11, 15, 16, 19, 20, 24, 25, 28, 29, 32, 33, 34);
        envanter.setItem(13, Esya.olustur(Material.WRITTEN_BOOK, "&e" + davetEdenKlan.getIsim(),
                java.util.List.of("&7[" + davetEdenKlan.getEtiket() + "]")));
        envanter.setItem(21, Esya.olustur(Material.LIME_WOOL, mesajlar.baslik("menu.davet.kabul")));
        envanter.setItem(23, Esya.olustur(Material.RED_WOOL, mesajlar.baslik("menu.davet.reddet")));
        envanter.setItem(31, Esya.olustur(Material.ARROW, mesajlar.baslik("menu.davet.geri")));
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        switch (olay.getSlot()) {
            case 21 -> {
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
            case 23 -> {
                if (!izinVarMi("REDDET")) return;
                yonetici.davetReddet(oyuncu);
                oyuncu.sendMessage(mesajlar.al("reddet.basarili"));
                oyuncu.closeInventory();
            }
            case 31 -> new AnaMenu(eklenti, oyuncu).ac();
            default -> {}
        }
    }
}

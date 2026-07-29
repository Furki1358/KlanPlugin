package com.klaneklentisi.klan.gui;

import com.klaneklentisi.klan.KlanEklentisi;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Map;

public class LiderlikMenu extends Menu {

    private static final int SAYFA_BASI = 45;

    private final int sayfa;
    private final List<Map.Entry<java.util.UUID, com.klaneklentisi.klan.istatistik.OyuncuIstatistik>> liste;

    public LiderlikMenu(KlanEklentisi eklenti, Player oyuncu, int sayfa) {
        super(eklenti, oyuncu);
        this.sayfa = sayfa;
        this.liste = eklenti.getIstatistikYoneticisi().siraliListe();
    }

    @Override
    protected int boyut() {
        return 54;
    }

    @Override
    protected String baslik() {
        return mesajlar.baslik("menu.ana.liderlik");
    }

    @Override
    protected void doldur() {
        for (int i = 0; i < boyut(); i++) {
            envanter.setItem(i, Esya.doldurucu());
        }

        int baslangic = sayfa * SAYFA_BASI;
        int bitis = Math.min(baslangic + SAYFA_BASI, liste.size());

        for (int i = baslangic; i < bitis; i++) {
            var girdi = liste.get(i);
            OfflinePlayer oyuncu = Bukkit.getOfflinePlayer(girdi.getKey());
            String isim = oyuncu.getName() == null ? "?" : oyuncu.getName();
            envanter.setItem(i - baslangic, Esya.oyuncuKafasi(oyuncu, "&6#" + (i + 1) + " &f" + isim, List.of(
                    "&a▪ Öldürme: &f" + girdi.getValue().getOldurme(),
                    "&c▪ Ölme: &f" + girdi.getValue().getOlme(),
                    "&7▪ K/D: &f" + girdi.getValue().getOran()
            )));
        }

        if (sayfa > 0) {
            envanter.setItem(48, Esya.olustur(Material.ARROW, mesajlar.baslik("liderlik.onceki-buton")));
        }
        if (bitis < liste.size()) {
            envanter.setItem(50, Esya.olustur(Material.ARROW, mesajlar.baslik("liderlik.sonraki-buton")));
        }
        envanter.setItem(49, Esya.olustur(Material.BARRIER, mesajlar.baslik("menu.uyeler.geri")));
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        int slot = olay.getSlot();
        int baslangic = sayfa * SAYFA_BASI;

        if (slot == 49) {
            new AnaMenu(eklenti, oyuncu).ac();
        } else if (slot == 48 && sayfa > 0) {
            new LiderlikMenu(eklenti, oyuncu, sayfa - 1).ac();
        } else if (slot == 50 && baslangic + SAYFA_BASI < liste.size()) {
            new LiderlikMenu(eklenti, oyuncu, sayfa + 1).ac();
        }
    }
}

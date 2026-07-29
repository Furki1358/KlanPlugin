package com.klaneklentisi.klan.gui.admin;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.gui.Esya;
import com.klaneklentisi.klan.gui.Menu;
import com.klaneklentisi.klan.model.Klan;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminKlanListesiMenu extends Menu {

    private static final int SAYFA_BASI = 45;

    private final int sayfa;
    private final List<Klan> klanlar;

    public AdminKlanListesiMenu(KlanEklentisi eklenti, Player oyuncu, int sayfa) {
        super(eklenti, oyuncu);
        this.sayfa = sayfa;
        this.klanlar = new ArrayList<>(yonetici.tumKlanlar());
    }

    @Override
    protected int boyut() {
        return 54;
    }

    @Override
    protected String baslik() {
        return mesajlar.baslik("admin.klan-listesi.baslik");
    }

    @Override
    protected void doldur() {
        for (int i = 0; i < boyut(); i++) {
            envanter.setItem(i, Esya.doldurucu());
        }

        int baslangic = sayfa * SAYFA_BASI;
        int bitis = Math.min(baslangic + SAYFA_BASI, klanlar.size());

        for (int i = baslangic; i < bitis; i++) {
            Klan k = klanlar.get(i);
            envanter.setItem(i - baslangic, Esya.olustur(Material.PAPER,
                    "&f" + k.getIsim() + " &8[&7" + k.getEtiket() + "&8]",
                    mesajlar.alListe("admin.klan-listesi.satir-aciklama", Map.of(
                            "etiket", k.getEtiket(),
                            "uyeSayisi", String.valueOf(k.getUyeSayisi()),
                            "tur", k.getKatilimTuru().name()))));
        }

        if (sayfa > 0) {
            envanter.setItem(48, Esya.olustur(Material.ARROW, mesajlar.baslik("admin.klan-listesi.onceki-sayfa")));
        }
        if (bitis < klanlar.size()) {
            envanter.setItem(50, Esya.olustur(Material.ARROW, mesajlar.baslik("admin.klan-listesi.sonraki-sayfa")));
        }
        envanter.setItem(49, Esya.olustur(Material.BARRIER, mesajlar.baslik("admin.klan-listesi.geri")));
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        int slot = olay.getSlot();
        int baslangic = sayfa * SAYFA_BASI;

        if (slot == 49) {
            new AdminAnaMenu(eklenti, oyuncu).ac();
            return;
        }
        if (slot == 48 && sayfa > 0) {
            new AdminKlanListesiMenu(eklenti, oyuncu, sayfa - 1).ac();
            return;
        }
        if (slot == 50 && baslangic + SAYFA_BASI < klanlar.size()) {
            new AdminKlanListesiMenu(eklenti, oyuncu, sayfa + 1).ac();
            return;
        }

        if (slot < SAYFA_BASI && baslangic + slot < klanlar.size()) {
            Klan secilen = klanlar.get(baslangic + slot);
            new AdminKlanDetayMenu(eklenti, oyuncu, secilen).ac();
        }
    }
}

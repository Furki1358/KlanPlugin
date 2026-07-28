package com.klaneklentisi.klan.gui;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.manager.KlanYoneticisi;
import com.klaneklentisi.klan.model.Klan;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KlanSecimMenu extends Menu {

    private static final int SAYFA_BASI = 45;

    private final Klan kaynakKlan;
    private final boolean muttefik;
    private final int sayfa;
    private final List<Klan> uygunKlanlar;

    public KlanSecimMenu(KlanEklentisi eklenti, Player oyuncu, Klan kaynakKlan, boolean muttefik, int sayfa) {
        super(eklenti, oyuncu);
        this.kaynakKlan = kaynakKlan;
        this.muttefik = muttefik;
        this.sayfa = sayfa;
        this.uygunKlanlar = new ArrayList<>();
        for (Klan k : yonetici.tumKlanlar()) {
            if (k.getIsim().equalsIgnoreCase(kaynakKlan.getIsim())) continue;
            boolean zatenIliskili = muttefik ? kaynakKlan.muttefikMi(k.getIsim()) : kaynakKlan.rakipMi(k.getIsim());
            if (!zatenIliskili) uygunKlanlar.add(k);
        }
    }

    @Override
    protected int boyut() {
        return 54;
    }

    @Override
    protected String baslik() {
        return mesajlar.baslik("menu.klan-sec.baslik");
    }

    @Override
    protected void doldur() {
        for (int i = 0; i < boyut(); i++) {
            envanter.setItem(i, Esya.doldurucu());
        }

        int baslangic = sayfa * SAYFA_BASI;
        int bitis = Math.min(baslangic + SAYFA_BASI, uygunKlanlar.size());

        for (int i = baslangic; i < bitis; i++) {
            Klan k = uygunKlanlar.get(i);
            envanter.setItem(i - baslangic, Esya.olustur(Material.PAPER,
                    "&f" + k.getIsim() + " &8[&7" + k.getEtiket() + "&8]"));
        }

        if (sayfa > 0) {
            envanter.setItem(48, Esya.olustur(Material.ARROW, mesajlar.baslik("menu.klan-sec.onceki-sayfa")));
        }
        if (bitis < uygunKlanlar.size()) {
            envanter.setItem(50, Esya.olustur(Material.ARROW, mesajlar.baslik("menu.klan-sec.sonraki-sayfa")));
        }
        envanter.setItem(49, Esya.olustur(Material.BARRIER, mesajlar.baslik("menu.klan-sec.geri")));
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        int slot = olay.getSlot();
        int baslangic = sayfa * SAYFA_BASI;

        if (slot == 49) {
            new MuttefikRakipMenu(eklenti, oyuncu, kaynakKlan, muttefik).ac();
            return;
        }
        if (slot == 48 && sayfa > 0) {
            new KlanSecimMenu(eklenti, oyuncu, kaynakKlan, muttefik, sayfa - 1).ac();
            return;
        }
        if (slot == 50 && baslangic + SAYFA_BASI < uygunKlanlar.size()) {
            new KlanSecimMenu(eklenti, oyuncu, kaynakKlan, muttefik, sayfa + 1).ac();
            return;
        }

        if (slot < SAYFA_BASI && baslangic + slot < uygunKlanlar.size()) {
            Klan secilen = uygunKlanlar.get(baslangic + slot);
            KlanYoneticisi.Sonuc sonuc = muttefik
                    ? yonetici.muttefikEkle(kaynakKlan, secilen)
                    : yonetici.rakipEkle(kaynakKlan, secilen);

            String anahtar = (muttefik ? "muttefik" : "rakip") + switch (sonuc) {
                case BASARILI -> ".eklendi";
                case KENDISI -> ".kendine";
                case ZATEN_MUTTEFIK, ZATEN_RAKIP -> ".zaten";
                case SINIR_ASILDI -> ".sinir";
                default -> ".klan-yok";
            };
            oyuncu.sendMessage(mesajlar.al(anahtar, Map.of("klan", secilen.getIsim())));
            new MuttefikRakipMenu(eklenti, oyuncu, kaynakKlan, muttefik).ac();
        }
    }
}

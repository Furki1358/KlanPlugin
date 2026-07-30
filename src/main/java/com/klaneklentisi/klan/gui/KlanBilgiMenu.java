package com.klaneklentisi.klan.gui;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.manager.KlanYoneticisi;
import com.klaneklentisi.klan.model.KatilimTuru;
import com.klaneklentisi.klan.model.Klan;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KlanBilgiMenu extends Menu {

    private final Klan klan;

    public KlanBilgiMenu(KlanEklentisi eklenti, Player oyuncu, Klan klan) {
        super(eklenti, oyuncu);
        this.klan = klan;
    }

    @Override
    protected int boyut() {
        return 27;
    }

    @Override
    protected String baslik() {
        return mesajlar.baslik("menu.klan-bilgi.baslik", Map.of("isim", klan.getIsim()));
    }

    @Override
    protected void doldur() {
        for (int i = 0; i < boyut(); i++) {
            envanter.setItem(i, Esya.doldurucu());
        }

        String liderAdi = Bukkit.getOfflinePlayer(klan.getKurucu()).getName();
        List<String> bilgi = new ArrayList<>();
        bilgi.add("&7Etiket: &f[" + klan.getEtiket() + "]");
        bilgi.add("&7Lider: &f" + (liderAdi == null ? "?" : liderAdi));
        bilgi.add("&7Üye sayısı: &f" + klan.getUyeSayisi());
        bilgi.add("&7Katılım: &f" + klan.getKatilimTuru().name());
        if (!klan.getAciklama().isEmpty()) {
            bilgi.add("&7Açıklama: &f" + klan.getAciklama());
        }
        envanter.setItem(13, Esya.olustur(Material.BOOK, "&f" + klan.getIsim(), renkliListe(bilgi)));

        if (klan.getKatilimTuru() == KatilimTuru.ACIK) {
            envanter.setItem(15, Esya.olustur(Material.LIME_WOOL, mesajlar.baslik("menu.klan-bilgi.katil")));
        } else {
            envanter.setItem(15, Esya.olustur(Material.RED_WOOL, mesajlar.baslik("menu.klan-bilgi.katil-kapali")));
        }

        envanter.setItem(22, Esya.olustur(Material.ARROW, mesajlar.baslik("menu.klan-bilgi.geri")));
    }

    private List<String> renkliListe(List<String> satirlar) {
        List<String> sonuc = new ArrayList<>();
        for (String s : satirlar) {
            sonuc.add(com.klaneklentisi.klan.util.Mesajlar.renkli(s));
        }
        return sonuc;
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        int slot = olay.getSlot();
        if (slot == 22) {
            new KlanListesiMenu(eklenti, oyuncu, 0).ac();
            return;
        }
        if (slot == 15 && klan.getKatilimTuru() == KatilimTuru.ACIK) {
            if (!izinVarMi("KATIL")) return;
            KlanYoneticisi.Sonuc sonuc = yonetici.klanaKatil(klan, oyuncu);
            switch (sonuc) {
                case BASARILI -> {
                    oyuncu.sendMessage(mesajlar.al("katil.basarili", Map.of("klan", klan.getIsim())));
                    oyuncu.closeInventory();
                }
                case ZATEN_KLANDA -> oyuncu.sendMessage(mesajlar.al("katil.zaten-klanda"));
                default -> oyuncu.sendMessage(mesajlar.al("katil.kapali"));
            }
        }
    }
}

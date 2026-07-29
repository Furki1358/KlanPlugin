package com.klaneklentisi.klan.gui;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.model.Klan;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AnaMenu extends Menu {

    private final Klan klan; // null olabilir (oyuncu klansızsa)

    public AnaMenu(KlanEklentisi eklenti, Player oyuncu) {
        super(eklenti, oyuncu);
        this.klan = yonetici.klanBul(oyuncu.getUniqueId()).orElse(null);
    }

    @Override
    protected int boyut() {
        return 27;
    }

    @Override
    protected String baslik() {
        return klan == null
                ? mesajlar.baslik("menu.ana.klanin-yok-baslik")
                : mesajlar.baslik("menu.ana.baslik");
    }

    @Override
    protected void doldur() {
        for (int i = 0; i < boyut(); i++) {
            envanter.setItem(i, Esya.doldurucu());
        }

        Optional<Klan> davetOpt = yonetici.davetiGetir(oyuncu.getUniqueId());
        if (davetOpt.isPresent()) {
            envanter.setItem(4, Esya.olustur(Material.WRITTEN_BOOK,
                    mesajlar.baslik("menu.ana.davet-var"),
                    mesajlar.alListe("menu.ana.davet-aciklama", Map.of("klan", davetOpt.get().getIsim()))));
        }

        if (klan == null) {
            envanter.setItem(13, Esya.olustur(Material.NETHER_STAR,
                    mesajlar.baslik("menu.ana.olustur"), mesajlar.alListe("menu.ana.olustur-aciklama")));
            envanter.setItem(20, Esya.olustur(Material.GOLDEN_SWORD, mesajlar.baslik("menu.ana.liderlik")));
            envanter.setItem(22, Esya.olustur(Material.BARRIER, mesajlar.baslik("menu.ana.kapat")));
            return;
        }

        envanter.setItem(10, Esya.olustur(Material.BOOK, mesajlar.baslik("menu.ana.bilgi")));
        envanter.setItem(11, Esya.olustur(Material.PLAYER_HEAD, mesajlar.baslik("menu.ana.uyeler")));
        envanter.setItem(12, Esya.olustur(Material.ANVIL, mesajlar.baslik("menu.ana.ayarlar")));
        envanter.setItem(13, Esya.olustur(Material.LIME_DYE, mesajlar.baslik("menu.ana.muttefikler")));
        envanter.setItem(14, Esya.olustur(Material.RED_DYE, mesajlar.baslik("menu.ana.rakipler")));
        envanter.setItem(15, Esya.olustur(Material.MAP, mesajlar.baslik("menu.ana.klan-listesi")));
        envanter.setItem(16, Esya.olustur(Material.COMPASS, mesajlar.baslik("menu.ana.us")));

        boolean sohbetAcik = yonetici.sohbetModuAcikMi(oyuncu.getUniqueId());
        envanter.setItem(19, Esya.olustur(Material.WRITABLE_BOOK,
                mesajlar.baslik(sohbetAcik ? "menu.ana.sohbet-ac" : "menu.ana.sohbet-kapali")));
        envanter.setItem(20, Esya.olustur(Material.GOLDEN_SWORD, mesajlar.baslik("menu.ana.liderlik")));

        envanter.setItem(22, Esya.olustur(Material.BARRIER, mesajlar.baslik("menu.ana.kapat")));
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        int slot = olay.getSlot();

        if (slot == 4 && yonetici.davetiGetir(oyuncu.getUniqueId()).isPresent()) {
            Klan davetKlani = yonetici.davetiGetir(oyuncu.getUniqueId()).get();
            new DavetMenu(eklenti, oyuncu, davetKlani).ac();
            return;
        }

        if (klan == null) {
            if (slot == 20) new LiderlikMenu(eklenti, oyuncu, 0).ac();
            if (slot == 22) oyuncu.closeInventory();
            return;
        }

        switch (slot) {
            case 10 -> {
                oyuncu.closeInventory();
                eklenti.getKlanKomutu().bilgiGoster(oyuncu, klan);
            }
            case 11 -> new UyelerMenu(eklenti, oyuncu, klan, 0).ac();
            case 12 -> new AyarlarMenu(eklenti, oyuncu, klan).ac();
            case 13 -> new MuttefikRakipMenu(eklenti, oyuncu, klan, true).ac();
            case 14 -> new MuttefikRakipMenu(eklenti, oyuncu, klan, false).ac();
            case 15 -> new KlanListesiMenu(eklenti, oyuncu, 0).ac();
            case 16 -> {
                oyuncu.closeInventory();
                eklenti.getKlanKomutu().usaIsinlan(oyuncu);
            }
            case 19 -> {
                yonetici.sohbetModunuDegistir(oyuncu.getUniqueId());
                yenile();
            }
            case 20 -> new LiderlikMenu(eklenti, oyuncu, 0).ac();
            case 22 -> oyuncu.closeInventory();
            default -> {}
        }
    }
}

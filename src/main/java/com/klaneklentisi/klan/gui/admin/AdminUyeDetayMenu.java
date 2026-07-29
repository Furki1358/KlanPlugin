package com.klaneklentisi.klan.gui.admin;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.gui.Esya;
import com.klaneklentisi.klan.gui.Menu;
import com.klaneklentisi.klan.manager.KlanYoneticisi;
import com.klaneklentisi.klan.model.Klan;
import com.klaneklentisi.klan.model.Rutbe;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminUyeDetayMenu extends Menu {

    private final Klan klan;
    private final UUID hedefUid;
    private final int gelinenSayfa;

    public AdminUyeDetayMenu(KlanEklentisi eklenti, Player oyuncu, Klan klan, UUID hedefUid, int gelinenSayfa) {
        super(eklenti, oyuncu);
        this.klan = klan;
        this.hedefUid = hedefUid;
        this.gelinenSayfa = gelinenSayfa;
    }

    private OfflinePlayer hedef() {
        return Bukkit.getOfflinePlayer(hedefUid);
    }

    @Override
    protected int boyut() {
        return 27;
    }

    @Override
    protected String baslik() {
        String isim = hedef().getName() == null ? hedefUid.toString().substring(0, 8) : hedef().getName();
        return mesajlar.baslik("admin.uye-detay.baslik", Map.of("oyuncu", isim));
    }

    @Override
    protected void doldur() {
        for (int i = 0; i < boyut(); i++) {
            envanter.setItem(i, Esya.doldurucu());
        }

        Rutbe hedefRutbe = klan.getRutbe(hedefUid);
        String isim = hedef().getName() == null ? hedefUid.toString().substring(0, 8) : hedef().getName();
        envanter.setItem(13, Esya.oyuncuKafasi(hedef(), "&f" + isim,
                List.of("&7▪ Rütbe: &f★ " + (hedefRutbe == null ? "-" : hedefRutbe.getGorunenAd()))));

        envanter.setItem(11, Esya.olustur(Material.NETHER_STAR, mesajlar.baslik("admin.uye-detay.lider-yap"),
                List.of(mesajlar.baslik("admin.uye-detay.lider-yap-aciklama"))));
        envanter.setItem(15, Esya.olustur(Material.BARRIER, mesajlar.baslik("admin.uye-detay.at"),
                List.of(mesajlar.baslik("admin.uye-detay.at-aciklama"))));
        envanter.setItem(22, Esya.olustur(Material.ARROW, mesajlar.baslik("admin.uye-detay.geri")));
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        switch (olay.getSlot()) {
            case 11 -> {
                yonetici.zorlaLiderYap(klan, hedefUid);
                new AdminKlanDetayMenu(eklenti, oyuncu, klan).ac();
            }
            case 15 -> {
                KlanYoneticisi.Sonuc sonuc = yonetici.zorlaCikar(klan, hedefUid);
                if (sonuc == KlanYoneticisi.Sonuc.BASARILI && yonetici.klanBul(klan.getIsim()).isPresent()) {
                    new AdminUyeYonetimMenu(eklenti, oyuncu, klan, gelinenSayfa).ac();
                } else {
                    new AdminKlanListesiMenu(eklenti, oyuncu, 0).ac();
                }
            }
            case 22 -> new AdminUyeYonetimMenu(eklenti, oyuncu, klan, gelinenSayfa).ac();
            default -> {}
        }
    }
}

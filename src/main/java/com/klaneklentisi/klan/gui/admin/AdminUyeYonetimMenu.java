package com.klaneklentisi.klan.gui.admin;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.gui.Esya;
import com.klaneklentisi.klan.gui.Menu;
import com.klaneklentisi.klan.model.Klan;
import com.klaneklentisi.klan.model.Rutbe;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminUyeYonetimMenu extends Menu {

    private static final int SAYFA_BASI = 45;

    private final Klan klan;
    private final int sayfa;
    private final List<UUID> uyeListesi;

    public AdminUyeYonetimMenu(KlanEklentisi eklenti, Player oyuncu, Klan klan, int sayfa) {
        super(eklenti, oyuncu);
        this.klan = klan;
        this.sayfa = sayfa;
        this.uyeListesi = new ArrayList<>(klan.getUyeler().keySet());
    }

    @Override
    protected int boyut() {
        return 54;
    }

    @Override
    protected String baslik() {
        return mesajlar.baslik("admin.uye-yonetimi.baslik", Map.of("isim", klan.getIsim()));
    }

    @Override
    protected void doldur() {
        for (int i = 0; i < boyut(); i++) {
            envanter.setItem(i, Esya.doldurucu());
        }

        int baslangic = sayfa * SAYFA_BASI;
        int bitis = Math.min(baslangic + SAYFA_BASI, uyeListesi.size());

        for (int i = baslangic; i < bitis; i++) {
            UUID uid = uyeListesi.get(i);
            OfflinePlayer hedef = Bukkit.getOfflinePlayer(uid);
            Rutbe rutbe = klan.getRutbe(uid);
            String isim = hedef.getName() == null ? uid.toString().substring(0, 8) : hedef.getName();
            envanter.setItem(i - baslangic, Esya.oyuncuKafasi(hedef, "&f" + isim,
                    List.of("&7▪ Rütbe: &f★ " + (rutbe == null ? "-" : rutbe.getGorunenAd()))));
        }

        if (sayfa > 0) {
            envanter.setItem(48, Esya.olustur(Material.ARROW, mesajlar.baslik("admin.uye-yonetimi.onceki-sayfa")));
        }
        if (bitis < uyeListesi.size()) {
            envanter.setItem(50, Esya.olustur(Material.ARROW, mesajlar.baslik("admin.uye-yonetimi.sonraki-sayfa")));
        }
        envanter.setItem(49, Esya.olustur(Material.BARRIER, mesajlar.baslik("admin.uye-yonetimi.geri")));
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        int slot = olay.getSlot();
        int baslangic = sayfa * SAYFA_BASI;

        if (slot == 49) {
            new AdminKlanDetayMenu(eklenti, oyuncu, klan).ac();
            return;
        }
        if (slot == 48 && sayfa > 0) {
            new AdminUyeYonetimMenu(eklenti, oyuncu, klan, sayfa - 1).ac();
            return;
        }
        if (slot == 50 && baslangic + SAYFA_BASI < uyeListesi.size()) {
            new AdminUyeYonetimMenu(eklenti, oyuncu, klan, sayfa + 1).ac();
            return;
        }

        if (slot < SAYFA_BASI && baslangic + slot < uyeListesi.size()) {
            UUID hedefUid = uyeListesi.get(baslangic + slot);
            new AdminUyeDetayMenu(eklenti, oyuncu, klan, hedefUid, sayfa).ac();
        }
    }
}

package com.klaneklentisi.klan.gui.admin;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.gui.Esya;
import com.klaneklentisi.klan.gui.Menu;
import com.klaneklentisi.klan.model.KatilimTuru;
import com.klaneklentisi.klan.model.Klan;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Map;

public class AdminKlanDetayMenu extends Menu {

    private final Klan klan;

    public AdminKlanDetayMenu(KlanEklentisi eklenti, Player oyuncu, Klan klan) {
        super(eklenti, oyuncu);
        this.klan = klan;
    }

    @Override
    protected int boyut() {
        return 54;
    }

    @Override
    protected String baslik() {
        return mesajlar.baslik("admin.klan-detay.baslik", Map.of("isim", klan.getIsim()));
    }

    private void dolduruCam(int... slotlar) {
        for (int s : slotlar) {
            envanter.setItem(s, Esya.olustur(Material.WHITE_STAINED_GLASS_PANE, " "));
        }
    }

    @Override
    protected void doldur() {
        kenarCiz();
        dolduruCam(19, 20, 21, 23, 24, 25, 28, 29, 30, 32, 33, 34);

        envanter.setItem(10, Esya.olustur(Material.BOOK, mesajlar.baslik("admin.klan-detay.bilgi")));
        envanter.setItem(12, Esya.olustur(Material.COMPARATOR,
                mesajlar.baslik("admin.klan-detay.katilim-turu", Map.of("tur", klan.getKatilimTuru().name())),
                List.of(mesajlar.baslik("admin.klan-detay.katilim-turu-aciklama"))));
        envanter.setItem(14, Esya.olustur(Material.PLAYER_HEAD, mesajlar.baslik("admin.klan-detay.uye-yonetimi")));
        envanter.setItem(16, Esya.olustur(Material.COMPASS, mesajlar.baslik("admin.klan-detay.us-sil"),
                List.of(mesajlar.baslik("admin.klan-detay.us-sil-aciklama"))));
        envanter.setItem(22, Esya.olustur(Material.TNT, mesajlar.baslik("admin.klan-detay.zorla-sil"),
                List.of(mesajlar.baslik("admin.klan-detay.zorla-sil-aciklama"))));
        envanter.setItem(31, Esya.olustur(Material.ARROW, mesajlar.baslik("admin.klan-detay.geri")));
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        switch (olay.getSlot()) {
            case 10 -> {
                oyuncu.closeInventory();
                eklenti.getKlanKomutu().bilgiGoster(oyuncu, klan);
            }
            case 12 -> {
                KatilimTuru yeni = klan.getKatilimTuru() == KatilimTuru.ACIK ? KatilimTuru.DAVETLI : KatilimTuru.ACIK;
                yonetici.zorlaKatilimTuru(klan, yeni);
                yenile();
            }
            case 14 -> new AdminUyeYonetimMenu(eklenti, oyuncu, klan, 0).ac();
            case 16 -> {
                yonetici.zorlaUsSil(klan);
                oyuncu.sendMessage(mesajlar.al("yonetim.yenilendi"));
                yenile();
            }
            case 22 -> {
                oyuncu.closeInventory();
                String klanIsmi = klan.getIsim();
                oyuncu.sendMessage(mesajlar.al("menu.girdi.sil-onay-iste"));
                var onayButon = com.klaneklentisi.klan.util.Butonlar.buton(
                        mesajlar.hamMetin("sil.onay-buton"),
                        net.kyori.adventure.text.format.NamedTextColor.RED,
                        mesajlar.hamMetin("sil.onay-ipucu"),
                        p -> {
                            yonetici.klanSil(klanIsmi);
                            p.sendMessage(mesajlar.al("yonetim.silindi", Map.of("isim", klanIsmi)));
                        });
                oyuncu.sendMessage(onayButon);
            }
            case 31 -> new AdminKlanListesiMenu(eklenti, oyuncu, 0).ac();
            default -> {}
        }
    }
}

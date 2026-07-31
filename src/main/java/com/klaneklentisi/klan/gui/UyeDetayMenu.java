package com.klaneklentisi.klan.gui;

import com.klaneklentisi.klan.KlanEklentisi;
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

public class UyeDetayMenu extends Menu {

    private final Klan klan;
    private final UUID hedefUid;
    private final int gelinenSayfa;

    public UyeDetayMenu(KlanEklentisi eklenti, Player oyuncu, Klan klan, UUID hedefUid, int gelinenSayfa) {
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
        return 54;
    }

    @Override
    protected String baslik() {
        String isim = hedef().getName() == null ? hedefUid.toString().substring(0, 8) : hedef().getName();
        return mesajlar.baslik("menu.uye-detay.baslik", Map.of("oyuncu", isim));
    }

    private void dolduruCam(int... slotlar) {
        for (int s : slotlar) {
            envanter.setItem(s, Esya.olustur(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
    }

    @Override
    protected void doldur() {
        kenarCiz();

        Rutbe yetkiliRutbe = klan.getRutbe(oyuncu.getUniqueId());
        Rutbe hedefRutbe = klan.getRutbe(hedefUid);
        boolean liderMi = yetkiliRutbe == Rutbe.LIDER;
        boolean kendisiMi = oyuncu.getUniqueId().equals(hedefUid);

        var ist = eklenti.getIstatistikYoneticisi().getIstatistik(hedefUid);
        String isim = hedef().getName() == null ? hedefUid.toString().substring(0, 8) : hedef().getName();
        envanter.setItem(13, Esya.oyuncuKafasi(hedef(), "&f" + isim, List.of(
                "&7▪ Rütbe: &f" + (hedefRutbe == null ? "-" : com.klaneklentisi.klan.util.Mesajlar.renkli(hedefRutbe.getGorunenAd())),
                "&a▪ Öldürme: &f" + ist.getOldurme(),
                "&c▪ Ölme: &f" + ist.getOlme(),
                "&7▪ K/D: &f" + ist.getOran()
        )));

        dolduruCam(10, 12, 14, 16, 19, 20, 24, 25, 28, 29, 30, 32, 33, 34);

        if (liderMi && !kendisiMi && hedefRutbe != Rutbe.LIDER) {
            envanter.setItem(11, Esya.olustur(Material.LIME_DYE, mesajlar.baslik("menu.uye-detay.terfi"),
                    List.of(mesajlar.baslik("menu.uye-detay.terfi-aciklama"))));
            envanter.setItem(15, Esya.olustur(Material.GRAY_DYE, mesajlar.baslik("menu.uye-detay.indir"),
                    List.of(mesajlar.baslik("menu.uye-detay.indir-aciklama"))));
            envanter.setItem(19, Esya.olustur(Material.BARRIER, mesajlar.baslik("menu.uye-detay.at"),
                    List.of(mesajlar.baslik("menu.uye-detay.at-aciklama"))));
            envanter.setItem(20, Esya.olustur(Material.NETHER_STAR, mesajlar.baslik("menu.uye-detay.devret"),
                    List.of(mesajlar.baslik("menu.uye-detay.devret-aciklama"))));
        } else if (!kendisiMi && yetkiliRutbe != null && hedefRutbe != null
                && yetkiliRutbe.getSeviye() > hedefRutbe.getSeviye()) {
            envanter.setItem(19, Esya.olustur(Material.BARRIER, mesajlar.baslik("menu.uye-detay.at"),
                    List.of(mesajlar.baslik("menu.uye-detay.at-aciklama"))));
        }

        envanter.setItem(31, Esya.olustur(Material.ARROW, mesajlar.baslik("menu.uye-detay.geri")));
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        int slot = olay.getSlot();
        KlanYoneticisi.Sonuc sonuc;

        switch (slot) {
            case 11 -> {
                if (!izinVarMi("TERFI")) return;
                sonuc = yonetici.rutbeYukselt(klan, oyuncu, hedef());
                mesajGoster(sonuc, "yukselt");
                yenile();
            }
            case 15 -> {
                if (!izinVarMi("INDIR")) return;
                sonuc = yonetici.rutbeIndir(klan, oyuncu, hedef());
                mesajGoster(sonuc, "indir");
                yenile();
            }
            case 19 -> {
                if (!izinVarMi("AT")) return;
                sonuc = yonetici.uyeAt(klan, oyuncu, hedef());
                if (sonuc == KlanYoneticisi.Sonuc.BASARILI) {
                    new UyelerMenu(eklenti, oyuncu, klan, gelinenSayfa).ac();
                } else {
                    oyuncu.sendMessage(mesajlar.al("menu.uye-detay.yetkisiz"));
                }
            }
            case 20 -> {
                if (!izinVarMi("DEVRET")) return;
                sonuc = yonetici.liderlikDevret(klan, oyuncu, hedef());
                if (sonuc == KlanYoneticisi.Sonuc.BASARILI) {
                    new AnaMenu(eklenti, oyuncu).ac();
                } else {
                    oyuncu.sendMessage(mesajlar.al("menu.uye-detay.yetkisiz"));
                }
            }
            case 31 -> new UyelerMenu(eklenti, oyuncu, klan, gelinenSayfa).ac();
            default -> {}
        }
    }

    private void mesajGoster(KlanYoneticisi.Sonuc sonuc, String komutOnEki) {
        if (sonuc == KlanYoneticisi.Sonuc.BASARILI) {
            Rutbe yeniRutbe = klan.getRutbe(hedefUid);
            String isim = hedef().getName() == null ? hedefUid.toString().substring(0, 8) : hedef().getName();
            oyuncu.sendMessage(mesajlar.al(komutOnEki + ".basarili", Map.of(
                    "oyuncu", isim, "rutbe", yeniRutbe == null ? "-" : yeniRutbe.getGorunenAd())));
        } else {
            oyuncu.sendMessage(mesajlar.al("menu.uye-detay.yetkisiz"));
        }
    }
}

package com.klaneklentisi.klan.gui;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.model.KatilimTuru;
import com.klaneklentisi.klan.model.Klan;
import com.klaneklentisi.klan.model.Rutbe;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Map;

public class AyarlarMenu extends Menu {

    private final Klan klan;

    public AyarlarMenu(KlanEklentisi eklenti, Player oyuncu, Klan klan) {
        super(eklenti, oyuncu);
        this.klan = klan;
    }

    @Override
    protected int boyut() {
        return 27;
    }

    @Override
    protected String baslik() {
        return mesajlar.baslik("menu.ayarlar.baslik");
    }

    @Override
    protected void doldur() {
        for (int i = 0; i < boyut(); i++) {
            envanter.setItem(i, Esya.doldurucu());
        }

        envanter.setItem(11, Esya.olustur(Material.COMPARATOR,
                mesajlar.baslik("menu.ayarlar.katilim-turu", Map.of("tur", klan.getKatilimTuru().name())),
                List.of(mesajlar.baslik("menu.ayarlar.katilim-turu-aciklama"))));

        envanter.setItem(13, Esya.olustur(Material.NAME_TAG, mesajlar.baslik("menu.ayarlar.etiket"),
                List.of(mesajlar.baslik("menu.ayarlar.etiket-aciklama", Map.of("etiket", klan.getEtiket())))));

        envanter.setItem(15, Esya.olustur(Material.WRITABLE_BOOK, mesajlar.baslik("menu.ayarlar.aciklama")));

        envanter.setItem(21, Esya.olustur(Material.TNT, mesajlar.baslik("menu.ayarlar.sil"),
                List.of(mesajlar.baslik("menu.ayarlar.sil-aciklama"))));

        envanter.setItem(26, Esya.olustur(Material.ARROW, mesajlar.baslik("menu.ayarlar.geri")));
    }

    private boolean yetkiVarMi(Rutbe minimum) {
        Rutbe rutbe = klan.getRutbe(oyuncu.getUniqueId());
        return rutbe != null && rutbe.getSeviye() >= minimum.getSeviye();
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        switch (olay.getSlot()) {
            case 11 -> {
                if (!yetkiVarMi(Rutbe.YONETICI)) {
                    oyuncu.sendMessage(mesajlar.al("menu.ayarlar.yetkisiz"));
                    return;
                }
                klan.setKatilimTuru(klan.getKatilimTuru() == KatilimTuru.ACIK ? KatilimTuru.DAVETLI : KatilimTuru.ACIK);
                yonetici.kaydet(klan);
                yenile();
            }
            case 13 -> {
                if (!yetkiVarMi(Rutbe.LIDER)) {
                    oyuncu.sendMessage(mesajlar.al("menu.ayarlar.yetkisiz"));
                    return;
                }
                oyuncu.closeInventory();
                oyuncu.sendMessage(mesajlar.al("menu.girdi.etiket-iste"));
                eklenti.getGirdiYoneticisi().girdiBekle(oyuncu.getUniqueId(), metin -> etiketIsle(metin));
            }
            case 15 -> {
                if (!yetkiVarMi(Rutbe.YONETICI)) {
                    oyuncu.sendMessage(mesajlar.al("menu.ayarlar.yetkisiz"));
                    return;
                }
                oyuncu.closeInventory();
                oyuncu.sendMessage(mesajlar.al("menu.girdi.aciklama-iste"));
                eklenti.getGirdiYoneticisi().girdiBekle(oyuncu.getUniqueId(), metin -> aciklamaIsle(metin));
            }
            case 21 -> {
                if (!yetkiVarMi(Rutbe.LIDER)) {
                    oyuncu.sendMessage(mesajlar.al("menu.ayarlar.yetkisiz"));
                    return;
                }
                oyuncu.closeInventory();
                String klanIsmi = klan.getIsim();
                oyuncu.sendMessage(mesajlar.al("sil.onay-gerekli"));
                var onayButon = com.klaneklentisi.klan.util.Butonlar.buton(
                        mesajlar.hamMetin("sil.onay-buton"),
                        net.kyori.adventure.text.format.NamedTextColor.RED,
                        mesajlar.hamMetin("sil.onay-ipucu"),
                        p -> {
                            var guncelKlan = yonetici.klanBul(p.getUniqueId());
                            if (guncelKlan.isPresent() && guncelKlan.get().getIsim().equalsIgnoreCase(klanIsmi)
                                    && guncelKlan.get().getRutbe(p.getUniqueId()) == com.klaneklentisi.klan.model.Rutbe.LIDER) {
                                yonetici.klanSil(klanIsmi);
                                p.sendMessage(mesajlar.al("sil.basarili"));
                            }
                        });
                oyuncu.sendMessage(onayButon);
            }
            case 26 -> new AnaMenu(eklenti, oyuncu).ac();
            default -> {}
        }
    }

    private void etiketIsle(String metin) {
        if (metin.equalsIgnoreCase("iptal")) {
            oyuncu.sendMessage(mesajlar.al("menu.girdi.iptal-edildi"));
            return;
        }
        int min = eklenti.getConfig().getInt("genel.min-etiket-uzunlugu", 2);
        int maks = eklenti.getConfig().getInt("genel.maks-etiket-uzunlugu", 6);
        if (metin.length() < min || metin.length() > maks) {
            oyuncu.sendMessage(mesajlar.al("etiket.gecersiz", Map.of("min", String.valueOf(min), "maks", String.valueOf(maks))));
            return;
        }
        boolean kullanimda = yonetici.tumKlanlar().stream()
                .anyMatch(k -> !k.getIsim().equalsIgnoreCase(klan.getIsim()) && k.getEtiket().equalsIgnoreCase(metin));
        if (kullanimda) {
            oyuncu.sendMessage(mesajlar.al("etiket.kullanimda"));
            return;
        }
        klan.setEtiket(metin);
        yonetici.kaydet(klan);
        oyuncu.sendMessage(mesajlar.al("etiket.basarili", Map.of("etiket", metin)));
    }

    private void aciklamaIsle(String metin) {
        if (metin.equalsIgnoreCase("iptal")) {
            oyuncu.sendMessage(mesajlar.al("menu.girdi.iptal-edildi"));
            return;
        }
        klan.setAciklama(metin);
        yonetici.kaydet(klan);
        oyuncu.sendMessage(mesajlar.al("aciklama.basarili"));
    }

}

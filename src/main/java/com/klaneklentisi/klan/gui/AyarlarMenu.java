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
        return 54;
    }

    @Override
    protected String baslik() {
        return mesajlar.baslik("menu.ayarlar.baslik");
    }

    private void dolduruCam(int... slotlar) {
        for (int s : slotlar) {
            envanter.setItem(s, Esya.olustur(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
    }

    @Override
    protected void doldur() {
        kenarCiz();
        dolduruCam(12, 14, 19, 20, 21, 23, 24, 25, 28, 29, 30, 32, 33, 34);

        envanter.setItem(10, Esya.olustur(Material.COMPARATOR,
                mesajlar.baslik("menu.ayarlar.katilim-turu", Map.of("tur", klan.getKatilimTuru().name())),
                List.of(mesajlar.baslik("menu.ayarlar.katilim-turu-aciklama"))));

        envanter.setItem(11, Esya.olustur(Material.NAME_TAG, mesajlar.baslik("menu.ayarlar.etiket"),
                List.of(mesajlar.baslik("menu.ayarlar.etiket-aciklama", Map.of("etiket", klan.getEtiket())))));

        envanter.setItem(13, Esya.olustur(Material.ITEM_FRAME, mesajlar.baslik("menu.sembol.baslik")));

        envanter.setItem(15, Esya.olustur(Material.WRITABLE_BOOK, mesajlar.baslik("menu.ayarlar.aciklama")));

        envanter.setItem(16, Esya.olustur(Material.TNT, mesajlar.baslik("menu.ayarlar.sil"),
                List.of(mesajlar.baslik("menu.ayarlar.sil-aciklama"))));

        envanter.setItem(22, klanOnizlemeEsyasi());

        envanter.setItem(31, Esya.olustur(Material.ARROW, mesajlar.baslik("menu.ayarlar.geri")));
    }

    private org.bukkit.inventory.ItemStack klanOnizlemeEsyasi() {
        List<String> lore = List.of(
                com.klaneklentisi.klan.util.Mesajlar.renkli("&7Etiket: &f[" + klan.getEtiket() + "]"),
                com.klaneklentisi.klan.util.Mesajlar.renkli("&7Katılım: &f" + klan.getKatilimTuru().name()));
        if (klan.getSembol() != null) {
            var esya = klan.getSembol().clone();
            esya.setAmount(1);
            var meta = esya.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(com.klaneklentisi.klan.util.Mesajlar.renkli("&6" + klan.getIsim()));
                meta.setLore(lore);
                esya.setItemMeta(meta);
            }
            return esya;
        }
        return Esya.olustur(Material.CHEST, "&6" + klan.getIsim(), lore);
    }

    private boolean yetkiVarMi(Rutbe minimum) {
        Rutbe rutbe = klan.getRutbe(oyuncu.getUniqueId());
        return rutbe != null && rutbe.getSeviye() >= minimum.getSeviye();
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        switch (olay.getSlot()) {
            case 10 -> {
                if (!izinVarMi("KATILIMTURU")) return;
                if (!yetkiVarMi(Rutbe.YONETICI)) {
                    oyuncu.sendMessage(mesajlar.al("menu.ayarlar.yetkisiz"));
                    return;
                }
                klan.setKatilimTuru(klan.getKatilimTuru() == KatilimTuru.ACIK ? KatilimTuru.DAVETLI : KatilimTuru.ACIK);
                yonetici.kaydet(klan);
                yenile();
            }
            case 11 -> {
                if (!izinVarMi("ETIKET")) return;
                if (!yetkiVarMi(Rutbe.LIDER)) {
                    oyuncu.sendMessage(mesajlar.al("menu.ayarlar.yetkisiz"));
                    return;
                }
                oyuncu.closeInventory();
                oyuncu.sendMessage(mesajlar.al("menu.girdi.etiket-iste"));
                eklenti.getGirdiYoneticisi().girdiBekle(oyuncu.getUniqueId(), metin -> etiketIsle(metin));
            }
            case 13 -> {
                if (!izinVarMi("SEMBOL")) return;
                if (!yetkiVarMi(Rutbe.YONETICI)) {
                    oyuncu.sendMessage(mesajlar.al("menu.ayarlar.yetkisiz"));
                    return;
                }
                new com.klaneklentisi.klan.gui.SembolAyarlaMenu(eklenti, oyuncu, klan).ac();
            }
            case 15 -> {
                if (!izinVarMi("ACIKLAMA")) return;
                if (!yetkiVarMi(Rutbe.YONETICI)) {
                    oyuncu.sendMessage(mesajlar.al("menu.ayarlar.yetkisiz"));
                    return;
                }
                oyuncu.closeInventory();
                oyuncu.sendMessage(mesajlar.al("menu.girdi.aciklama-iste"));
                eklenti.getGirdiYoneticisi().girdiBekle(oyuncu.getUniqueId(), metin -> aciklamaIsle(metin));
            }
            case 16 -> {
                if (!izinVarMi("SIL")) return;
                if (!yetkiVarMi(Rutbe.LIDER)) {
                    oyuncu.sendMessage(mesajlar.al("menu.ayarlar.yetkisiz"));
                    return;
                }
                oyuncu.closeInventory();
                String klanIsmi = klan.getIsim();
                String izinDugumu = eklenti.getKomutAyarlari().izinDugumu("SIL");
                oyuncu.sendMessage(mesajlar.al("sil.onay-gerekli"));
                var onayButon = com.klaneklentisi.klan.util.Butonlar.buton(
                        mesajlar.hamMetin("sil.onay-buton"),
                        net.kyori.adventure.text.format.NamedTextColor.RED,
                        mesajlar.hamMetin("sil.onay-ipucu"),
                        p -> {
                            if (!p.hasPermission(izinDugumu)) return;
                            var guncelKlan = yonetici.klanBul(p.getUniqueId());
                            if (guncelKlan.isPresent() && guncelKlan.get().getIsim().equalsIgnoreCase(klanIsmi)
                                    && guncelKlan.get().getRutbe(p.getUniqueId()) == com.klaneklentisi.klan.model.Rutbe.LIDER) {
                                yonetici.klanSil(klanIsmi);
                                p.sendMessage(mesajlar.al("sil.basarili"));
                            }
                        });
                oyuncu.sendMessage(onayButon);
            }
            case 31 -> new AnaMenu(eklenti, oyuncu).ac();
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
        if (!yonetici.etiketGecerliMi(metin)) {
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
        klan.setAciklama(yonetici.aciklamaTemizle(metin));
        yonetici.kaydet(klan);
        oyuncu.sendMessage(mesajlar.al("aciklama.basarili"));
    }

}

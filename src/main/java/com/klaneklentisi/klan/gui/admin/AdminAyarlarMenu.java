package com.klaneklentisi.klan.gui.admin;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.gui.Esya;
import com.klaneklentisi.klan.gui.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Map;

public class AdminAyarlarMenu extends Menu {

    public AdminAyarlarMenu(KlanEklentisi eklenti, Player oyuncu) {
        super(eklenti, oyuncu);
    }

    @Override
    protected int boyut() {
        return 36;
    }

    @Override
    protected String baslik() {
        return mesajlar.baslik("admin.ayarlar.baslik");
    }

    @Override
    protected void doldur() {
        for (int i = 0; i < boyut(); i++) {
            envanter.setItem(i, Esya.doldurucu());
        }

        boolean tekKlan = eklenti.getConfig().getBoolean("genel.tek-klan-siniri", true);
        boolean usAktif = eklenti.getConfig().getBoolean("us-sistemi.aktif", true);
        int usBekleme = eklenti.getConfig().getInt("us-sistemi.bekleme-suresi", 3);
        int minIsim = eklenti.getConfig().getInt("genel.min-isim-uzunlugu", 3);
        int maksIsim = eklenti.getConfig().getInt("genel.maks-isim-uzunlugu", 16);

        envanter.setItem(10, Esya.olustur(Material.IRON_BARS,
                mesajlar.baslik("admin.ayarlar.tek-klan-siniri", Map.of("deger", tekKlan ? "Açık" : "Kapalı")),
                List.of(mesajlar.baslik("admin.ayarlar.tek-klan-siniri-aciklama"))));

        envanter.setItem(12, Esya.olustur(Material.RED_BED,
                mesajlar.baslik("admin.ayarlar.us-aktif", Map.of("deger", usAktif ? "Açık" : "Kapalı")),
                List.of(mesajlar.baslik("admin.ayarlar.us-aktif-aciklama"))));

        envanter.setItem(14, Esya.olustur(Material.CLOCK,
                mesajlar.baslik("admin.ayarlar.us-bekleme", Map.of("deger", String.valueOf(usBekleme))),
                List.of(mesajlar.baslik("admin.ayarlar.us-bekleme-aciklama"))));

        envanter.setItem(19, Esya.olustur(Material.NAME_TAG,
                mesajlar.baslik("admin.ayarlar.min-isim", Map.of("deger", String.valueOf(minIsim))),
                List.of(mesajlar.baslik("admin.ayarlar.uzunluk-aciklama"))));

        envanter.setItem(21, Esya.olustur(Material.NAME_TAG,
                mesajlar.baslik("admin.ayarlar.maks-isim", Map.of("deger", String.valueOf(maksIsim))),
                List.of(mesajlar.baslik("admin.ayarlar.uzunluk-aciklama"))));

        envanter.setItem(31, Esya.olustur(Material.ARROW, mesajlar.baslik("admin.ayarlar.geri")));
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        switch (olay.getSlot()) {
            case 10 -> {
                boolean mevcut = eklenti.getConfig().getBoolean("genel.tek-klan-siniri", true);
                eklenti.getConfig().set("genel.tek-klan-siniri", !mevcut);
                eklenti.saveConfig();
                yenile();
            }
            case 12 -> {
                boolean mevcut = eklenti.getConfig().getBoolean("us-sistemi.aktif", true);
                eklenti.getConfig().set("us-sistemi.aktif", !mevcut);
                eklenti.saveConfig();
                yenile();
            }
            case 14 -> sayiIste("us-sistemi.bekleme-suresi", 0, 300);
            case 19 -> sayiIste("genel.min-isim-uzunlugu", 1, 32);
            case 21 -> sayiIste("genel.maks-isim-uzunlugu", 1, 32);
            case 31 -> new AdminAnaMenu(eklenti, oyuncu).ac();
            default -> {}
        }
    }

    private void sayiIste(String configYolu, int min, int maks) {
        oyuncu.closeInventory();
        oyuncu.sendMessage(mesajlar.al("admin.girdi.sayi-iste"));
        eklenti.getGirdiYoneticisi().girdiBekle(oyuncu.getUniqueId(), metin -> {
            if (metin.equalsIgnoreCase("iptal")) {
                oyuncu.sendMessage(mesajlar.al("menu.girdi.iptal-edildi"));
                return;
            }
            try {
                int deger = Integer.parseInt(metin.trim());
                if (deger < min || deger > maks) {
                    oyuncu.sendMessage(mesajlar.al("admin.girdi.gecersiz-sayi"));
                    return;
                }
                eklenti.getConfig().set(configYolu, deger);
                eklenti.saveConfig();
                oyuncu.sendMessage(mesajlar.al("admin.girdi.guncellendi", Map.of("deger", String.valueOf(deger))));
            } catch (NumberFormatException e) {
                oyuncu.sendMessage(mesajlar.al("admin.girdi.gecersiz-sayi"));
            }
        });
    }
}

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
        return 54;
    }

    @Override
    protected String baslik() {
        return klan == null
                ? mesajlar.baslik("menu.ana.klanin-yok-baslik")
                : mesajlar.baslik("menu.ana.baslik");
    }

    private void dolduruCam(int... slotlar) {
        for (int s : slotlar) {
            envanter.setItem(s, Esya.olustur(Material.BLACK_STAINED_GLASS_PANE, " "));
        }
    }

    @Override
    protected void doldur() {
        kenarCiz();

        if (klan == null) {
            dolduruCam(10, 11, 12, 14, 15, 16, 19, 20, 24, 25, 28, 29, 30, 32, 33, 34);
            Optional<Klan> davetOpt = yonetici.davetiGetir(oyuncu.getUniqueId());
            if (davetOpt.isPresent()) {
                envanter.setItem(21, Esya.olustur(Material.WRITTEN_BOOK,
                        mesajlar.baslik("menu.ana.davet-var"),
                        mesajlar.alListe("menu.ana.davet-aciklama", Map.of("klan", davetOpt.get().getIsim()))));
            } else {
                envanter.setItem(21, Esya.olustur(Material.GOLDEN_SWORD, mesajlar.baslik("menu.ana.liderlik")));
            }
            envanter.setItem(13, Esya.olustur(Material.OAK_SAPLING,
                    mesajlar.baslik("menu.ana.olustur"), mesajlar.alListe("menu.ana.olustur-aciklama")));
            envanter.setItem(31, Esya.olustur(Material.BARRIER, mesajlar.baslik("menu.ana.kapat")));
            return;
        }

        // Dağınık/gruplu düzen: her satırda tek başına bir ikon + küçük bir küme,
        // aralarında nefes alan boşluklar bırakılır (sıkışık tek blok yerine).
        dolduruCam(11, 12, 14, 15, 21, 23, 29, 32);

        // --- 1. iç satır: Bilgi (yalnız) — Klan Sembolü (odak) — Üyeler (yalnız) ---
        envanter.setItem(10, Esya.olustur(Material.BOOK, mesajlar.baslik("menu.ana.bilgi")));
        envanter.setItem(13, klanSembolEsyasi());
        envanter.setItem(16, Esya.olustur(Material.PLAYER_HEAD, mesajlar.baslik("menu.ana.uyeler")));

        // --- 2. iç satır: Ayarlar + Üs (küme) — Kendi İstatistiğin (odak) — Sohbet + Klan Listesi (küme) ---
        envanter.setItem(19, Esya.olustur(Material.ANVIL, mesajlar.baslik("menu.ana.ayarlar")));
        envanter.setItem(20, Esya.olustur(Material.COMPASS, mesajlar.baslik("menu.ana.us")));
        envanter.setItem(22, kendiIstatistigimEsyasi());
        boolean sohbetAcik = yonetici.sohbetModuAcikMi(oyuncu.getUniqueId());
        envanter.setItem(24, Esya.olustur(Material.WRITABLE_BOOK,
                mesajlar.baslik(sohbetAcik ? "menu.ana.sohbet-ac" : "menu.ana.sohbet-kapali")));
        envanter.setItem(25, Esya.olustur(Material.MAP, mesajlar.baslik("menu.ana.klan-listesi")));

        // --- 3. iç satır: Müttefikler (yalnız) — Liderlik + Kapat (küme, orta) — Davet/Rakipler (yalnız) ---
        envanter.setItem(28, Esya.olustur(Material.LIME_BANNER, mesajlar.baslik("menu.ana.muttefikler")));
        envanter.setItem(30, Esya.olustur(Material.GOLDEN_SWORD, mesajlar.baslik("menu.ana.liderlik")));
        envanter.setItem(31, Esya.olustur(Material.BARRIER, mesajlar.baslik("menu.ana.kapat")));

        Optional<Klan> davetOpt = yonetici.davetiGetir(oyuncu.getUniqueId());
        if (davetOpt.isPresent()) {
            envanter.setItem(33, Esya.olustur(Material.WRITTEN_BOOK,
                    mesajlar.baslik("menu.ana.davet-var"),
                    mesajlar.alListe("menu.ana.davet-aciklama", Map.of("klan", davetOpt.get().getIsim()))));
        } else {
            dolduruCam(33);
        }
        envanter.setItem(34, Esya.olustur(Material.RED_BANNER, mesajlar.baslik("menu.ana.rakipler")));
    }

    /** Klanın kendi seçtiği sembolü varsa onu, yoksa nötr bir sandık ikonunu döner. */
    private org.bukkit.inventory.ItemStack klanSembolEsyasi() {
        List<String> lore = List.of(
                com.klaneklentisi.klan.util.Mesajlar.renkli("&7[&f" + klan.getEtiket() + "&7]"),
                com.klaneklentisi.klan.util.Mesajlar.renkli("&7Üye: &f" + klan.getUyeSayisi()));
        if (klan.getSembol() != null) {
            var esya = klan.getSembol().clone();
            esya.setAmount(1);
            var meta = esya.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(com.klaneklentisi.klan.util.Mesajlar.renkli("&6★ " + klan.getIsim()));
                meta.setLore(lore);
                esya.setItemMeta(meta);
            }
            return esya;
        }
        return Esya.olustur(Material.CHEST, "&6★ " + klan.getIsim(), lore);
    }

    private org.bukkit.inventory.ItemStack kendiIstatistigimEsyasi() {
        var ist = eklenti.getIstatistikYoneticisi().getIstatistik(oyuncu.getUniqueId());
        return Esya.oyuncuKafasi(oyuncu, "&e★ İstatistiklerim", List.of(
                com.klaneklentisi.klan.util.Mesajlar.renkli("&a▪ Öldürme: &f" + ist.getOldurme()),
                com.klaneklentisi.klan.util.Mesajlar.renkli("&c▪ Ölme: &f" + ist.getOlme()),
                com.klaneklentisi.klan.util.Mesajlar.renkli("&7▪ K/D: &f" + ist.getOran())));
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        int slot = olay.getSlot();

        if (klan == null) {
            if (slot == 21 && yonetici.davetiGetir(oyuncu.getUniqueId()).isPresent()) {
                new DavetMenu(eklenti, oyuncu, yonetici.davetiGetir(oyuncu.getUniqueId()).get()).ac();
            } else if (slot == 21) {
                new LiderlikMenu(eklenti, oyuncu, 0).ac();
            } else if (slot == 31) {
                oyuncu.closeInventory();
            }
            return;
        }

        if (slot == 33 && yonetici.davetiGetir(oyuncu.getUniqueId()).isPresent()) {
            new DavetMenu(eklenti, oyuncu, yonetici.davetiGetir(oyuncu.getUniqueId()).get()).ac();
            return;
        }

        switch (slot) {
            case 10, 13 -> {
                if (!izinVarMi("BILGI")) return;
                oyuncu.closeInventory();
                eklenti.getKlanKomutu().bilgiGoster(oyuncu, klan);
            }
            case 16 -> new UyelerMenu(eklenti, oyuncu, klan, 0).ac();
            case 19 -> new AyarlarMenu(eklenti, oyuncu, klan).ac();
            case 20 -> {
                if (!izinVarMi("US")) return;
                oyuncu.closeInventory();
                eklenti.getKlanKomutu().usaIsinlan(oyuncu);
            }
            case 24 -> {
                if (!izinVarMi("SOHBET")) return;
                yonetici.sohbetModunuDegistir(oyuncu.getUniqueId());
                yenile();
            }
            case 25 -> new KlanListesiMenu(eklenti, oyuncu, 0).ac();
            case 28 -> new MuttefikRakipMenu(eklenti, oyuncu, klan, true).ac();
            case 30 -> new LiderlikMenu(eklenti, oyuncu, 0).ac();
            case 31 -> oyuncu.closeInventory();
            case 34 -> new MuttefikRakipMenu(eklenti, oyuncu, klan, false).ac();
            default -> {}
        }
    }
}

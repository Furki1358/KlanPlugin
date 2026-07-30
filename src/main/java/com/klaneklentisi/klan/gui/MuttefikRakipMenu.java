package com.klaneklentisi.klan.gui;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.model.Klan;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MuttefikRakipMenu extends Menu {

    private final Klan klan;
    private final boolean muttefik; // true = müttefikler, false = rakipler
    private final List<String> liste;

    public MuttefikRakipMenu(KlanEklentisi eklenti, Player oyuncu, Klan klan, boolean muttefik) {
        super(eklenti, oyuncu);
        this.klan = klan;
        this.muttefik = muttefik;
        this.liste = new ArrayList<>(muttefik ? klan.getMuttefikler() : klan.getRakipler());
    }

    @Override
    protected int boyut() {
        return 54;
    }

    @Override
    protected String baslik() {
        return mesajlar.baslik(muttefik ? "menu.muttefik-rakip.baslik-muttefik" : "menu.muttefik-rakip.baslik-rakip");
    }

    @Override
    protected void doldur() {
        for (int i = 0; i < boyut(); i++) {
            envanter.setItem(i, Esya.doldurucu());
        }

        Material esyaTuru = muttefik ? Material.LIME_BANNER : Material.RED_BANNER;
        for (int i = 0; i < liste.size() && i < 45; i++) {
            envanter.setItem(i, Esya.olustur(esyaTuru, "&f" + liste.get(i),
                    List.of(mesajlar.baslik("menu.muttefik-rakip.cikar-aciklama"))));
        }

        envanter.setItem(49, Esya.olustur(Material.EMERALD, mesajlar.baslik("menu.muttefik-rakip.ekle")));
        envanter.setItem(45, Esya.olustur(Material.ARROW, mesajlar.baslik("menu.muttefik-rakip.geri")));
    }

    @Override
    public void tikla(InventoryClickEvent olay) {
        int slot = olay.getSlot();

        if (slot == 45) {
            new AnaMenu(eklenti, oyuncu).ac();
            return;
        }
        if (slot == 49) {
            new KlanSecimMenu(eklenti, oyuncu, klan, muttefik, 0).ac();
            return;
        }
        if (slot < 45 && slot < liste.size()) {
            if (!izinVarMi(muttefik ? "MUTTEFIK" : "RAKIP")) return;
            String hedefIsim = liste.get(slot);
            yonetici.klanBul(hedefIsim).ifPresent(hedef -> {
                if (muttefik) {
                    yonetici.muttefikCikar(klan, hedef);
                    oyuncu.sendMessage(mesajlar.al("muttefik.cikarildi", Map.of("klan", hedef.getIsim())));
                } else {
                    yonetici.rakipCikar(klan, hedef);
                    oyuncu.sendMessage(mesajlar.al("rakip.cikarildi", Map.of("klan", hedef.getIsim())));
                }
            });
            new MuttefikRakipMenu(eklenti, oyuncu, klan, muttefik).ac();
        }
    }
}

package com.klaneklentisi.klan.gui;

import com.klaneklentisi.klan.util.Mesajlar;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

/**
 * GUI menülerinde kullanılan ItemStack'leri kolayca oluşturmak için yardımcı sınıf.
 */
public final class Esya {

    private Esya() {}

    public static ItemStack olustur(Material tur, String isim, List<String> lore) {
        ItemStack esya = new ItemStack(tur);
        ItemMeta meta = esya.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Mesajlar.renkli(isim));
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            esya.setItemMeta(meta);
        }
        return esya;
    }

    public static ItemStack olustur(Material tur, String isim) {
        return olustur(tur, isim, null);
    }

    public static ItemStack oyuncuKafasi(OfflinePlayer oyuncu, String isim, List<String> lore) {
        ItemStack esya = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) esya.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(oyuncu);
            meta.setDisplayName(Mesajlar.renkli(isim));
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            esya.setItemMeta(meta);
        }
        return esya;
    }

    public static ItemStack doldurucu() {
        return olustur(Material.BLACK_STAINED_GLASS_PANE, " ");
    }
}

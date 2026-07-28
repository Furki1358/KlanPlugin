package com.klaneklentisi.klan.gui;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.manager.KlanYoneticisi;
import com.klaneklentisi.klan.util.Mesajlar;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Tüm GUI menüleri bu sınıfı miras alır.
 * doldur() envanterin içeriğini oluşturur, tikla() bir slota tıklandığında çağrılır.
 */
public abstract class Menu {

    protected final KlanEklentisi eklenti;
    protected final KlanYoneticisi yonetici;
    protected final Mesajlar mesajlar;
    protected final Player oyuncu;
    protected final MenuTutucu tutucu;
    protected Inventory envanter;

    protected Menu(KlanEklentisi eklenti, Player oyuncu) {
        this.eklenti = eklenti;
        this.yonetici = eklenti.getKlanYoneticisi();
        this.mesajlar = eklenti.getMesajlar();
        this.oyuncu = oyuncu;
        this.tutucu = new MenuTutucu(this);
    }

    /** Envanter boyutu (9'un katları, 9-54 arası). */
    protected abstract int boyut();

    /** GUI başlığı. */
    protected abstract String baslik();

    /** Envanter içeriğini doldurur. */
    protected abstract void doldur();

    /** Bir slota tıklandığında çağrılır. */
    public abstract void tikla(InventoryClickEvent olay);

    /** Menüyü oluşturup oyuncuya açar. */
    public void ac() {
        this.envanter = eklenti.getServer().createInventory(tutucu, boyut(), Mesajlar.renkli(baslik()));
        tutucu.envanterAta(envanter);
        doldur();
        oyuncu.openInventory(envanter);
    }

    /** Envanteri yeniden doldurmak için (işlem sonrası anlık güncelleme). */
    protected void yenile() {
        envanter.clear();
        doldur();
    }
}

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

    /**
     * Bazı özel menüler (örn. klan sembolü seçme) belirli slotlarda normal eşya
     * yerleştirme/alma işlemine izin vermek isteyebilir. Varsayılan olarak hiçbir
     * slotta buna izin verilmez (tüm tıklamalar iptal edilir).
     */
    public boolean izinliSlotMu(int slot) {
        return false;
    }

    /** Envanter kapatıldığında çağrılır (örn. sembol menüsünde son durumu kaydetmek için). */
    public void kapandi(org.bukkit.event.inventory.InventoryCloseEvent olay) {
        // varsayılan: hiçbir şey yapma
    }

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

    /**
     * GUI'den yapılan bir aksiyonun ilgili komutla aynı Bukkit iznine sahip olup
     * olmadığını kontrol eder (örn. SIL, ETIKET, TERFI...). İzin yoksa mesaj gönderir
     * ve false döner. Bu olmadan GUI, LuckPerms ile kısıtlanmış komutları atlatabilirdi.
     */
    protected boolean izinVarMi(String komutId) {
        String izinDugumu = eklenti.getKomutAyarlari().izinDugumu(komutId);
        if (!oyuncu.hasPermission(izinDugumu)) {
            oyuncu.sendMessage(mesajlar.al("genel.izin-yok"));
            return false;
        }
        return true;
    }

    /**
     * Envanterin tüm kenarlarını (üst/alt satır + sağ/sol sütun) siyah-altın desenli
     * cam panelle çerçeveler ("Kraliyet Altını" teması), iç kısmı boş bırakır.
     * Geniş/ferah, simetrik bir görünüm için her menü doldur()'da önce bunu çağırıp
     * sonra ikonlarını iç kısma yerleştirir.
     */
    protected void kenarCiz() {
        int boyut = boyut();
        int satirSayisi = boyut / 9;
        var altin = Esya.olustur(org.bukkit.Material.YELLOW_STAINED_GLASS_PANE, " ");
        var siyah = Esya.olustur(org.bukkit.Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int s = 0; s < boyut; s++) {
            int satir = s / 9;
            int sutun = s % 9;
            boolean kenar = satir == 0 || satir == satirSayisi - 1 || sutun == 0 || sutun == 8;
            if (kenar) {
                boolean altinSirasi = (satir + sutun) % 2 == 0;
                envanter.setItem(s, altinSirasi ? altin.clone() : siyah.clone());
            }
        }
    }
}

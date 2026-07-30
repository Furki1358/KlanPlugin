package com.klaneklentisi.klan.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * /klan alt komutlarının isimlerini komutlar.yml dosyasından okur.
 * Böylece sunucu sahibi "terfi" gibi bir komutun ismini kendi isteğine göre
 * değiştirebilir; kod içindeki mantık her zaman sabit bir ID (örn. "TERFI") kullanır.
 */
public class KomutAyarlari {

    private static final Locale TR = Locale.forLanguageTag("tr");

    private final Plugin eklenti;
    /** girilen etiket (küçük harf) -> sabit ID */
    private final Map<String, String> etiketToId = new LinkedHashMap<>();
    /** ID -> etiket listesi (ilki birincil/gösterilen isim) */
    private final Map<String, List<String>> idToEtiketler = new LinkedHashMap<>();
    private final Map<String, String> idToKullanim = new LinkedHashMap<>();
    private final Map<String, String> idToAciklama = new LinkedHashMap<>();
    private final Map<String, String> idToIzin = new LinkedHashMap<>();
    /** Sıralı ID listesi (yardım ekranında dosyadaki sıra korunur) */
    private final List<String> idSirasi = new ArrayList<>();

    public KomutAyarlari(Plugin eklenti) {
        this.eklenti = eklenti;
        yukle();
    }

    public void yukle() {
        File dosya = new File(eklenti.getDataFolder(), "komutlar.yml");
        if (!dosya.exists()) {
            eklenti.saveResource("komutlar.yml", false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dosya);

        etiketToId.clear();
        idToEtiketler.clear();
        idToKullanim.clear();
        idToAciklama.clear();
        idToIzin.clear();
        idSirasi.clear();

        if (!yaml.isConfigurationSection("komutlar")) return;

        for (String id : yaml.getConfigurationSection("komutlar").getKeys(false)) {
            List<String> etiketler = yaml.getStringList("komutlar." + id + ".etiketler");
            if (etiketler.isEmpty()) continue;

            idSirasi.add(id);
            idToEtiketler.put(id, etiketler);
            idToKullanim.put(id, yaml.getString("komutlar." + id + ".kullanim", ""));
            idToAciklama.put(id, yaml.getString("komutlar." + id + ".aciklama", ""));
            idToIzin.put(id, yaml.getString("komutlar." + id + ".izin", "klan.kullan"));

            for (String etiket : etiketler) {
                etiketToId.put(etiket.toLowerCase(TR), id);
            }
        }
    }

    /** Girilen alt komut metnini (örn. "terfi") sabit ID'ye (örn. "TERFI") çevirir. Bulunamazsa null. */
    public String idBul(String girilenEtiket) {
        if (girilenEtiket == null) return null;
        return etiketToId.get(girilenEtiket.toLowerCase(TR));
    }

    /** Bir ID'nin birincil (gösterilen) etiketini döner. */
    public String birincilEtiket(String id) {
        List<String> etiketler = idToEtiketler.get(id);
        return (etiketler == null || etiketler.isEmpty()) ? id.toLowerCase(TR) : etiketler.get(0);
    }

    public String kullanim(String id) {
        return idToKullanim.getOrDefault(id, "");
    }

    public String aciklama(String id) {
        return idToAciklama.getOrDefault(id, "");
    }

    /** Bir ID'nin Bukkit izin düğümünü döner (LuckPerms vb. ile kontrol edilir). */
    public String izinDugumu(String id) {
        return idToIzin.getOrDefault(id, "klan.kullan");
    }

    /** Tab-tamamlama için tüm komutların birincil etiketlerini döner. */
    public List<String> tumBirincilEtiketler() {
        List<String> sonuc = new ArrayList<>();
        for (String id : idSirasi) {
            sonuc.add(birincilEtiket(id));
        }
        return sonuc;
    }

    /** /klan yardim ekranı için "/klan {etiket} {kullanim} - {açıklama}" biçiminde satırlar üretir. */
    public List<String> yardimSatirlariOlustur() {
        List<String> satirlar = new ArrayList<>();
        for (String id : idSirasi) {
            if (id.equals("YARDIM")) continue; // yardımın kendisini listede göstermeye gerek yok
            String etiket = birincilEtiket(id);
            String kullanim = kullanim(id);
            String aciklama = aciklama(id);
            String satir = "&f➤ /klan " + etiket + (kullanim.isEmpty() ? "" : " " + kullanim) + " &7- " + aciklama;
            satirlar.add(satir);
        }
        return satirlar;
    }

    public List<String> idSirasi() {
        return idSirasi;
    }
}

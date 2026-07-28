package com.klaneklentisi.klan.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * lang/tr.yml dosyasından mesajları okuyup {yer-tutucu} değişimlerini yapan yardımcı sınıf.
 * Sunucu sahibi bu dosyayı doğrudan düzenleyerek tüm metinleri değiştirebilir.
 */
public class Mesajlar {

    private static final Pattern HEX_DESENI = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private final Plugin eklenti;
    private YamlConfiguration yaml;
    private String onEk;

    public Mesajlar(Plugin eklenti) {
        this.eklenti = eklenti;
        yukle();
    }

    public void yukle() {
        File dilKlasoru = new File(eklenti.getDataFolder(), "lang");
        File dosya = new File(dilKlasoru, "tr.yml");

        if (!dosya.exists()) {
            eklenti.saveResource("lang/tr.yml", false);
        }

        yaml = YamlConfiguration.loadConfiguration(dosya);

        // Jar içindeki varsayılan değerleri "default" olarak ekle: eksik anahtar varsa oradan tamamlanır.
        try (InputStream varsayilanAkis = eklenti.getResource("lang/tr.yml")) {
            if (varsayilanAkis != null) {
                YamlConfiguration varsayilan = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(varsayilanAkis, StandardCharsets.UTF_8));
                yaml.setDefaults(varsayilan);
                yaml.options().copyDefaults(true);
                yaml.save(dosya);
            }
        } catch (IOException e) {
            eklenti.getLogger().warning("lang/tr.yml güncellenirken hata: " + e.getMessage());
        }

        this.onEk = renkli(yaml.getString("on-ek", "&8[&6Klan&8] &r"));
    }

    public String getOnEk() {
        return onEk;
    }

    /** Ham mesajı (renksiz, yer tutucusuz) anahtarından döndürür. */
    public String hamMetin(String anahtar) {
        String metin = yaml.getString(anahtar);
        if (metin == null) {
            return "&c[Eksik mesaj: " + anahtar + "]";
        }
        return metin;
    }

    /** Anahtara karşılık gelen mesajı ön ek + renk + yer tutucularla birlikte döndürür. */
    public String al(String anahtar, Map<String, String> yerTutucular) {
        String metin = hamMetin(anahtar);
        if (yerTutucular != null) {
            for (Map.Entry<String, String> girdi : yerTutucular.entrySet()) {
                metin = metin.replace("{" + girdi.getKey() + "}", girdi.getValue());
            }
        }
        return renkli(onEk + metin);
    }

    public String al(String anahtar) {
        return al(anahtar, null);
    }

    /** Ön ek eklemeden, sadece renklendirilmiş mesaj (uzun blok mesajlar için). */
    public String alOnEksiz(String anahtar, Map<String, String> yerTutucular) {
        String metin = hamMetin(anahtar);
        if (yerTutucular != null) {
            for (Map.Entry<String, String> girdi : yerTutucular.entrySet()) {
                metin = metin.replace("{" + girdi.getKey() + "}", girdi.getValue());
            }
        }
        return renkli(metin);
    }

    /** Bir liste anahtarını (örn. GUI item lore'u) yer tutucularla birlikte renklendirilmiş şekilde döndürür. */
    public java.util.List<String> alListe(String anahtar, Map<String, String> yerTutucular) {
        java.util.List<String> ham = yaml.getStringList(anahtar);
        java.util.List<String> sonuc = new java.util.ArrayList<>();
        for (String satir : ham) {
            String metin = satir;
            if (yerTutucular != null) {
                for (Map.Entry<String, String> girdi : yerTutucular.entrySet()) {
                    metin = metin.replace("{" + girdi.getKey() + "}", girdi.getValue());
                }
            }
            sonuc.add(renkli(metin));
        }
        return sonuc;
    }

    public java.util.List<String> alListe(String anahtar) {
        return alListe(anahtar, null);
    }

    /** Ön eksiz, tek satır mesaj + yer tutucu (GUI başlıkları için; başlıklarda ön ek istemeyiz). */
    public String baslik(String anahtar, Map<String, String> yerTutucular) {
        return alOnEksiz(anahtar, yerTutucular);
    }

    public String baslik(String anahtar) {
        return baslik(anahtar, null);
    }

    /** & renk kodlarını ve &#RRGGBB hex kodlarını işler. */
    public static String renkli(String metin) {
        if (metin == null) return "";
        Matcher eslesme = HEX_DESENI.matcher(metin);
        StringBuilder tampon = new StringBuilder();
        while (eslesme.find()) {
            String hex = eslesme.group(1);
            eslesme.appendReplacement(tampon, ChatColor.of("#" + hex).toString());
        }
        eslesme.appendTail(tampon);
        return ChatColor.translateAlternateColorCodes('&', tampon.toString());
    }
}

package com.klaneklentisi.klan.storage;

import com.klaneklentisi.klan.model.KatilimTuru;
import com.klaneklentisi.klan.model.Klan;
import com.klaneklentisi.klan.model.Rutbe;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class YamlKlanDeposu implements KlanDeposu {

    private final Plugin eklenti;
    private final File klasor;

    public YamlKlanDeposu(Plugin eklenti) {
        this.eklenti = eklenti;
        this.klasor = new File(eklenti.getDataFolder(), "klanlar");
    }

    @Override
    public void baslat() {
        if (!klasor.exists() && !klasor.mkdirs()) {
            eklenti.getLogger().warning("klanlar/ klasörü oluşturulamadı: " + klasor.getAbsolutePath());
        }
    }

    @Override
    public void kapat() {
        // YAML için özel bir kapatma işlemi gerekmiyor.
    }

    private File dosyaYolu(String klanIsmi) {
        return new File(klasor, klanIsmi.toLowerCase(java.util.Locale.forLanguageTag("tr")) + ".yml");
    }

    @Override
    public List<Klan> tumunuYukle() {
        List<Klan> sonuc = new ArrayList<>();
        File[] dosyalar = klasor.listFiles((dir, ad) -> ad.toLowerCase(java.util.Locale.forLanguageTag("tr")).endsWith(".yml"));
        if (dosyalar == null) return sonuc;

        for (File dosya : dosyalar) {
            try {
                Klan klan = dosyadanOku(dosya);
                if (klan != null) {
                    sonuc.add(klan);
                }
            } catch (Exception e) {
                eklenti.getLogger().log(Level.WARNING, "Klan dosyası okunamadı: " + dosya.getName(), e);
            }
        }
        return sonuc;
    }

    private Klan dosyadanOku(File dosya) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dosya);

        String isim = yaml.getString("isim");
        String etiket = yaml.getString("etiket", isim);
        String kurucuStr = yaml.getString("kurucu");
        if (isim == null || kurucuStr == null) return null;

        UUID kurucu = UUID.fromString(kurucuStr);
        Klan klan = new Klan(isim, etiket, kurucu);
        klan.setAciklama(yaml.getString("aciklama", ""));
        klan.setOlusturulmaZamani(yaml.getLong("olusturulma-zamani", System.currentTimeMillis()));
        klan.setKatilimTuru(KatilimTuru.cozumle(yaml.getString("katilim-turu", "DAVETLI")));
        klan.setKasa(yaml.getDouble("kasa", 0.0));

        // Üyeler (kurucu zaten constructor'da eklendi, temizleyip yeniden dolduruyoruz)
        klan.getUyeler().clear();
        if (yaml.isConfigurationSection("uyeler")) {
            for (String uuidStr : yaml.getConfigurationSection("uyeler").getKeys(false)) {
                try {
                    UUID uid = UUID.fromString(uuidStr);
                    Rutbe rutbe = Rutbe.cozumle(yaml.getString("uyeler." + uuidStr));
                    if (rutbe != null) {
                        klan.getUyeler().put(uid, rutbe);
                    }
                } catch (IllegalArgumentException ignored) {
                    // Bozuk UUID girdisi, atla
                }
            }
        }
        if (klan.getUyeler().isEmpty()) {
            klan.getUyeler().put(kurucu, Rutbe.LIDER);
        }

        for (String m : yaml.getStringList("muttefikler")) {
            klan.getMuttefikler().add(m.toLowerCase(java.util.Locale.forLanguageTag("tr")));
        }
        for (String r : yaml.getStringList("rakipler")) {
            klan.getRakipler().add(r.toLowerCase(java.util.Locale.forLanguageTag("tr")));
        }

        if (yaml.contains("us")) {
            Object obj = yaml.get("us");
            if (obj instanceof Location loc) {
                klan.setUs(loc);
            }
        }

        return klan;
    }

    @Override
    public void kaydet(Klan klan) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("isim", klan.getIsim());
        yaml.set("etiket", klan.getEtiket());
        yaml.set("aciklama", klan.getAciklama());
        yaml.set("kurucu", klan.getKurucu().toString());
        yaml.set("olusturulma-zamani", klan.getOlusturulmaZamani());
        yaml.set("katilim-turu", klan.getKatilimTuru().name());
        yaml.set("kasa", klan.getKasa());

        for (var girdi : klan.getUyeler().entrySet()) {
            yaml.set("uyeler." + girdi.getKey().toString(), girdi.getValue().name());
        }

        yaml.set("muttefikler", new ArrayList<>(klan.getMuttefikler()));
        yaml.set("rakipler", new ArrayList<>(klan.getRakipler()));

        if (klan.getUs() != null) {
            yaml.set("us", klan.getUs());
        }

        try {
            yaml.save(dosyaYolu(klan.getIsim()));
        } catch (IOException e) {
            eklenti.getLogger().log(Level.SEVERE, "Klan kaydedilemedi: " + klan.getIsim(), e);
        }
    }

    @Override
    public void sil(String klanIsmi) {
        File dosya = dosyaYolu(klanIsmi);
        if (dosya.exists() && !dosya.delete()) {
            eklenti.getLogger().warning("Klan dosyası silinemedi: " + dosya.getName());
        }
    }
}

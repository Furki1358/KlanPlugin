package com.klaneklentisi.klan.istatistik;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class YamlIstatistikDeposu implements IstatistikDeposu {

    private final Plugin eklenti;
    private final File dosya;

    public YamlIstatistikDeposu(Plugin eklenti) {
        this.eklenti = eklenti;
        this.dosya = new File(eklenti.getDataFolder(), "istatistikler.yml");
    }

    @Override
    public Map<UUID, OyuncuIstatistik> tumunuYukle() {
        Map<UUID, OyuncuIstatistik> sonuc = new HashMap<>();
        if (!dosya.exists()) return sonuc;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dosya);
        if (!yaml.isConfigurationSection("oyuncular")) return sonuc;

        for (String uuidStr : yaml.getConfigurationSection("oyuncular").getKeys(false)) {
            try {
                UUID uid = UUID.fromString(uuidStr);
                int oldurme = yaml.getInt("oyuncular." + uuidStr + ".oldurme", 0);
                int olme = yaml.getInt("oyuncular." + uuidStr + ".olme", 0);
                sonuc.put(uid, new OyuncuIstatistik(oldurme, olme));
            } catch (IllegalArgumentException ignored) {
                // bozuk UUID, atla
            }
        }
        return sonuc;
    }

    @Override
    public void kaydet(Map<UUID, OyuncuIstatistik> istatistikler) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (var girdi : istatistikler.entrySet()) {
            String yol = "oyuncular." + girdi.getKey();
            yaml.set(yol + ".oldurme", girdi.getValue().getOldurme());
            yaml.set(yol + ".olme", girdi.getValue().getOlme());
        }
        try {
            yaml.save(dosya);
        } catch (IOException e) {
            eklenti.getLogger().log(Level.SEVERE, "İstatistikler kaydedilemedi", e);
        }
    }
}

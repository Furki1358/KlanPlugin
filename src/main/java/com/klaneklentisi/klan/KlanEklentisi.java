package com.klaneklentisi.klan;

import com.klaneklentisi.klan.command.KlanKomutu;
import com.klaneklentisi.klan.command.KlanYonetimKomutu;
import com.klaneklentisi.klan.listener.SohbetDinleyici;
import com.klaneklentisi.klan.manager.KlanYoneticisi;
import com.klaneklentisi.klan.storage.KlanDeposu;
import com.klaneklentisi.klan.storage.YamlKlanDeposu;
import com.klaneklentisi.klan.util.Mesajlar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class KlanEklentisi extends JavaPlugin {

    private KlanYoneticisi klanYoneticisi;
    private Mesajlar mesajlar;
    private KlanDeposu depo;
    private KlanKomutu klanKomutu;
    private final com.klaneklentisi.klan.gui.GirdiYoneticisi girdiYoneticisi = new com.klaneklentisi.klan.gui.GirdiYoneticisi();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.depo = new YamlKlanDeposu(this);
        this.klanYoneticisi = new KlanYoneticisi(this, depo);
        this.mesajlar = new Mesajlar(this);

        klanYoneticisi.yukle();

        this.klanKomutu = new KlanKomutu(this);
        getCommand("klan").setExecutor(klanKomutu);
        getCommand("klan").setTabCompleter(klanKomutu);

        KlanYonetimKomutu yonetimKomutu = new KlanYonetimKomutu(this);
        getCommand("klanyonetim").setExecutor(yonetimKomutu);
        getCommand("klanyonetim").setTabCompleter(yonetimKomutu);

        getServer().getPluginManager().registerEvents(new SohbetDinleyici(this, klanKomutu), this);
        getServer().getPluginManager().registerEvents(new com.klaneklentisi.klan.gui.GuiDinleyici(), this);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new com.klaneklentisi.klan.util.KlanPlaceholder(this).register();
            getLogger().info("PlaceholderAPI bulundu, klan placeholderları etkinleştirildi.");
        }

        getLogger().info("KlanEklentisi etkinleştirildi.");
    }

    @Override
    public void onDisable() {
        if (klanYoneticisi != null) {
            klanYoneticisi.tumunuKaydet();
        }
        if (depo != null) {
            depo.kapat();
        }
        getLogger().info("KlanEklentisi devre dışı bırakıldı, tüm klan verileri kaydedildi.");
    }

    public KlanYoneticisi getKlanYoneticisi() {
        return klanYoneticisi;
    }

    public Mesajlar getMesajlar() {
        return mesajlar;
    }

    public KlanKomutu getKlanKomutu() {
        return klanKomutu;
    }

    public com.klaneklentisi.klan.gui.GirdiYoneticisi getGirdiYoneticisi() {
        return girdiYoneticisi;
    }

    /** lang/tr.yml içeriğini doğrudan okumak isteyenler için (yardım metni gibi liste alanları). */
    public YamlConfiguration getLangYaml() {
        return YamlConfiguration.loadConfiguration(new File(getDataFolder(), "lang/tr.yml"));
    }
}

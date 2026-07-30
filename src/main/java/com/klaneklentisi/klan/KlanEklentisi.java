package com.klaneklentisi.klan;

import com.klaneklentisi.klan.command.KlanKomutu;
import com.klaneklentisi.klan.command.KlanYonetimKomutu;
import com.klaneklentisi.klan.istatistik.IstatistikDeposu;
import com.klaneklentisi.klan.istatistik.IstatistikYoneticisi;
import com.klaneklentisi.klan.istatistik.YamlIstatistikDeposu;
import com.klaneklentisi.klan.listener.OlumDinleyici;
import com.klaneklentisi.klan.listener.SohbetDinleyici;
import com.klaneklentisi.klan.manager.KlanYoneticisi;
import com.klaneklentisi.klan.storage.KlanDeposu;
import com.klaneklentisi.klan.storage.YamlKlanDeposu;
import com.klaneklentisi.klan.util.Mesajlar;
import com.klaneklentisi.klan.util.VaultEkonomi;
import com.klaneklentisi.klan.util.KomutAyarlari;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class KlanEklentisi extends JavaPlugin {

    private KlanYoneticisi klanYoneticisi;
    private Mesajlar mesajlar;
    private KlanDeposu depo;
    private KlanKomutu klanKomutu;
    private IstatistikYoneticisi istatistikYoneticisi;
    private IstatistikDeposu istatistikDeposu;
    private VaultEkonomi vaultEkonomi;
    private KomutAyarlari komutAyarlari;
    private com.klaneklentisi.klan.util.Loglayici loglayici;
    private final com.klaneklentisi.klan.gui.GirdiYoneticisi girdiYoneticisi = new com.klaneklentisi.klan.gui.GirdiYoneticisi(this);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.loglayici = new com.klaneklentisi.klan.util.Loglayici(this);

        this.depo = new YamlKlanDeposu(this);
        this.klanYoneticisi = new KlanYoneticisi(this, depo);
        this.mesajlar = new Mesajlar(this);

        klanYoneticisi.yukle();

        this.istatistikDeposu = new YamlIstatistikDeposu(this);
        this.istatistikYoneticisi = new IstatistikYoneticisi(this, istatistikDeposu);
        istatistikYoneticisi.yukle();
        istatistikYoneticisi.otomatikKaydiBaslat();

        this.vaultEkonomi = new VaultEkonomi(this);
        this.komutAyarlari = new KomutAyarlari(this);

        this.klanKomutu = new KlanKomutu(this);
        getCommand("klan").setExecutor(klanKomutu);
        getCommand("klan").setTabCompleter(klanKomutu);

        KlanYonetimKomutu yonetimKomutu = new KlanYonetimKomutu(this);
        getCommand("klanyonetim").setExecutor(yonetimKomutu);
        getCommand("klanyonetim").setTabCompleter(yonetimKomutu);

        getServer().getPluginManager().registerEvents(new SohbetDinleyici(this, klanKomutu), this);
        getServer().getPluginManager().registerEvents(new com.klaneklentisi.klan.gui.GuiDinleyici(this), this);
        getServer().getPluginManager().registerEvents(new OlumDinleyici(this), this);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new com.klaneklentisi.klan.util.KlanPlaceholder(this).register();
            getLogger().info("PlaceholderAPI bulundu, klan placeholderları etkinleştirildi.");
        }
        if (vaultEkonomi.aktifMi()) {
            getLogger().info("Vault bulundu, öldürme ödüllerinde para desteği etkinleştirildi.");
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
        if (istatistikYoneticisi != null) {
            istatistikYoneticisi.durdur();
            istatistikYoneticisi.kaydet();
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

    public IstatistikYoneticisi getIstatistikYoneticisi() {
        return istatistikYoneticisi;
    }

    public VaultEkonomi getVaultEkonomi() {
        return vaultEkonomi;
    }

    public KomutAyarlari getKomutAyarlari() {
        return komutAyarlari;
    }

    public com.klaneklentisi.klan.util.Loglayici getLoglayici() {
        return loglayici;
    }

    public com.klaneklentisi.klan.gui.GirdiYoneticisi getGirdiYoneticisi() {
        return girdiYoneticisi;
    }

    /** lang/tr.yml içeriğini doğrudan okumak isteyenler için (yardım metni gibi liste alanları). */
    public YamlConfiguration getLangYaml() {
        return YamlConfiguration.loadConfiguration(new File(getDataFolder(), "lang/tr.yml"));
    }
}

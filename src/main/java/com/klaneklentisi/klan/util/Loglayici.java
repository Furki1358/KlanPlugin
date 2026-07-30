package com.klaneklentisi.klan.util;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Beklenmeyen hataları plugins/KlanEklentisi/loglar/hatalar.log dosyasına yazar.
 * Böylece sunucu sahibi konsolu kaçırsa bile sorunları sonradan inceleyebilir.
 */
public class Loglayici {

    private static final DateTimeFormatter ZAMAN_FORMATI = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final Plugin eklenti;
    private final File logDosyasi;

    public Loglayici(Plugin eklenti) {
        this.eklenti = eklenti;
        File klasor = new File(eklenti.getDataFolder(), "loglar");
        if (!klasor.exists()) {
            klasor.mkdirs();
        }
        this.logDosyasi = new File(klasor, "hatalar.log");
    }

    /** Bir hatayı bağlam bilgisiyle birlikte log dosyasına yazar. Ayrıca konsola da basar. */
    public void hataKaydet(String baglam, Throwable hata) {
        eklenti.getLogger().warning("[" + baglam + "] Hata oluştu: " + hata.getMessage());

        try (PrintWriter yazici = new PrintWriter(new FileWriter(logDosyasi, true))) {
            yazici.println("==================================================");
            yazici.println("Zaman: " + LocalDateTime.now().format(ZAMAN_FORMATI));
            yazici.println("Bağlam: " + baglam);
            StringWriter sw = new StringWriter();
            hata.printStackTrace(new PrintWriter(sw));
            yazici.println(sw);
        } catch (IOException e) {
            eklenti.getLogger().warning("Log dosyasına yazılamadı: " + e.getMessage());
        }
    }
}

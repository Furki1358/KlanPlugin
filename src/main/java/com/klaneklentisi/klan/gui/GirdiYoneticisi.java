package com.klaneklentisi.klan.gui;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * GUI'den bir metin girdisi istendiğinde (örn. yeni etiket), oyuncunun bir sonraki
 * sohbet mesajını yakalayıp GUI'ye geri döndürmek için kullanılır.
 * Böylece anvil/sign gibi ek bir arayüze gerek kalmaz.
 *
 * Güvenlik notu: bekleyen istekler 60 saniye sonra otomatik iptal olur ve oyuncu
 * çıkış yaptığında hemen temizlenir. Aksi halde bir oyuncu "etiket değiştir" deyip
 * oyundan ayrılsa, günler sonra geri gelip yazdığı ilk (alakasız) sohbet mesajı
 * yanlışlıkla o eski isteğe uygulanabilirdi.
 */
public class GirdiYoneticisi {

    private static final long ZAMAN_ASIMI_TICK = 20L * 60; // 60 saniye

    private final Plugin eklenti;
    private final Map<UUID, Consumer<String>> bekleyenler = new ConcurrentHashMap<>();

    public GirdiYoneticisi(Plugin eklenti) {
        this.eklenti = eklenti;
    }

    public void girdiBekle(UUID oyuncu, Consumer<String> geriBildirim) {
        bekleyenler.put(oyuncu, geriBildirim);
        // remove(key, value) kasıtlı: süre dolduğunda SADECE hâlâ aynı istek bekliyorsa
        // temizler; oyuncu bu arada yeni bir girdi isteği başlattıysa onu etkilemez.
        Bukkit.getScheduler().runTaskLater(eklenti,
                () -> bekleyenler.remove(oyuncu, geriBildirim), ZAMAN_ASIMI_TICK);
    }

    public boolean bekleyenVarMi(UUID oyuncu) {
        return bekleyenler.containsKey(oyuncu);
    }

    /** Gelen mesajı işler ve bekleyen callback'i çalıştırır. Mesaj tüketilmiş olur (chat'e düşmez). */
    public void girdiIsle(UUID oyuncu, String mesaj) {
        Consumer<String> geriBildirim = bekleyenler.remove(oyuncu);
        if (geriBildirim != null) {
            geriBildirim.accept(mesaj);
        }
    }

    public void iptalEt(UUID oyuncu) {
        bekleyenler.remove(oyuncu);
    }
}

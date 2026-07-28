package com.klaneklentisi.klan.gui;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * GUI'den bir metin girdisi istendiğinde (örn. yeni etiket), oyuncunun bir sonraki
 * sohbet mesajını yakalayıp GUI'ye geri döndürmek için kullanılır.
 * Böylece anvil/sign gibi ek bir arayüze gerek kalmaz.
 */
public class GirdiYoneticisi {

    private final Map<UUID, Consumer<String>> bekleyenler = new ConcurrentHashMap<>();

    public void girdiBekle(UUID oyuncu, Consumer<String> geriBildirim) {
        bekleyenler.put(oyuncu, geriBildirim);
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

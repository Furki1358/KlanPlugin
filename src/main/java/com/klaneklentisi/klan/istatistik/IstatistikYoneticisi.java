package com.klaneklentisi.klan.istatistik;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class IstatistikYoneticisi {

    private final Plugin eklenti;
    private final IstatistikDeposu depo;
    private final Map<UUID, OyuncuIstatistik> istatistikler = new ConcurrentHashMap<>();

    /**
     * Performans notu: eskiden her öldürmede diske senkron yazım yapılıyordu, bu
     * yoğun PvP sunucularında ana thread'i kilitleyebilirdi. Artık sadece bu bayrak
     * işaretleniyor (bedavaya yakın) ve gerçek yazım periyodik + asenkron yapılıyor.
     */
    private final AtomicBoolean kirli = new AtomicBoolean(false);
    private org.bukkit.scheduler.BukkitTask otomatikKayitGorevi;

    public IstatistikYoneticisi(Plugin eklenti, IstatistikDeposu depo) {
        this.eklenti = eklenti;
        this.depo = depo;
    }

    public void yukle() {
        istatistikler.clear();
        istatistikler.putAll(depo.tumunuYukle());
        eklenti.getLogger().info(istatistikler.size() + " oyuncunun istatistiği yüklendi.");
    }

    /** Sunucu açılışında bir kez çağrılır: periyodik otomatik kayıt görevini başlatır. */
    public void otomatikKaydiBaslat() {
        int araliksn = Math.max(15, eklenti.getConfig().getInt("istatistik.kayit-araligi-saniye", 60));
        long araliksTick = araliksn * 20L;
        otomatikKayitGorevi = Bukkit.getScheduler().runTaskTimerAsynchronously(eklenti, () -> {
            if (kirli.compareAndSet(true, false)) {
                depo.kaydet(new HashMap<>(istatistikler));
            }
        }, araliksTick, araliksTick);
    }

    public void durdur() {
        if (otomatikKayitGorevi != null) {
            otomatikKayitGorevi.cancel();
        }
    }

    /** Anlık, senkron kayıt - sadece plugin kapanırken (onDisable) kullanılmalı. */
    public void kaydet() {
        depo.kaydet(new HashMap<>(istatistikler));
        kirli.set(false);
    }

    public OyuncuIstatistik getIstatistik(UUID oyuncu) {
        return istatistikler.getOrDefault(oyuncu, new OyuncuIstatistik());
    }

    /** Bir öldürme olayını kaydeder. Bellek içi anında güncellenir, diske yazım periyodiktir. */
    public void olumKaydet(UUID katil, UUID olen) {
        istatistikler.computeIfAbsent(katil, k -> new OyuncuIstatistik()).oldurmeEkle();
        istatistikler.computeIfAbsent(olen, k -> new OyuncuIstatistik()).olmeEkle();
        kirli.set(true);
    }

    /** En çok öldürmeye göre sıralı liste döner (isim çözümü çağıran tarafta yapılır). */
    public List<Map.Entry<UUID, OyuncuIstatistik>> siraliListe() {
        List<Map.Entry<UUID, OyuncuIstatistik>> liste = new ArrayList<>(istatistikler.entrySet());
        liste.sort(Comparator.comparingInt((Map.Entry<UUID, OyuncuIstatistik> e) -> e.getValue().getOldurme()).reversed());
        return liste;
    }
}

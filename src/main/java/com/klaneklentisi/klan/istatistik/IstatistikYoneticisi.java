package com.klaneklentisi.klan.istatistik;

import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IstatistikYoneticisi {

    private final Plugin eklenti;
    private final IstatistikDeposu depo;
    private final Map<UUID, OyuncuIstatistik> istatistikler = new ConcurrentHashMap<>();

    public IstatistikYoneticisi(Plugin eklenti, IstatistikDeposu depo) {
        this.eklenti = eklenti;
        this.depo = depo;
    }

    public void yukle() {
        istatistikler.clear();
        istatistikler.putAll(depo.tumunuYukle());
        eklenti.getLogger().info(istatistikler.size() + " oyuncunun istatistiği yüklendi.");
    }

    public void kaydet() {
        depo.kaydet(new HashMap<>(istatistikler));
    }

    public OyuncuIstatistik getIstatistik(UUID oyuncu) {
        return istatistikler.getOrDefault(oyuncu, new OyuncuIstatistik());
    }

    /** Bir öldürme olayını kaydeder. Katil ve ölen aynı kişi değilse (intihar hariç) sayaçlar güncellenir. */
    public void olumKaydet(UUID katil, UUID olen) {
        istatistikler.computeIfAbsent(katil, k -> new OyuncuIstatistik()).oldurmeEkle();
        istatistikler.computeIfAbsent(olen, k -> new OyuncuIstatistik()).olmeEkle();
        kaydet();
    }

    /** En çok öldürmeye göre sıralı liste döner (isim çözümü çağıran tarafta yapılır). */
    public List<Map.Entry<UUID, OyuncuIstatistik>> siraliListe() {
        List<Map.Entry<UUID, OyuncuIstatistik>> liste = new ArrayList<>(istatistikler.entrySet());
        liste.sort(Comparator.comparingInt((Map.Entry<UUID, OyuncuIstatistik> e) -> e.getValue().getOldurme()).reversed());
        return liste;
    }
}

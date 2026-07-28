package com.klaneklentisi.klan.model;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Bir klanın tüm verisini tutan model sınıfı.
 * Bu sınıf saf veri taşır; iş mantığı KlanYoneticisi içindedir.
 */
public class Klan {

    private final String isim;              // benzersiz, küçük harfe duyarsız kimlik olarak kullanılır
    private String etiket;                   // [TAG] - sohbette gösterilir
    private String aciklama = "";
    private UUID kurucu;
    private final Map<UUID, Rutbe> uyeler = new LinkedHashMap<>();
    private final Set<String> muttefikler = new HashSet<>();   // müttefik klan isimleri (küçük harf)
    private final Set<String> rakipler = new HashSet<>();      // rakip klan isimleri (küçük harf)
    private Location us; // klan üssü / home noktası
    private KatilimTuru katilimTuru = KatilimTuru.DAVETLI;
    private long olusturulmaZamani;
    private double kasa = 0.0; // basit klan bankası (ileride ekonomi modülü için)

    public Klan(String isim, String etiket, UUID kurucu) {
        this.isim = isim;
        this.etiket = etiket;
        this.kurucu = kurucu;
        this.olusturulmaZamani = System.currentTimeMillis();
        this.uyeler.put(kurucu, Rutbe.LIDER);
    }

    public String getIsim() {
        return isim;
    }

    public String getEtiket() {
        return etiket;
    }

    public void setEtiket(String etiket) {
        this.etiket = etiket;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public UUID getKurucu() {
        return kurucu;
    }

    public void setKurucu(UUID kurucu) {
        this.kurucu = kurucu;
    }

    public Map<UUID, Rutbe> getUyeler() {
        return uyeler;
    }

    public boolean uyeMi(UUID oyuncu) {
        return uyeler.containsKey(oyuncu);
    }

    public Rutbe getRutbe(UUID oyuncu) {
        return uyeler.get(oyuncu);
    }

    public int getUyeSayisi() {
        return uyeler.size();
    }

    public Set<String> getMuttefikler() {
        return muttefikler;
    }

    public Set<String> getRakipler() {
        return rakipler;
    }

    public Location getUs() {
        return us;
    }

    public void setUs(Location us) {
        this.us = us;
    }

    public KatilimTuru getKatilimTuru() {
        return katilimTuru;
    }

    public void setKatilimTuru(KatilimTuru katilimTuru) {
        this.katilimTuru = katilimTuru;
    }

    public long getOlusturulmaZamani() {
        return olusturulmaZamani;
    }

    public void setOlusturulmaZamani(long olusturulmaZamani) {
        this.olusturulmaZamani = olusturulmaZamani;
    }

    public double getKasa() {
        return kasa;
    }

    public void setKasa(double kasa) {
        this.kasa = kasa;
    }

    public boolean muttefikMi(String klanIsmi) {
        return muttefikler.contains(klanIsmi.toLowerCase(java.util.Locale.forLanguageTag("tr")));
    }

    public boolean rakipMi(String klanIsmi) {
        return rakipler.contains(klanIsmi.toLowerCase(java.util.Locale.forLanguageTag("tr")));
    }
}

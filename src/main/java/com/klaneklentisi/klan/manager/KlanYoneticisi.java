package com.klaneklentisi.klan.manager;

import com.klaneklentisi.klan.model.KatilimTuru;
import com.klaneklentisi.klan.model.Klan;
import com.klaneklentisi.klan.model.Rutbe;
import com.klaneklentisi.klan.storage.KlanDeposu;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class KlanYoneticisi {

    private static final Pattern GECERLI_ISIM = Pattern.compile("^[a-zA-ZçöşğüıÇÖŞĞÜİ0-9_]+$");
    private static final Locale TR = Locale.forLanguageTag("tr");

    private final Plugin eklenti;
    private final KlanDeposu depo;

    /** anahtar = klan ismi (küçük harf) */
    private final Map<String, Klan> klanlar = new HashMap<>();
    /** oyuncu UUID -> klan ismi (küçük harf), hızlı arama için cache */
    private final Map<UUID, String> oyuncuKlanCache = new HashMap<>();
    /** oyuncu UUID -> davet eden klan ismi (küçük harf) */
    private final Map<UUID, String> davetler = new HashMap<>();
    /** klan sohbet modunda olan oyuncular */
    private final Set<UUID> sohbetModunda = new HashSet<>();
    /** müttefik sohbet modunda olan oyuncular */
    private final Set<UUID> mSohbetModunda = new HashSet<>();

    public KlanYoneticisi(Plugin eklenti, KlanDeposu depo) {
        this.eklenti = eklenti;
        this.depo = depo;
    }

    public void yukle() {
        depo.baslat();
        klanlar.clear();
        oyuncuKlanCache.clear();
        for (Klan klan : depo.tumunuYukle()) {
            klanlar.put(klan.getIsim().toLowerCase(TR), klan);
            for (UUID uid : klan.getUyeler().keySet()) {
                oyuncuKlanCache.put(uid, klan.getIsim().toLowerCase(TR));
            }
        }
        eklenti.getLogger().info(klanlar.size() + " klan yüklendi.");
    }

    public void tumunuKaydet() {
        for (Klan klan : klanlar.values()) {
            depo.kaydet(klan);
        }
    }

    private FileConfiguration ayarlar() {
        return eklenti.getConfig();
    }

    // ---------------------------------------------------------------
    // Sorgulama
    // ---------------------------------------------------------------

    public Optional<Klan> klanBul(String isim) {
        if (isim == null) return Optional.empty();
        return Optional.ofNullable(klanlar.get(isim.toLowerCase(TR)));
    }

    public Optional<Klan> klanBul(UUID oyuncu) {
        String isim = oyuncuKlanCache.get(oyuncu);
        if (isim == null) return Optional.empty();
        return klanBul(isim);
    }

    public boolean klanVarMi(String isim) {
        return klanlar.containsKey(isim.toLowerCase(TR));
    }

    public Collection<Klan> tumKlanlar() {
        return klanlar.values();
    }

    // ---------------------------------------------------------------
    // Doğrulama yardımcıları
    // ---------------------------------------------------------------

    public enum Sonuc {
        BASARILI, GECERSIZ_ISIM, GECERSIZ_ETIKET, ISIM_KULLANIMDA,
        ETIKET_KULLANIMDA, ZATEN_KLANDA, KLAN_YOK, YETKISIZ, KENDISI,
        ZATEN_UYE, DAVET_YOK, KURUCU_AYRILAMAZ, SINIR_ASILDI, ZATEN_MUTTEFIK,
        ZATEN_RAKIP, MUTTEFIK_DEGIL, RAKIP_DEGIL
    }

    private boolean isimGecerliMi(String isim) {
        int min = ayarlar().getInt("genel.min-isim-uzunlugu", 3);
        int maks = ayarlar().getInt("genel.maks-isim-uzunlugu", 16);
        return isim != null && isim.length() >= min && isim.length() <= maks && GECERLI_ISIM.matcher(isim).matches();
    }

    private boolean etiketGecerliMi(String etiket) {
        int min = ayarlar().getInt("genel.min-etiket-uzunlugu", 2);
        int maks = ayarlar().getInt("genel.maks-etiket-uzunlugu", 6);
        return etiket != null && etiket.length() >= min && etiket.length() <= maks && GECERLI_ISIM.matcher(etiket).matches();
    }

    // ---------------------------------------------------------------
    // Klan oluşturma / silme
    // ---------------------------------------------------------------

    public Sonuc klanOlustur(Player kurucu, String isim, String etiket) {
        if (ayarlar().getBoolean("genel.tek-klan-siniri", true) && oyuncuKlanCache.containsKey(kurucu.getUniqueId())) {
            return Sonuc.ZATEN_KLANDA;
        }
        if (!isimGecerliMi(isim)) return Sonuc.GECERSIZ_ISIM;
        if (!etiketGecerliMi(etiket)) return Sonuc.GECERSIZ_ETIKET;
        if (klanVarMi(isim)) return Sonuc.ISIM_KULLANIMDA;

        boolean etiketDolu = klanlar.values().stream()
                .anyMatch(k -> k.getEtiket().equalsIgnoreCase(etiket));
        if (etiketDolu) return Sonuc.ETIKET_KULLANIMDA;

        Klan klan = new Klan(isim, etiket, kurucu.getUniqueId());
        klanlar.put(isim.toLowerCase(TR), klan);
        oyuncuKlanCache.put(kurucu.getUniqueId(), isim.toLowerCase(TR));
        depo.kaydet(klan);
        return Sonuc.BASARILI;
    }

    public Sonuc klanSil(String isim) {
        Optional<Klan> klanOpt = klanBul(isim);
        if (klanOpt.isEmpty()) return Sonuc.KLAN_YOK;
        Klan klan = klanOpt.get();

        for (UUID uid : klan.getUyeler().keySet()) {
            oyuncuKlanCache.remove(uid);
        }
        klanlar.remove(klan.getIsim().toLowerCase(TR));
        depo.sil(klan.getIsim());

        String kendiIsmi = klan.getIsim().toLowerCase(TR);
        for (Klan diger : klanlar.values()) {
            diger.getMuttefikler().remove(kendiIsmi);
            diger.getRakipler().remove(kendiIsmi);
        }
        return Sonuc.BASARILI;
    }

    // ---------------------------------------------------------------
    // Üyelik
    // ---------------------------------------------------------------

    public Sonuc davetGonder(Klan klan, Player davetEden, Player hedef) {
        Rutbe rutbe = klan.getRutbe(davetEden.getUniqueId());
        if (rutbe == null || rutbe.getSeviye() < Rutbe.YONETICI.getSeviye()) return Sonuc.YETKISIZ;
        if (hedef.getUniqueId().equals(davetEden.getUniqueId())) return Sonuc.KENDISI;
        if (oyuncuKlanCache.containsKey(hedef.getUniqueId())) return Sonuc.ZATEN_KLANDA;

        davetler.put(hedef.getUniqueId(), klan.getIsim().toLowerCase(TR));
        return Sonuc.BASARILI;
    }

    public Optional<Klan> davetiGetir(UUID oyuncu) {
        String isim = davetler.get(oyuncu);
        if (isim == null) return Optional.empty();
        return klanBul(isim);
    }

    public Sonuc davetKabulEt(Player oyuncu) {
        String isim = davetler.remove(oyuncu.getUniqueId());
        if (isim == null) return Sonuc.DAVET_YOK;
        if (ayarlar().getBoolean("genel.tek-klan-siniri", true) && oyuncuKlanCache.containsKey(oyuncu.getUniqueId())) {
            return Sonuc.ZATEN_KLANDA;
        }
        Klan klan = klanlar.get(isim);
        if (klan == null) return Sonuc.KLAN_YOK;

        klan.getUyeler().put(oyuncu.getUniqueId(), Rutbe.UYE);
        oyuncuKlanCache.put(oyuncu.getUniqueId(), isim);
        depo.kaydet(klan);
        return Sonuc.BASARILI;
    }

    public Sonuc davetReddet(Player oyuncu) {
        String isim = davetler.remove(oyuncu.getUniqueId());
        return isim == null ? Sonuc.DAVET_YOK : Sonuc.BASARILI;
    }

    public Sonuc klanaKatil(Klan klan, Player oyuncu) {
        if (klan.getKatilimTuru() != KatilimTuru.ACIK) return Sonuc.YETKISIZ;
        if (ayarlar().getBoolean("genel.tek-klan-siniri", true) && oyuncuKlanCache.containsKey(oyuncu.getUniqueId())) {
            return Sonuc.ZATEN_KLANDA;
        }
        klan.getUyeler().put(oyuncu.getUniqueId(), Rutbe.UYE);
        oyuncuKlanCache.put(oyuncu.getUniqueId(), klan.getIsim().toLowerCase(TR));
        depo.kaydet(klan);
        return Sonuc.BASARILI;
    }

    public Sonuc klandanAyril(Klan klan, Player oyuncu) {
        Rutbe rutbe = klan.getRutbe(oyuncu.getUniqueId());
        if (rutbe == null) return Sonuc.KLAN_YOK;
        if (rutbe == Rutbe.LIDER && klan.getUyeSayisi() > 1) return Sonuc.KURUCU_AYRILAMAZ;

        klan.getUyeler().remove(oyuncu.getUniqueId());
        oyuncuKlanCache.remove(oyuncu.getUniqueId());

        if (klan.getUyeler().isEmpty()) {
            klanSil(klan.getIsim());
        } else {
            depo.kaydet(klan);
        }
        return Sonuc.BASARILI;
    }

    public Sonuc uyeAt(Klan klan, Player yetkili, OfflinePlayer hedef) {
        Rutbe yetkiliRutbe = klan.getRutbe(yetkili.getUniqueId());
        Rutbe hedefRutbe = klan.getRutbe(hedef.getUniqueId());
        if (yetkiliRutbe == null || hedefRutbe == null) return Sonuc.KLAN_YOK;
        if (yetkiliRutbe.getSeviye() < Rutbe.YONETICI.getSeviye()) return Sonuc.YETKISIZ;
        if (hedefRutbe.getSeviye() >= yetkiliRutbe.getSeviye()) return Sonuc.YETKISIZ;

        klan.getUyeler().remove(hedef.getUniqueId());
        oyuncuKlanCache.remove(hedef.getUniqueId());
        depo.kaydet(klan);
        return Sonuc.BASARILI;
    }

    public Sonuc rutbeYukselt(Klan klan, Player yetkili, OfflinePlayer hedef) {
        Rutbe yetkiliRutbe = klan.getRutbe(yetkili.getUniqueId());
        Rutbe hedefRutbe = klan.getRutbe(hedef.getUniqueId());
        if (yetkiliRutbe == null || hedefRutbe == null) return Sonuc.KLAN_YOK;
        if (yetkiliRutbe != Rutbe.LIDER) return Sonuc.YETKISIZ;
        if (hedefRutbe == Rutbe.LIDER) return Sonuc.YETKISIZ;

        klan.getUyeler().put(hedef.getUniqueId(), hedefRutbe.birUstu());
        depo.kaydet(klan);
        return Sonuc.BASARILI;
    }

    public Sonuc rutbeIndir(Klan klan, Player yetkili, OfflinePlayer hedef) {
        Rutbe yetkiliRutbe = klan.getRutbe(yetkili.getUniqueId());
        Rutbe hedefRutbe = klan.getRutbe(hedef.getUniqueId());
        if (yetkiliRutbe == null || hedefRutbe == null) return Sonuc.KLAN_YOK;
        if (yetkiliRutbe != Rutbe.LIDER) return Sonuc.YETKISIZ;
        if (hedefRutbe == Rutbe.LIDER) return Sonuc.YETKISIZ;

        klan.getUyeler().put(hedef.getUniqueId(), hedefRutbe.birAlti());
        depo.kaydet(klan);
        return Sonuc.BASARILI;
    }

    public Sonuc liderlikDevret(Klan klan, Player mevcutLider, OfflinePlayer yeniLider) {
        Rutbe mevcutRutbe = klan.getRutbe(mevcutLider.getUniqueId());
        Rutbe yeniRutbe = klan.getRutbe(yeniLider.getUniqueId());
        if (mevcutRutbe != Rutbe.LIDER) return Sonuc.YETKISIZ;
        if (yeniRutbe == null) return Sonuc.KLAN_YOK;

        klan.getUyeler().put(mevcutLider.getUniqueId(), Rutbe.YONETICI);
        klan.getUyeler().put(yeniLider.getUniqueId(), Rutbe.LIDER);
        klan.setKurucu(yeniLider.getUniqueId());
        depo.kaydet(klan);
        return Sonuc.BASARILI;
    }

    // ---------------------------------------------------------------
    // Üs (home) sistemi
    // ---------------------------------------------------------------

    public Sonuc usAyarla(Klan klan, Player oyuncu, Location konum) {
        Rutbe rutbe = klan.getRutbe(oyuncu.getUniqueId());
        if (rutbe == null || rutbe.getSeviye() < Rutbe.YONETICI.getSeviye()) return Sonuc.YETKISIZ;
        klan.setUs(konum);
        depo.kaydet(klan);
        return Sonuc.BASARILI;
    }

    // ---------------------------------------------------------------
    // Müttefik / Rakip
    // ---------------------------------------------------------------

    public Sonuc muttefikEkle(Klan klan, Klan hedef) {
        if (klan.getIsim().equalsIgnoreCase(hedef.getIsim())) return Sonuc.KENDISI;
        if (klan.muttefikMi(hedef.getIsim())) return Sonuc.ZATEN_MUTTEFIK;

        int sinir = ayarlar().getInt("muttefik-rakip.maks-muttefik", 0);
        if (sinir > 0 && klan.getMuttefikler().size() >= sinir) return Sonuc.SINIR_ASILDI;

        klan.getRakipler().remove(hedef.getIsim().toLowerCase(TR));
        klan.getMuttefikler().add(hedef.getIsim().toLowerCase(TR));
        depo.kaydet(klan);
        return Sonuc.BASARILI;
    }

    public Sonuc muttefikCikar(Klan klan, Klan hedef) {
        if (!klan.muttefikMi(hedef.getIsim())) return Sonuc.MUTTEFIK_DEGIL;
        klan.getMuttefikler().remove(hedef.getIsim().toLowerCase(TR));
        depo.kaydet(klan);
        return Sonuc.BASARILI;
    }

    public Sonuc rakipEkle(Klan klan, Klan hedef) {
        if (klan.getIsim().equalsIgnoreCase(hedef.getIsim())) return Sonuc.KENDISI;
        if (klan.rakipMi(hedef.getIsim())) return Sonuc.ZATEN_RAKIP;

        int sinir = ayarlar().getInt("muttefik-rakip.maks-rakip", 0);
        if (sinir > 0 && klan.getRakipler().size() >= sinir) return Sonuc.SINIR_ASILDI;

        klan.getMuttefikler().remove(hedef.getIsim().toLowerCase(TR));
        klan.getRakipler().add(hedef.getIsim().toLowerCase(TR));
        depo.kaydet(klan);
        return Sonuc.BASARILI;
    }

    public Sonuc rakipCikar(Klan klan, Klan hedef) {
        if (!klan.rakipMi(hedef.getIsim())) return Sonuc.RAKIP_DEGIL;
        klan.getRakipler().remove(hedef.getIsim().toLowerCase(TR));
        depo.kaydet(klan);
        return Sonuc.BASARILI;
    }

    // ---------------------------------------------------------------
    // Klan sohbet modu
    // ---------------------------------------------------------------

    public boolean sohbetModuAcikMi(UUID oyuncu) {
        return sohbetModunda.contains(oyuncu);
    }

    public void sohbetModunuDegistir(UUID oyuncu) {
        mSohbetModunda.remove(oyuncu); // ikisi aynı anda açık olamaz
        if (!sohbetModunda.remove(oyuncu)) {
            sohbetModunda.add(oyuncu);
        }
    }

    public boolean mSohbetModuAcikMi(UUID oyuncu) {
        return mSohbetModunda.contains(oyuncu);
    }

    public void mSohbetModunuDegistir(UUID oyuncu) {
        sohbetModunda.remove(oyuncu); // ikisi aynı anda açık olamaz
        if (!mSohbetModunda.remove(oyuncu)) {
            mSohbetModunda.add(oyuncu);
        }
    }

    // ---------------------------------------------------------------
    // Admin GUI için zorla işlemler (rütbe/yetki kontrolü yapılmaz,
    // çağıran taraf klan.yonetici iznini zaten kontrol etmiş olmalı)
    // ---------------------------------------------------------------

    /** Bir üyeyi rütbesine bakılmaksızın klandan çıkarır. Lider çıkarılırsa otomatik yeni lider atanır. */
    public Sonuc zorlaCikar(Klan klan, UUID hedef) {
        Rutbe hedefRutbe = klan.getRutbe(hedef);
        if (hedefRutbe == null) return Sonuc.KLAN_YOK;

        klan.getUyeler().remove(hedef);
        oyuncuKlanCache.remove(hedef);

        if (klan.getUyeler().isEmpty()) {
            klanSil(klan.getIsim());
            return Sonuc.BASARILI;
        }

        if (hedefRutbe == Rutbe.LIDER) {
            UUID yeniLider = klan.getUyeler().entrySet().stream()
                    .max((a, b) -> Integer.compare(a.getValue().getSeviye(), b.getValue().getSeviye()))
                    .map(Map.Entry::getKey)
                    .orElse(null);
            if (yeniLider != null) {
                klan.getUyeler().put(yeniLider, Rutbe.LIDER);
                klan.setKurucu(yeniLider);
            }
        }

        depo.kaydet(klan);
        return Sonuc.BASARILI;
    }

    /** Belirtilen üyeyi rütbesine bakılmaksızın klanın yeni lideri yapar, eski lideri yönetici yapar. */
    public Sonuc zorlaLiderYap(Klan klan, UUID yeniLider) {
        if (!klan.uyeMi(yeniLider)) return Sonuc.KLAN_YOK;

        for (var girdi : klan.getUyeler().entrySet()) {
            if (girdi.getValue() == Rutbe.LIDER && !girdi.getKey().equals(yeniLider)) {
                klan.getUyeler().put(girdi.getKey(), Rutbe.YONETICI);
            }
        }
        klan.getUyeler().put(yeniLider, Rutbe.LIDER);
        klan.setKurucu(yeniLider);
        depo.kaydet(klan);
        return Sonuc.BASARILI;
    }

    /** Klanın üs konumunu sıfırlar. */
    public void zorlaUsSil(Klan klan) {
        klan.setUs(null);
        depo.kaydet(klan);
    }

    /** Katılım türünü yetki kontrolü olmadan değiştirir. */
    public void zorlaKatilimTuru(Klan klan, KatilimTuru tur) {
        klan.setKatilimTuru(tur);
        depo.kaydet(klan);
    }

    public void kaydet(Klan klan) {
        depo.kaydet(klan);
    }
}

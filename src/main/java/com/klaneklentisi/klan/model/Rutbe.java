package com.klaneklentisi.klan.model;

/**
 * Klan içindeki üye rütbelerini temsil eder.
 * Sıralama önemlidir: seviye() değeri yüksek olan daha yetkilidir.
 */
public enum Rutbe {

    UYE("Üye", 0),
    YONETICI("Yönetici", 1),
    LIDER("Lider", 2);

    private final String gorunenAd;
    private final int seviye;

    Rutbe(String gorunenAd, int seviye) {
        this.gorunenAd = gorunenAd;
        this.seviye = seviye;
    }

    public String getGorunenAd() {
        return gorunenAd;
    }

    public int getSeviye() {
        return seviye;
    }

    public boolean enAzYuksekMi(Rutbe diger) {
        return this.seviye >= diger.seviye;
    }

    public Rutbe birUstu() {
        return switch (this) {
            case UYE -> YONETICI;
            case YONETICI -> LIDER;
            case LIDER -> LIDER;
        };
    }

    public Rutbe birAlti() {
        return switch (this) {
            case UYE -> UYE;
            case YONETICI -> UYE;
            case LIDER -> YONETICI;
        };
    }

    /**
     * Metinden (config/komut girdisinden) Rutbe çözümler. Türkçe ve İngilizce isimleri kabul eder.
     */
    public static Rutbe cozumle(String metin) {
        if (metin == null) return null;
        String temiz = metin.trim().toLowerCase(java.util.Locale.forLanguageTag("tr"));
        return switch (temiz) {
            case "uye", "üye", "member" -> UYE;
            case "yonetici", "yönetici", "officer", "mod" -> YONETICI;
            case "lider", "leader", "owner", "kurucu" -> LIDER;
            default -> null;
        };
    }
}

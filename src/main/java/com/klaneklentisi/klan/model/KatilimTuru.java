package com.klaneklentisi.klan.model;

/** Klana nasıl katılınabileceğini belirler. */
public enum KatilimTuru {
    DAVETLI, // Sadece davet edilenler katılabilir (varsayılan)
    ACIK;    // Herkes /klan katil ile direkt katılabilir

    public static KatilimTuru cozumle(String metin) {
        if (metin == null) return DAVETLI;
        String temiz = metin.trim().toLowerCase(java.util.Locale.forLanguageTag("tr"));
        return switch (temiz) {
            case "acik", "açık", "open" -> ACIK;
            default -> DAVETLI;
        };
    }
}

package com.klaneklentisi.klan.istatistik;

/** Bir oyuncunun öldürme/ölme istatistiklerini tutar. */
public class OyuncuIstatistik {

    private int oldurme;
    private int olme;

    public OyuncuIstatistik() {
        this(0, 0);
    }

    public OyuncuIstatistik(int oldurme, int olme) {
        this.oldurme = oldurme;
        this.olme = olme;
    }

    public int getOldurme() {
        return oldurme;
    }

    public int getOlme() {
        return olme;
    }

    public void oldurmeEkle() {
        oldurme++;
    }

    public void olmeEkle() {
        olme++;
    }

    /** K/D oranı. Hiç ölmediyse (0 bölme hatasını önlemek için) doğrudan kill sayısı döner. */
    public double getOran() {
        if (olme == 0) return oldurme;
        return Math.round((oldurme / (double) olme) * 100.0) / 100.0;
    }
}

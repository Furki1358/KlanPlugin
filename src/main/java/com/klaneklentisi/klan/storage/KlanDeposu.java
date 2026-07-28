package com.klaneklentisi.klan.storage;

import com.klaneklentisi.klan.model.Klan;

import java.util.List;

/**
 * Klan verilerinin nasıl saklandığını soyutlar.
 * Şu an tek uygulama YamlKlanDeposu'dur; ileride SqliteKlanDeposu / MysqlKlanDeposu
 * eklenirse KlanYoneticisi hiç değişmeden bu arayüz üzerinden çalışmaya devam eder.
 */
public interface KlanDeposu {

    /** Eklenti başlarken tüm klanları diskten yükler. */
    List<Klan> tumunuYukle();

    /** Tek bir klanı kaydeder (oluşturma/güncelleme). */
    void kaydet(Klan klan);

    /** Bir klanı kalıcı depodan siler. */
    void sil(String klanIsmi);

    /** Depoyu hazırlar (dosya/tablo oluşturma vb.). */
    void baslat();

    /** Depoyu güvenli şekilde kapatır. */
    void kapat();
}

# Değişiklik Günlüğü

## [1.0.6] - Tek renk gölgeli cam çerçeve ve otomatik Release notları
### Değişti
- GUI çerçevesi siyah-altın damadan, tek renkli (siyah + koyu gri tonlama) ince gölgeli cama çevrildi. Altın vurgular metin/ikonlarda korundu.
- Her GitHub Release artık o sürümde tam olarak neyin değiştiğini bu CHANGELOG.md dosyasından otomatik çekip açıklamasına yazıyor.

## [1.0.5] - Mekanik hata düzeltmeleri
### Düzeltildi
- Klan sembolü (ikon) seçme menüsünde eşya yerleştirilemiyordu — güvenlik sıkılaştırması, kendi envanterinden eşya almayı da yanlışlıkla engelliyordu, düzeltildi.
- Müttefik/rakip seçme menüsü açıkken hedef klan silinmişse oluşabilecek tutarsızlık kapatıldı (güncel klan tekrar doğrulanıyor).

## [1.0.4] - Kraliyet Altını teması
### Değişti
- Tüm GUI'lerin çerçevesi siyah-altın (dama deseni) cam panellerle yeniden tasarlandı.
- İç boşluklar siyah cam ile dolduruldu; başlıklar ve kategori etiketleri altın renge çevrildi.
- "Yenile"/"Ekle" ikonları zümrütten altın külçeye çevrildi; sembolsüz klanlar için varsayılan ikon sandık yapıldı.
- Başarı/hata/kabul-reddet gibi anlamlı durum renkleri bilinçli olarak korundu.

## [1.0.3] - Geniş sandık ve simetrik tasarım
### Değişti
- Ana Menü, Ayarlar, Klan Bilgisi, Davet, Üye Detay ve tüm Admin menüleri 27 slottan 54 slota (çift sandık) çıkarıldı.
- Simetrik, çerçeveli, merkez odaklı yeni yerleşimler uygulandı.
- Müttefik/Rakip ikonları banner'a çevrildi; klanın kendi sembolü artık menülerde görünüyor.
- Üye detay menüsüne K/D istatistiği eklendi.

## [1.0.2] - Güvenlik ve performans denetimi
### Düzeltildi
- GUI üzerinden yapılan işlemler artık LuckPerms izinlerini de kontrol ediyor (önceden atlatılabiliyordu).
- Kendi klan/müttefik üyeni öldürerek sınırsız ödül/para farmlama kapatıldı.
- Klan/müttefik sohbetinin asenkron thread'den paylaşılan veriye güvensiz erişimi düzeltildi.
- Etiket değiştirirken karakter seti/uzunluk doğrulaması eksikliği giderildi.
- Açıklamaya renk kodu enjeksiyonu ve uzunluk sınırsızlığı kapatıldı.
- Bekleyen girdi istekleri ve klan davetleri artık zaman aşımına uğruyor (sızıntı önlendi).
- İstatistikler artık her öldürmede değil, periyodik ve asenkron kaydediliyor (performans).
- GUI'de çift-tık ile oluşabilecek ek güvenlik boşluğu kapatıldı.

## [1.0.1] - FXKlan, GUI-öncelikli tasarım ve komut özelleştirme
### Eklendi
- Eklenti adı **FXKlan** oldu.
- Komut isimleri artık `komutlar.yml`'den değiştirilebiliyor (örn. "yükselt" → "terfi").
- Klan sembolü/ikonu özelliği (GUI'den eşya seçerek klan simgesi belirleme).
- Hata/log sistemi (`plugins/FXKlan/loglar/hatalar.log`).
- GitHub Actions ile otomatik derleme + sürüm etiketli Release oluşturma.
### Değişti
- `/klan` tek başına yazılınca artık GUI açılıyor.
- Mesajlara semboller/renkler eklendi; "Klan" ön eki artık her satırda tekrarlanmıyor.

## [1.0.0] - İlk sürüm
### Eklendi
- Temel klan sistemi: oluşturma, silme, üyelik, rütbe (Üye/Yönetici/Lider), davet/kabul/reddet, ayrılma, atma.
- Klan içi sohbet, müttefik/rakip sistemi, klan üssü (home).
- Tam GUI menü sistemi (Ana Menü, Üyeler, Ayarlar, Müttefik/Rakip, Klan Listesi, Davet).
- Admin GUI ve komutları.
- KDR takibi, öldürme ödülü, liderlik tablosu.
- Müttefik sohbeti (ittifak chat).
- Tüm mesajlar ve ayarlar `config.yml` / `lang/tr.yml` üzerinden düzenlenebilir.
- PlaceholderAPI ve Vault (opsiyonel) entegrasyonu.

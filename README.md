# KlanEklentisi (v1.0.0 - MVP)

Minecraft 1.21.11 / Paper için tamamen Türkçe, sıfırdan yazılmış klan (clan) eklentisi.
Oyuncular herhangi bir mod olmadan (vanilla veya Fabric client fark etmeksizin) bağlanıp
kullanabilir — bu tamamen sunucu tarafında (server-side) çalışan bir Paper plugin'idir.

## Özellikler (MVP)
- Klan oluşturma / silme
- Üyelik: davet, kabul/reddet, açık klana direkt katılma, ayrılma, atma
- Rütbe sistemi: Üye / Yönetici / Lider (yükseltme, indirme, liderlik devretme)
- Klan içi sohbet (`/klan sohbet <mesaj>` veya toggle modu)
- Klan üssü (home): `/klan us ayarla` ve `/klan us` (bekleme süreli, hareket ederse iptal)
- Müttefik / Rakip sistemi
- Klan bilgi ekranı, klan listesi
- PlaceholderAPI desteği (varsa otomatik etkinleşir): `%klan_isim%`, `%klan_etiket%`,
  `%klan_rutbe%`, `%klan_uye_sayisi%`, `%klan_lider%`
- Tüm mesajlar `plugins/KlanEklentisi/lang/tr.yml` dosyasından **tamamen düzenlenebilir**
- Tüm genel ayarlar `plugins/KlanEklentisi/config.yml` dosyasından düzenlenebilir
- Veri depolama: YAML (her klan `plugins/KlanEklentisi/klanlar/<isim>.yml` dosyasında)

## Jar Dosyasını Elde Etme (Kod Yazmadan, Sadece Tarayıcı ile)

Bilgisayarında Java/Maven kurmana gerek yok. Bu proje `.github/workflows/build.yml`
içinde hazır bir GitHub Actions ayarıyla geliyor — GitHub'ın sunucuları senin için
otomatik derleyip jar dosyasını üretir.

**Adımlar:**

1. [github.com](https://github.com) adresine git, yoksa ücretsiz bir hesap oluştur.
2. Sağ üstteki **+** işaretine tıkla → **New repository**. İsim ver (örn. `klan-eklentisi`),
   **Public** veya **Private** seçebilirsin, **Create repository** de.
3. Açılan sayfada **"uploading an existing file"** linkine tıkla (veya *Add file* →
   *Upload files*).
4. Bu zip'i bilgisayarında aç, içindeki **tüm dosya ve klasörleri** (pom.xml, src/,
   .github/ dahil) o sayfaya sürükle-bırak.
5. Alt kısımdaki **Commit changes** butonuna bas.
6. Üst menüden **Actions** sekmesine tıkla. Birkaç saniye içinde "KlanEklentisi
   Derleme" adında bir çalışma başladığını göreceksin (sarı nokta → yeşil tik).
   Genelde 30-60 saniye sürer.
7. Çalışma bittiğinde (yeşil tik ✅) üzerine tıkla, sayfanın altında
   **Artifacts** bölümünde **KlanEklentisi-jar** adında bir indirme linki
   göreceksin. Ona tıkla, bir zip iner.
8. O zip'in içinden çıkan `KlanEklentisi-1.0.0.jar` dosyasını sunucunun
   `plugins/` klasörüne koy, sunucuyu başlat/yeniden başlat.

**Bundan sonrası için:** Sana yeni bir özellik/düzeltme gönderdiğimde, sadece o
dosyaları aynı GitHub reposuna tekrar yükleyip **Commit changes** demen yeterli —
Actions otomatik yeniden derler, yeni jar birkaç saniye içinde **Actions** sekmesinde
hazır olur. Hiç komut satırı kullanmana gerek yok.


## (İsteğe Bağlı) Kendi Bilgisayarında Manuel Derleme

Yukarıdaki GitHub Actions yöntemi yeterli; ama Java/Maven kuruluysa manuel de
derleyebilirsin:

- Java 21+
- Maven 3.9+
- Paper 1.21.11 (veya Paper 1.21.x, api-version 1.21 olduğu için genelde geriye dönük uyumludur)

## Derleme
```bash
mvn clean package
```
Derleme sonrası jar dosyası `target/KlanEklentisi-1.0.0.jar` içinde oluşur.
Bu jar'ı sunucunun `plugins/` klasörüne koyup sunucuyu başlatmanız yeterli.


## Komutlar
`/klan yardim` yazarak oyuncu içi tam komut listesini görebilirsiniz.
Admin komutları için: `/klanyonetim yardim` (izin: `klan.yonetici`, varsayılan: op)

## Yapılandırma dosyaları
- `config.yml` — genel ayarlar (isim/etiket uzunlukları, üs sistemi, sohbet formatı, sınırlar)
- `lang/tr.yml` — tüm oyuncu mesajları (ön ek dahil, `&` renk kodları ve `&#RRGGBB` hex destekli)

## Sonraki adımlar için öneriler
Bu MVP; ally/rival, üs, rütbe, sohbet gibi temel klan sistemini içerir. Bir sonraki
aşamada konuştuğumuz gibi şunlar eklenebilir:
- Klan seviyesi / XP sistemi
- Klan ekonomisi (Vault entegrasyonu, klan kasası deposit/withdraw)
- Klan savaşları (war) sistemi
- Klan marketi
- Sezon sistemi ve top klanlar sıralaması
- MySQL/SQLite depolama seçeneği (mimari zaten `KlanDeposu` arayüzü üzerinden buna hazır)

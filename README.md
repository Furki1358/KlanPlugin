# FXKlan

Minecraft (Paper 1.21.11) için Türkçe, **GUI-öncelikli** klan eklentisi. Oyuncular
işlerinin çoğunu tıklanabilir menülerden yapar; sunucu sahibi ise komut isimlerinden
mesajlara kadar hemen her şeyi metin dosyalarından düzenleyebilir.

## Kurulum

1. **[Releases](../../releases)** sayfasından en son `FXKlan-x.y.z.jar` dosyasını indirin.
2. Sunucunun `plugins/` klasörüne atın, sunucuyu başlatın/yeniden başlatın.
3. Ekstra hiçbir şey gerekmez. Aşağıdaki entegrasyonlar tamamen opsiyoneldir ve
   kuruluysa otomatik devreye girer:
   - **Vault** — öldürme ödülüne para eklemek isterseniz
   - **PlaceholderAPI** — `%klan_isim%` gibi placeholder'lar için
   - **LuckPerms** (veya herhangi bir izin eklentisi) — komutları tek tek açıp
     kapatmak için (bkz. aşağıdaki İzinler bölümü)

## Özellikler

- Klan oluşturma / silme / üyelik / rütbe (Üye · Yönetici · Lider) / liderlik devri
- Tamamen tıklanabilir GUI: `/klan` yazmak yeterli — üyeler, ayarlar, müttefik/rakip,
  klan listesi, liderlik tablosu, hepsi menüden
- Klan üssü: ışınlanırken sohbette 3-2-1 geri sayımı, hareket edilirse iptal
- Klan sembolü/ikonu: istediğiniz eşyayı GUI'ye bırakarak klanınızın simgesi yapın
- Klan sohbeti ve müttefik sohbeti (ayrı kanal)
- KDR takibi, `/klan liderlik` (sayfalı, tıklanabilir gezinme), öldürme ödülü
  (Vault parası ve/veya konsol komutu)
- Davet, silme onayı gibi işlemler sohbette **tıklanabilir butonlarla** yapılır —
  komut yazmaya gerek yok
- Admin paneli: `/klanyonetim menu` — klanları yönetme, genel ayarları GUI'den değiştirme
- Hatalar `plugins/FXKlan/loglar/hatalar.log` dosyasında toplanır

## Yapılandırma Dosyaları

Hepsi `plugins/FXKlan/` klasöründe, sunucu ilk açılışta otomatik oluşturur:

| Dosya | Ne işe yarar |
|---|---|
| `config.yml` | Genel ayarlar (isim/etiket uzunlukları, üs sistemi, sohbet formatı, KDR ödülleri) |
| `komutlar.yml` | Her komutun ismini/takma adlarını değiştirin (örn. "terfi" yerine "yükselt" yapın) |
| `lang/tr.yml` | Tüm oyuncu mesajları ve GUI metinleri |
| `klanlar/*.yml` | Her klan kendi dosyasında saklanır |
| `istatistikler.yml` | Oyuncu KDR verileri |

Değişiklik sonrası sunucuyu yeniden başlatmadan uygulamak için: `/klanyonetim yenile`

## İzinler (LuckPerms uyumlu)

Her komutun ayrı bir izin düğümü vardır (`klan.komut.<isim>`), varsayılan olarak
herkese açıktır. Belirli bir komutu belirli bir gruba kısıtlamak isterseniz
LuckPerms ile o düğümü kaldırmanız yeterli. Admin komutları (`/klanyonetim`) için
`klan.yonetici` izni gerekir (varsayılan: op).

## Kaynak Koddan Derleme

Bu repo her `main` dalına yapılan pushta GitHub Actions ile otomatik derlenir ve
**Releases** sayfasına jar olarak eklenir — genelde jar'ı Releases'ten indirmeniz
yeterlidir. Kendiniz derlemek isterseniz (Java 21 + Maven gerekir):

```bash
mvn clean package
```

## Sürüm Notu

Şu an **beta** aşamasındadır (`1.0.x`). Her güncellemede sürüm numarası artar
(`1.0.1` → `1.0.2` → ...) ve ilgili jar Releases sayfasında ayrı bir sürüm olarak
görünür.

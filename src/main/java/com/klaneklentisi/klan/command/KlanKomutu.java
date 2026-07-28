package com.klaneklentisi.klan.command;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.manager.KlanYoneticisi;
import com.klaneklentisi.klan.model.KatilimTuru;
import com.klaneklentisi.klan.model.Klan;
import com.klaneklentisi.klan.model.Rutbe;
import com.klaneklentisi.klan.util.Mesajlar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class KlanKomutu implements CommandExecutor, TabCompleter {

    private static final Locale TR = Locale.forLanguageTag("tr");
    private static final List<String> ALT_KOMUTLAR = Arrays.asList(
            "yardim", "olustur", "sil", "bilgi", "liste", "davet", "kabul", "reddet",
            "katil", "ayril", "at", "yukselt", "indir", "devret", "katilimturu",
            "etiket", "aciklama", "us", "muttefik", "rakip", "sohbet", "menu", "gui"
    );

    private final KlanEklentisi eklenti;
    private final KlanYoneticisi yonetici;
    private final Mesajlar mesajlar;

    public KlanKomutu(KlanEklentisi eklenti) {
        this.eklenti = eklenti;
        this.yonetici = eklenti.getKlanYoneticisi();
        this.mesajlar = eklenti.getMesajlar();
    }

    private void gonder(CommandSender alici, String anahtar, Map<String, String> yer) {
        alici.sendMessage(mesajlar.al(anahtar, yer));
    }

    private void gonder(CommandSender alici, String anahtar) {
        alici.sendMessage(mesajlar.al(anahtar));
    }

    private Map<String, String> harita(String... kv) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        return m;
    }

    @Override
    public boolean onCommand(CommandSender gonderen, Command komut, String etiket, String[] args) {
        if (args.length == 0) {
            yardimGoster(gonderen);
            return true;
        }

        String alt = args[0].toLowerCase(TR);
        switch (alt) {
            case "yardim" -> yardimGoster(gonderen);
            case "olustur" -> olustur(gonderen, args);
            case "sil" -> sil(gonderen, args);
            case "bilgi" -> bilgi(gonderen, args);
            case "liste" -> liste(gonderen);
            case "davet" -> davet(gonderen, args);
            case "kabul" -> kabul(gonderen);
            case "reddet" -> reddet(gonderen);
            case "katil" -> katil(gonderen, args);
            case "ayril" -> ayril(gonderen);
            case "at" -> at(gonderen, args);
            case "yukselt" -> rutbeDegistir(gonderen, args, true);
            case "indir" -> rutbeDegistir(gonderen, args, false);
            case "devret" -> devret(gonderen, args);
            case "katilimturu" -> katilimTuru(gonderen, args);
            case "etiket" -> etiketDegistir(gonderen, args);
            case "aciklama" -> aciklamaDegistir(gonderen, args);
            case "us" -> us(gonderen, args);
            case "muttefik" -> muttefikRakip(gonderen, args, true);
            case "rakip" -> muttefikRakip(gonderen, args, false);
            case "sohbet" -> sohbet(gonderen, args);
            case "menu", "gui" -> menuAc(gonderen);
            default -> gonder(gonderen, "genel.bilinmeyen-komut");
        }
        return true;
    }

    private void yardimGoster(CommandSender gonderen) {
        List<String> satirlar = mesajlarListesi("yardim-metni");
        for (String satir : satirlar) {
            gonderen.sendMessage(Mesajlar.renkli(satir));
        }
    }

    private List<String> mesajlarListesi(String anahtar) {
        return eklenti.getLangYaml().getStringList(anahtar);
    }

    private boolean oyuncuMu(CommandSender gonderen) {
        if (!(gonderen instanceof Player)) {
            gonder(gonderen, "genel.oyuncu-degil");
            return false;
        }
        return true;
    }

    // -----------------------------------------------------------------
    // OLUŞTUR
    // -----------------------------------------------------------------
    private void olustur(CommandSender gonderen, String[] args) {
        if (!oyuncuMu(gonderen)) return;
        Player oyuncu = (Player) gonderen;
        if (args.length < 3) {
            gonder(gonderen, "kullanim.olustur");
            return;
        }
        String isim = args[1];
        String etiket = args[2];

        KlanYoneticisi.Sonuc sonuc = yonetici.klanOlustur(oyuncu, isim, etiket);
        switch (sonuc) {
            case BASARILI -> gonder(gonderen, "olustur.basarili", harita("isim", isim, "etiket", etiket));
            case ZATEN_KLANDA -> gonder(gonderen, "olustur.zaten-klanda");
            case GECERSIZ_ISIM -> gonder(gonderen, "olustur.gecersiz-isim", harita(
                    "min", eklenti.getConfig().getString("genel.min-isim-uzunlugu"),
                    "maks", eklenti.getConfig().getString("genel.maks-isim-uzunlugu")));
            case GECERSIZ_ETIKET -> gonder(gonderen, "olustur.gecersiz-etiket", harita(
                    "min", eklenti.getConfig().getString("genel.min-etiket-uzunlugu"),
                    "maks", eklenti.getConfig().getString("genel.maks-etiket-uzunlugu")));
            case ISIM_KULLANIMDA -> gonder(gonderen, "olustur.isim-kullanimda");
            case ETIKET_KULLANIMDA -> gonder(gonderen, "olustur.etiket-kullanimda");
            default -> {}
        }
    }

    // -----------------------------------------------------------------
    // SİL
    // -----------------------------------------------------------------
    private void sil(CommandSender gonderen, String[] args) {
        if (!oyuncuMu(gonderen)) return;
        Player oyuncu = (Player) gonderen;
        Optional<Klan> klanOpt = yonetici.klanBul(oyuncu.getUniqueId());
        if (klanOpt.isEmpty()) {
            gonder(gonderen, "genel.klanin-yok");
            return;
        }
        Klan klan = klanOpt.get();
        Rutbe rutbe = klan.getRutbe(oyuncu.getUniqueId());
        if (rutbe != Rutbe.LIDER) {
            gonder(gonderen, "sil.yetkisiz");
            return;
        }
        if (args.length < 2 || !args[1].equalsIgnoreCase("onayla")) {
            gonder(gonderen, "sil.onay-gerekli");
            return;
        }
        yonetici.klanSil(klan.getIsim());
        gonder(gonderen, "sil.basarili");
    }

    // -----------------------------------------------------------------
    // BİLGİ
    // -----------------------------------------------------------------
    private void bilgi(CommandSender gonderen, String[] args) {
        Klan klan;
        if (args.length >= 2) {
            klan = yonetici.klanBul(args[1]).orElse(null);
        } else if (gonderen instanceof Player oyuncu) {
            klan = yonetici.klanBul(oyuncu.getUniqueId()).orElse(null);
        } else {
            gonder(gonderen, "kullanim.bilgi");
            return;
        }

        if (klan == null) {
            gonder(gonderen, "bilgi.klan-yok");
            return;
        }
        bilgiGoster(gonderen, klan);
    }

    /** Belirli bir klanın bilgi ekranını gönderir. Hem komuttan hem GUI'den çağrılabilir. */
    public void bilgiGoster(CommandSender gonderen, Klan klan) {
        String liderAdi = Bukkit.getOfflinePlayer(klan.getKurucu()).getName();
        gonder(gonderen, "bilgi.baslik", harita("isim", klan.getIsim(), "etiket", klan.getEtiket()));
        gonder(gonderen, "bilgi.aciklama", harita("aciklama", klan.getAciklama().isEmpty() ? "-" : klan.getAciklama()));
        gonder(gonderen, "bilgi.lider", harita("lider", liderAdi == null ? "?" : liderAdi));
        gonder(gonderen, "bilgi.uye-sayisi", harita("sayi", String.valueOf(klan.getUyeSayisi())));
        gonder(gonderen, "bilgi.katilim-turu", harita("tur", klan.getKatilimTuru().name()));

        String bos = mesajlar.hamMetin("bilgi.liste-bos");
        String muttefikListesi = klan.getMuttefikler().isEmpty() ? bos : String.join(", ", klan.getMuttefikler());
        String rakipListesi = klan.getRakipler().isEmpty() ? bos : String.join(", ", klan.getRakipler());
        gonder(gonderen, "bilgi.muttefikler", harita("liste", muttefikListesi));
        gonder(gonderen, "bilgi.rakipler", harita("liste", rakipListesi));

        String tarih = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(klan.getOlusturulmaZamani()));
        gonder(gonderen, "bilgi.kurulus-tarihi", harita("tarih", tarih));
    }

    // -----------------------------------------------------------------
    // LİSTE
    // -----------------------------------------------------------------
    private void liste(CommandSender gonderen) {
        var klanlar = yonetici.tumKlanlar();
        gonder(gonderen, "liste.baslik", harita("sayi", String.valueOf(klanlar.size())));
        if (klanlar.isEmpty()) {
            gonder(gonderen, "liste.bos");
            return;
        }
        for (Klan klan : klanlar) {
            gonder(gonderen, "liste.satir", harita(
                    "isim", klan.getIsim(), "etiket", klan.getEtiket(),
                    "uyeSayisi", String.valueOf(klan.getUyeSayisi())));
        }
    }

    // -----------------------------------------------------------------
    // DAVET / KABUL / REDDET / KATIL
    // -----------------------------------------------------------------
    private void davet(CommandSender gonderen, String[] args) {
        if (!oyuncuMu(gonderen)) return;
        Player davetEden = (Player) gonderen;
        if (args.length < 2) {
            gonder(gonderen, "kullanim.davet");
            return;
        }
        Optional<Klan> klanOpt = yonetici.klanBul(davetEden.getUniqueId());
        if (klanOpt.isEmpty()) {
            gonder(gonderen, "genel.klanin-yok");
            return;
        }
        Player hedef = Bukkit.getPlayerExact(args[1]);
        if (hedef == null) {
            gonder(gonderen, "genel.oyuncu-bulunamadi", harita("oyuncu", args[1]));
            return;
        }
        if (hedef.getUniqueId().equals(davetEden.getUniqueId())) {
            gonder(gonderen, "davet.kendine");
            return;
        }

        KlanYoneticisi.Sonuc sonuc = yonetici.davetGonder(klanOpt.get(), davetEden, hedef);
        switch (sonuc) {
            case BASARILI -> {
                gonder(gonderen, "davet.gonderildi", harita("oyuncu", hedef.getName()));
                gonder(hedef, "davet.aldin", harita("klan", klanOpt.get().getIsim()));
            }
            case YETKISIZ -> gonder(gonderen, "davet.yetkisiz");
            case ZATEN_KLANDA -> gonder(gonderen, "davet.zaten-klanda");
            default -> {}
        }
    }

    private void kabul(CommandSender gonderen) {
        if (!oyuncuMu(gonderen)) return;
        Player oyuncu = (Player) gonderen;
        KlanYoneticisi.Sonuc sonuc = yonetici.davetKabulEt(oyuncu);
        switch (sonuc) {
            case BASARILI -> {
                Klan klan = yonetici.klanBul(oyuncu.getUniqueId()).orElseThrow();
                gonder(gonderen, "kabul.basarili", harita("klan", klan.getIsim()));
                klanUyelerineMesaj(klan, "kabul.duyuru", harita("oyuncu", oyuncu.getName()), oyuncu.getUniqueId());
            }
            case DAVET_YOK -> gonder(gonderen, "kabul.davet-yok");
            case ZATEN_KLANDA -> gonder(gonderen, "kabul.zaten-klanda");
            case KLAN_YOK -> gonder(gonderen, "kabul.klan-yok");
            default -> {}
        }
    }

    private void reddet(CommandSender gonderen) {
        if (!oyuncuMu(gonderen)) return;
        Player oyuncu = (Player) gonderen;
        KlanYoneticisi.Sonuc sonuc = yonetici.davetReddet(oyuncu);
        if (sonuc == KlanYoneticisi.Sonuc.DAVET_YOK) {
            gonder(gonderen, "reddet.davet-yok");
        } else {
            gonder(gonderen, "reddet.basarili");
        }
    }

    private void katil(CommandSender gonderen, String[] args) {
        if (!oyuncuMu(gonderen)) return;
        Player oyuncu = (Player) gonderen;
        if (args.length < 2) {
            gonder(gonderen, "kullanim.katil");
            return;
        }
        Optional<Klan> klanOpt = yonetici.klanBul(args[1]);
        if (klanOpt.isEmpty()) {
            gonder(gonderen, "katil.klan-yok");
            return;
        }
        Klan klan = klanOpt.get();
        KlanYoneticisi.Sonuc sonuc = yonetici.klanaKatil(klan, oyuncu);
        switch (sonuc) {
            case BASARILI -> {
                gonder(gonderen, "katil.basarili", harita("klan", klan.getIsim()));
                klanUyelerineMesaj(klan, "katil.duyuru", harita("oyuncu", oyuncu.getName()), oyuncu.getUniqueId());
            }
            case YETKISIZ -> gonder(gonderen, "katil.kapali");
            case ZATEN_KLANDA -> gonder(gonderen, "katil.zaten-klanda");
            default -> {}
        }
    }

    // -----------------------------------------------------------------
    // AYRIL / AT
    // -----------------------------------------------------------------
    private void ayril(CommandSender gonderen) {
        if (!oyuncuMu(gonderen)) return;
        Player oyuncu = (Player) gonderen;
        Optional<Klan> klanOpt = yonetici.klanBul(oyuncu.getUniqueId());
        if (klanOpt.isEmpty()) {
            gonder(gonderen, "ayril.klanin-yok");
            return;
        }
        Klan klan = klanOpt.get();
        String klanIsmi = klan.getIsim();
        KlanYoneticisi.Sonuc sonuc = yonetici.klandanAyril(klan, oyuncu);
        switch (sonuc) {
            case BASARILI -> {
                gonder(gonderen, "ayril.basarili", harita("klan", klanIsmi));
                klanUyelerineMesaj(klan, "ayril.duyuru", harita("oyuncu", oyuncu.getName()), null);
            }
            case KURUCU_AYRILAMAZ -> gonder(gonderen, "ayril.lider-ayrilamaz");
            default -> {}
        }
    }

    private void at(CommandSender gonderen, String[] args) {
        if (!oyuncuMu(gonderen)) return;
        Player yetkili = (Player) gonderen;
        if (args.length < 2) {
            gonder(gonderen, "kullanim.at");
            return;
        }
        Optional<Klan> klanOpt = yonetici.klanBul(yetkili.getUniqueId());
        if (klanOpt.isEmpty()) {
            gonder(gonderen, "genel.klanin-yok");
            return;
        }
        OfflinePlayer hedef = Bukkit.getOfflinePlayer(args[1]);
        Klan klan = klanOpt.get();
        KlanYoneticisi.Sonuc sonuc = yonetici.uyeAt(klan, yetkili, hedef);
        switch (sonuc) {
            case BASARILI -> {
                gonder(gonderen, "at.basarili", harita("oyuncu", args[1]));
                klanUyelerineMesaj(klan, "at.duyuru", harita("oyuncu", args[1]), null);
            }
            case YETKISIZ -> gonder(gonderen, "at.yetkisiz");
            case KLAN_YOK -> gonder(gonderen, "at.oyuncu-yok");
            default -> {}
        }
    }

    // -----------------------------------------------------------------
    // RÜTBE / DEVRET
    // -----------------------------------------------------------------
    private void rutbeDegistir(CommandSender gonderen, String[] args, boolean yukselt) {
        if (!oyuncuMu(gonderen)) return;
        Player yetkili = (Player) gonderen;
        String anahtarOnEk = yukselt ? "yukselt" : "indir";
        if (args.length < 2) {
            gonder(gonderen, "kullanim." + anahtarOnEk);
            return;
        }
        Optional<Klan> klanOpt = yonetici.klanBul(yetkili.getUniqueId());
        if (klanOpt.isEmpty()) {
            gonder(gonderen, "genel.klanin-yok");
            return;
        }
        Klan klan = klanOpt.get();
        OfflinePlayer hedef = Bukkit.getOfflinePlayer(args[1]);
        KlanYoneticisi.Sonuc sonuc = yukselt
                ? yonetici.rutbeYukselt(klan, yetkili, hedef)
                : yonetici.rutbeIndir(klan, yetkili, hedef);

        switch (sonuc) {
            case BASARILI -> {
                Rutbe yeniRutbe = klan.getRutbe(hedef.getUniqueId());
                gonder(gonderen, anahtarOnEk + ".basarili", harita(
                        "oyuncu", args[1], "rutbe", yeniRutbe.getGorunenAd()));
            }
            case YETKISIZ -> gonder(gonderen, anahtarOnEk + ".yetkisiz");
            case KLAN_YOK -> gonder(gonderen, anahtarOnEk + ".oyuncu-yok");
            default -> {}
        }
    }

    private void devret(CommandSender gonderen, String[] args) {
        if (!oyuncuMu(gonderen)) return;
        Player lider = (Player) gonderen;
        if (args.length < 2) {
            gonder(gonderen, "kullanim.devret");
            return;
        }
        Optional<Klan> klanOpt = yonetici.klanBul(lider.getUniqueId());
        if (klanOpt.isEmpty()) {
            gonder(gonderen, "genel.klanin-yok");
            return;
        }
        OfflinePlayer yeniLider = Bukkit.getOfflinePlayer(args[1]);
        KlanYoneticisi.Sonuc sonuc = yonetici.liderlikDevret(klanOpt.get(), lider, yeniLider);
        switch (sonuc) {
            case BASARILI -> gonder(gonderen, "devret.basarili", harita("oyuncu", args[1]));
            case YETKISIZ -> gonder(gonderen, "devret.yetkisiz");
            case KLAN_YOK -> gonder(gonderen, "devret.oyuncu-yok");
            default -> {}
        }
    }

    // -----------------------------------------------------------------
    // KATILIM TÜRÜ / ETİKET / AÇIKLAMA
    // -----------------------------------------------------------------
    private void katilimTuru(CommandSender gonderen, String[] args) {
        if (!oyuncuMu(gonderen)) return;
        Player oyuncu = (Player) gonderen;
        if (args.length < 2) {
            gonder(gonderen, "kullanim.katilim-turu");
            return;
        }
        Optional<Klan> klanOpt = yonetici.klanBul(oyuncu.getUniqueId());
        if (klanOpt.isEmpty()) {
            gonder(gonderen, "genel.klanin-yok");
            return;
        }
        Klan klan = klanOpt.get();
        Rutbe rutbe = klan.getRutbe(oyuncu.getUniqueId());
        if (rutbe == null || rutbe.getSeviye() < Rutbe.YONETICI.getSeviye()) {
            gonder(gonderen, "katilimturu.yetkisiz");
            return;
        }
        String girdi = args[1].toLowerCase(TR);
        if (!girdi.equals("acik") && !girdi.equals("açık") && !girdi.equals("davetli")) {
            gonder(gonderen, "katilimturu.gecersiz");
            return;
        }
        klan.setKatilimTuru(KatilimTuru.cozumle(girdi));
        yonetici.kaydet(klan);
        gonder(gonderen, "katilimturu.basarili", harita("tur", klan.getKatilimTuru().name()));
    }

    private void etiketDegistir(CommandSender gonderen, String[] args) {
        if (!oyuncuMu(gonderen)) return;
        Player oyuncu = (Player) gonderen;
        if (args.length < 2) {
            gonder(gonderen, "kullanim.etiket");
            return;
        }
        Optional<Klan> klanOpt = yonetici.klanBul(oyuncu.getUniqueId());
        if (klanOpt.isEmpty()) {
            gonder(gonderen, "genel.klanin-yok");
            return;
        }
        Klan klan = klanOpt.get();
        Rutbe rutbe = klan.getRutbe(oyuncu.getUniqueId());
        if (rutbe != Rutbe.LIDER) {
            gonder(gonderen, "etiket.yetkisiz");
            return;
        }
        String yeniEtiket = args[1];
        int min = eklenti.getConfig().getInt("genel.min-etiket-uzunlugu", 2);
        int maks = eklenti.getConfig().getInt("genel.maks-etiket-uzunlugu", 6);
        if (yeniEtiket.length() < min || yeniEtiket.length() > maks) {
            gonder(gonderen, "etiket.gecersiz", harita("min", String.valueOf(min), "maks", String.valueOf(maks)));
            return;
        }
        boolean kullanimda = yonetici.tumKlanlar().stream()
                .anyMatch(k -> !k.getIsim().equalsIgnoreCase(klan.getIsim()) && k.getEtiket().equalsIgnoreCase(yeniEtiket));
        if (kullanimda) {
            gonder(gonderen, "etiket.kullanimda");
            return;
        }
        klan.setEtiket(yeniEtiket);
        yonetici.kaydet(klan);
        gonder(gonderen, "etiket.basarili", harita("etiket", yeniEtiket));
    }

    private void aciklamaDegistir(CommandSender gonderen, String[] args) {
        if (!oyuncuMu(gonderen)) return;
        Player oyuncu = (Player) gonderen;
        if (args.length < 2) {
            gonder(gonderen, "kullanim.aciklama");
            return;
        }
        Optional<Klan> klanOpt = yonetici.klanBul(oyuncu.getUniqueId());
        if (klanOpt.isEmpty()) {
            gonder(gonderen, "genel.klanin-yok");
            return;
        }
        Klan klan = klanOpt.get();
        Rutbe rutbe = klan.getRutbe(oyuncu.getUniqueId());
        if (rutbe == null || rutbe.getSeviye() < Rutbe.YONETICI.getSeviye()) {
            gonder(gonderen, "aciklama.yetkisiz");
            return;
        }
        String metin = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        klan.setAciklama(metin);
        yonetici.kaydet(klan);
        gonder(gonderen, "aciklama.basarili");
    }

    // -----------------------------------------------------------------
    // ÜS (HOME)
    // -----------------------------------------------------------------
    private void us(CommandSender gonderen, String[] args) {
        if (!oyuncuMu(gonderen)) return;
        Player oyuncu = (Player) gonderen;

        if (!eklenti.getConfig().getBoolean("us-sistemi.aktif", true)) {
            gonder(gonderen, "us.kapali");
            return;
        }

        Optional<Klan> klanOpt = yonetici.klanBul(oyuncu.getUniqueId());
        if (klanOpt.isEmpty()) {
            gonder(gonderen, "genel.klanin-yok");
            return;
        }
        Klan klan = klanOpt.get();

        if (args.length >= 2 && args[1].equalsIgnoreCase("ayarla")) {
            KlanYoneticisi.Sonuc sonuc = yonetici.usAyarla(klan, oyuncu, oyuncu.getLocation());
            if (sonuc == KlanYoneticisi.Sonuc.YETKISIZ) {
                gonder(gonderen, "us.yetkisiz");
            } else {
                gonder(gonderen, "us.ayarlandi");
            }
            return;
        }

        usaIsinlan(oyuncu);
    }

    /** Oyuncuyu kendi klan üssüne ışınlar (bekleme süresi ve hareket kontrolüyle). Komut ve GUI ortak kullanır. */
    public void usaIsinlan(Player oyuncu) {
        if (!eklenti.getConfig().getBoolean("us-sistemi.aktif", true)) {
            gonder(oyuncu, "us.kapali");
            return;
        }
        Optional<Klan> klanOpt = yonetici.klanBul(oyuncu.getUniqueId());
        if (klanOpt.isEmpty()) {
            gonder(oyuncu, "genel.klanin-yok");
            return;
        }
        Klan klan = klanOpt.get();

        Location hedefKonum = klan.getUs();
        if (hedefKonum == null) {
            gonder(oyuncu, "us.yok");
            return;
        }

        int bekleme = eklenti.getConfig().getInt("us-sistemi.bekleme-suresi", 3);
        if (bekleme <= 0) {
            oyuncu.teleport(hedefKonum);
            gonder(oyuncu, "us.isinlandi");
            return;
        }

        gonder(oyuncu, "us.isinlaniyor", harita("saniye", String.valueOf(bekleme)));
        Location baslangicKonum = oyuncu.getLocation().clone();
        boolean hareketIptal = eklenti.getConfig().getBoolean("us-sistemi.hareket-ederse-iptal", true);

        Bukkit.getScheduler().runTaskLater(eklenti, () -> {
            if (!oyuncu.isOnline()) return;
            if (hareketIptal && konumFarkliMi(baslangicKonum, oyuncu.getLocation())) {
                gonder(oyuncu, "us.iptal-hareket");
                return;
            }
            oyuncu.teleport(hedefKonum);
            gonder(oyuncu, "us.isinlandi");
        }, bekleme * 20L);
    }

    private boolean konumFarkliMi(Location a, Location b) {
        if (!a.getWorld().equals(b.getWorld())) return true;
        return a.distanceSquared(b) > 0.09; // ~0.3 blok tolerans
    }

    // -----------------------------------------------------------------
    // MÜTTEFİK / RAKİP
    // -----------------------------------------------------------------
    private void muttefikRakip(CommandSender gonderen, String[] args, boolean muttefik) {
        if (!oyuncuMu(gonderen)) return;
        Player oyuncu = (Player) gonderen;
        String onEk = muttefik ? "muttefik" : "rakip";
        if (args.length < 3) {
            gonder(gonderen, "kullanim." + onEk);
            return;
        }
        Optional<Klan> klanOpt = yonetici.klanBul(oyuncu.getUniqueId());
        if (klanOpt.isEmpty()) {
            gonder(gonderen, "genel.klanin-yok");
            return;
        }
        Klan klan = klanOpt.get();
        Rutbe rutbe = klan.getRutbe(oyuncu.getUniqueId());
        if (rutbe == null || rutbe.getSeviye() < Rutbe.YONETICI.getSeviye()) {
            gonder(gonderen, onEk + ".yetkisiz");
            return;
        }

        Optional<Klan> hedefOpt = yonetici.klanBul(args[2]);
        if (hedefOpt.isEmpty()) {
            gonder(gonderen, onEk + ".klan-yok");
            return;
        }
        Klan hedef = hedefOpt.get();
        String islem = args[1].toLowerCase(TR);

        KlanYoneticisi.Sonuc sonuc;
        if (islem.equals("ekle")) {
            sonuc = muttefik ? yonetici.muttefikEkle(klan, hedef) : yonetici.rakipEkle(klan, hedef);
        } else if (islem.equals("cikar") || islem.equals("çıkar")) {
            sonuc = muttefik ? yonetici.muttefikCikar(klan, hedef) : yonetici.rakipCikar(klan, hedef);
        } else {
            gonder(gonderen, "kullanim." + onEk);
            return;
        }

        switch (sonuc) {
            case BASARILI -> gonder(gonderen, onEk + (islem.equals("ekle") ? ".eklendi" : ".cikarildi"),
                    harita("klan", hedef.getIsim()));
            case KENDISI -> gonder(gonderen, onEk + ".kendine");
            case ZATEN_MUTTEFIK, ZATEN_RAKIP -> gonder(gonderen, onEk + ".zaten");
            case MUTTEFIK_DEGIL, RAKIP_DEGIL -> gonder(gonderen, onEk + ".degil");
            case SINIR_ASILDI -> gonder(gonderen, onEk + ".sinir");
            default -> {}
        }
    }

    // -----------------------------------------------------------------
    // KLAN SOHBETİ
    // -----------------------------------------------------------------
    private void sohbet(CommandSender gonderen, String[] args) {
        if (!oyuncuMu(gonderen)) return;
        Player oyuncu = (Player) gonderen;
        Optional<Klan> klanOpt = yonetici.klanBul(oyuncu.getUniqueId());
        if (klanOpt.isEmpty()) {
            gonder(gonderen, "sohbet.klanin-yok");
            return;
        }
        Klan klan = klanOpt.get();

        if (args.length >= 2) {
            String mesaj = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            klanSohbetMesajiGonder(klan, oyuncu, mesaj);
            return;
        }

        yonetici.sohbetModunuDegistir(oyuncu.getUniqueId());
        if (yonetici.sohbetModuAcikMi(oyuncu.getUniqueId())) {
            gonder(gonderen, "sohbet.acildi");
        } else {
            gonder(gonderen, "sohbet.kapandi");
        }
    }

    public void klanSohbetMesajiGonder(Klan klan, Player gonderen, String mesaj) {
        Rutbe rutbe = klan.getRutbe(gonderen.getUniqueId());
        String format = eklenti.getConfig().getString("sohbet.format",
                "&8[&6{etiket}&8] &7{rutbe} &f{oyuncu}&8: &f{mesaj}");
        format = format.replace("{etiket}", klan.getEtiket())
                .replace("{rutbe}", rutbe == null ? "" : rutbe.getGorunenAd())
                .replace("{oyuncu}", gonderen.getName())
                .replace("{mesaj}", mesaj);
        String renkli = Mesajlar.renkli(format);
        for (var girdi : klan.getUyeler().keySet()) {
            Player alici = Bukkit.getPlayer(girdi);
            if (alici != null) {
                alici.sendMessage(renkli);
            }
        }
    }

    private void klanUyelerineMesaj(Klan klan, String anahtar, Map<String, String> yer, java.util.UUID haric) {
        for (var uid : klan.getUyeler().keySet()) {
            if (haric != null && uid.equals(haric)) continue;
            Player alici = Bukkit.getPlayer(uid);
            if (alici != null) {
                gonder(alici, anahtar, yer);
            }
        }
    }

    private void menuAc(CommandSender gonderen) {
        if (!oyuncuMu(gonderen)) return;
        new com.klaneklentisi.klan.gui.AnaMenu(eklenti, (Player) gonderen).ac();
    }

    // -----------------------------------------------------------------
    // TAB TAMAMLAMA
    // -----------------------------------------------------------------
    @Override
    public List<String> onTabComplete(CommandSender gonderen, Command komut, String etiket, String[] args) {
        if (args.length == 1) {
            String baslangic = args[0].toLowerCase(TR);
            return ALT_KOMUTLAR.stream().filter(s -> s.startsWith(baslangic)).collect(Collectors.toList());
        }
        if (args.length == 2) {
            String alt = args[0].toLowerCase(TR);
            return switch (alt) {
                case "davet", "at", "yukselt", "indir", "devret" -> Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName).collect(Collectors.toList());
                case "katil", "bilgi" -> yonetici.tumKlanlar().stream()
                        .map(Klan::getIsim).collect(Collectors.toList());
                case "katilimturu" -> Arrays.asList("acik", "davetli");
                case "muttefik", "rakip" -> Arrays.asList("ekle", "cikar");
                default -> new ArrayList<>();
            };
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("muttefik") || args[0].equalsIgnoreCase("rakip"))) {
            return yonetici.tumKlanlar().stream().map(Klan::getIsim).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}

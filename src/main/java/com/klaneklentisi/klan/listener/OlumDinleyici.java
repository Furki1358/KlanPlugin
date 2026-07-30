package com.klaneklentisi.klan.listener;

import com.klaneklentisi.klan.KlanEklentisi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

/**
 * Oyuncular arası (PvP) öldürmeleri yakalar: istatistik günceller, isteğe bağlı
 * komut/para ödülü verir.
 */
public class OlumDinleyici implements Listener {

    private final KlanEklentisi eklenti;

    public OlumDinleyici(KlanEklentisi eklenti) {
        this.eklenti = eklenti;
    }

    @EventHandler
    public void onOlum(PlayerDeathEvent olay) {
        Player olen = olay.getEntity();
        Player katil = olen.getKiller();
        if (katil == null || katil.getUniqueId().equals(olen.getUniqueId())) return;

        eklenti.getIstatistikYoneticisi().olumKaydet(katil.getUniqueId(), olen.getUniqueId());

        if (!eklenti.getConfig().getBoolean("istatistik.aktif", true)) return;

        if (odulEngelliMi(katil, olen)) {
            return; // aynı klan/müttefik - istatistik sayıldı ama ödül verilmedi (farm istismarını önler)
        }

        // Konsol komutları (örn. verilecek eşya/efekt) - {oyuncu} ve {hedef} yer tutucuları desteklenir
        List<String> komutlar = eklenti.getConfig().getStringList("istatistik.odul-komutlari");
        for (String komut : komutlar) {
            String islenmis = komut.replace("{oyuncu}", katil.getName()).replace("{hedef}", olen.getName());
            Bukkit.getScheduler().runTask(eklenti, () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), islenmis));
        }

        double vaultOdulu = eklenti.getConfig().getDouble("istatistik.vault-para-odulu", 0);
        if (vaultOdulu > 0 && eklenti.getVaultEkonomi().aktifMi()) {
            eklenti.getVaultEkonomi().paraEkle(katil, vaultOdulu);
        }

        if (eklenti.getConfig().getBoolean("istatistik.olum-mesaji-goster", true)) {
            var istatistik = eklenti.getIstatistikYoneticisi().getIstatistik(katil.getUniqueId());
            katil.sendMessage(eklenti.getMesajlar().al("istatistik.olum-bildirimi", java.util.Map.of(
                    "hedef", olen.getName(),
                    "oran", String.valueOf(istatistik.getOran()))));
        }
    }

    /**
     * Katil ve ölen aynı klandaysa veya klanları müttefikse ödülü engeller.
     * Bu kontrol olmadan iki oyuncu (veya iki müttefik klan üyesi) birbirini
     * güvenli şekilde tekrar tekrar öldürerek sınırsız para/ödül üretebilirdi.
     */
    private boolean odulEngelliMi(Player katil, Player olen) {
        if (!eklenti.getConfig().getBoolean("istatistik.odul-sadece-dusman-klan", true)) {
            return false; // sunucu sahibi bu korumayı bilerek kapatmış
        }
        var katilKlanOpt = eklenti.getKlanYoneticisi().klanBul(katil.getUniqueId());
        var olenKlanOpt = eklenti.getKlanYoneticisi().klanBul(olen.getUniqueId());
        if (katilKlanOpt.isEmpty() || olenKlanOpt.isEmpty()) return false;

        var katilKlan = katilKlanOpt.get();
        var olenKlan = olenKlanOpt.get();
        if (katilKlan.getIsim().equalsIgnoreCase(olenKlan.getIsim())) return true; // aynı klan
        return katilKlan.muttefikMi(olenKlan.getIsim()) || olenKlan.muttefikMi(katilKlan.getIsim());
    }
}

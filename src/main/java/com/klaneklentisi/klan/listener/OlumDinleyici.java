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
}

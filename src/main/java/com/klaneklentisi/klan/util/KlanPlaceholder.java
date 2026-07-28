package com.klaneklentisi.klan.util;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.manager.KlanYoneticisi;
import com.klaneklentisi.klan.model.Klan;
import com.klaneklentisi.klan.model.Rutbe;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Kullanılabilir placeholder'lar:
 *   %klan_isim%        - oyuncunun klan ismi (yoksa "-")
 *   %klan_etiket%       - klan etiketi, örn: [ABC]
 *   %klan_rutbe%        - oyuncunun klan içindeki rütbesi
 *   %klan_uye_sayisi%   - klan üye sayısı
 *   %klan_lider%        - klan lideri
 */
public class KlanPlaceholder extends PlaceholderExpansion {

    private final KlanEklentisi eklenti;
    private final KlanYoneticisi yonetici;

    public KlanPlaceholder(KlanEklentisi eklenti) {
        this.eklenti = eklenti;
        this.yonetici = eklenti.getKlanYoneticisi();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "klan";
    }

    @Override
    public @NotNull String getAuthor() {
        return "KlanEklentisi";
    }

    @Override
    public @NotNull String getVersion() {
        return eklenti.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer oyuncu, @NotNull String params) {
        if (oyuncu == null) return "";
        Optional<Klan> klanOpt = yonetici.klanBul(oyuncu.getUniqueId());

        return switch (params.toLowerCase()) {
            case "isim" -> klanOpt.map(Klan::getIsim).orElse("-");
            case "etiket" -> klanOpt.map(k -> "[" + k.getEtiket() + "]").orElse("");
            case "rutbe" -> klanOpt.map(k -> {
                Rutbe r = k.getRutbe(oyuncu.getUniqueId());
                return r == null ? "-" : r.getGorunenAd();
            }).orElse("-");
            case "uye_sayisi" -> klanOpt.map(k -> String.valueOf(k.getUyeSayisi())).orElse("0");
            case "lider" -> klanOpt.map(k -> {
                OfflinePlayer lider = eklenti.getServer().getOfflinePlayer(k.getKurucu());
                return lider.getName() == null ? "-" : lider.getName();
            }).orElse("-");
            default -> null;
        };
    }
}

package com.klaneklentisi.klan.util;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Vault üzerinden ekonomi eklentisine (varsa) bağlanır. Vault yoksa tüm işlemler
 * sessizce yok sayılır (para ödülü devre dışı kalır, hata fırlatmaz).
 */
public class VaultEkonomi {

    private Economy ekonomi;

    public VaultEkonomi(JavaPlugin eklenti) {
        if (eklenti.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> saglayici = eklenti.getServer()
                .getServicesManager().getRegistration(Economy.class);
        if (saglayici != null) {
            this.ekonomi = saglayici.getProvider();
        }
    }

    public boolean aktifMi() {
        return ekonomi != null;
    }

    /** Belirtilen oyuncuya para ekler. Vault/ekonomi yoksa hiçbir şey yapmaz. */
    public void paraEkle(OfflinePlayer oyuncu, double miktar) {
        if (ekonomi == null || miktar <= 0) return;
        ekonomi.depositPlayer(oyuncu, miktar);
    }

    public String formatla(double miktar) {
        return ekonomi != null ? ekonomi.format(miktar) : String.valueOf(miktar);
    }
}

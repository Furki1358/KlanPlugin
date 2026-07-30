package com.klaneklentisi.klan.listener;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.command.KlanKomutu;
import com.klaneklentisi.klan.manager.KlanYoneticisi;
import com.klaneklentisi.klan.model.Klan;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

/**
 * Oyuncu klan sohbet modundayken (/klan sohbet ile açılan mod) yazdığı her mesaj
 * normal genel sohbete gitmek yerine sadece klan üyelerine iletilir.
 *
 * ÖNEMLİ (thread-safety): AsyncChatEvent, adından da anlaşılacağı gibi ANA THREAD
 * DIŞINDA bir thread'de tetiklenir. Klan verilerine (KlanYoneticisi'nin Map'leri,
 * GUI açma vb.) buradan doğrudan erişmek yarış koşuluna (race condition) yol açar.
 * Bu yüzden gerçek işlem her zaman runTask ile ana thread'e devrediyoruz.
 */
public class SohbetDinleyici implements Listener {

    private final KlanEklentisi eklenti;
    private final KlanYoneticisi yonetici;
    private final KlanKomutu klanKomutu;

    public SohbetDinleyici(KlanEklentisi eklenti, KlanKomutu klanKomutu) {
        this.eklenti = eklenti;
        this.yonetici = eklenti.getKlanYoneticisi();
        this.klanKomutu = klanKomutu;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSohbet(AsyncChatEvent olay) {
        Player oyuncu = olay.getPlayer();
        boolean girdiBekleniyor = eklenti.getGirdiYoneticisi().bekleyenVarMi(oyuncu.getUniqueId());
        boolean klanSohbeti = yonetici.sohbetModuAcikMi(oyuncu.getUniqueId());
        boolean muttefikSohbeti = yonetici.mSohbetModuAcikMi(oyuncu.getUniqueId());

        if (!girdiBekleniyor && !klanSohbeti && !muttefikSohbeti) {
            return; // normal sohbet, dokunma
        }

        olay.setCancelled(true);
        String duzMetin = PlainTextComponentSerializer.plainText().serialize(olay.message());

        // Tüm gerçek işlem ana thread'de: klan verisi okuma/yazma ve GUI açma
        // thread-safe değildir, async event thread'inde asla yapılmamalı.
        eklenti.getServer().getScheduler().runTask(eklenti, () -> {
            if (girdiBekleniyor) {
                eklenti.getGirdiYoneticisi().girdiIsle(oyuncu.getUniqueId(), duzMetin);
                return;
            }
            Optional<Klan> klanOpt = yonetici.klanBul(oyuncu.getUniqueId());
            if (klanOpt.isEmpty()) return;
            if (klanSohbeti) {
                klanKomutu.klanSohbetMesajiGonder(klanOpt.get(), oyuncu, duzMetin);
            } else {
                klanKomutu.klanMuttefikSohbetiGonder(klanOpt.get(), oyuncu, duzMetin);
            }
        });
    }

    /** Oyuncu çıkış yaptığında bekleyen girdi isteğini temizler. Aksi halde günler sonra
     *  girdikleri ilk mesaj yanlışlıkla eski bir isteğe (örn. etiket değişikliği) uygulanabilirdi. */
    @EventHandler
    public void onCikis(PlayerQuitEvent olay) {
        eklenti.getGirdiYoneticisi().iptalEt(olay.getPlayer().getUniqueId());
    }
}

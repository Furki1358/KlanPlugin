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

import java.util.Optional;

/**
 * Oyuncu klan sohbet modundayken (/klan sohbet ile açılan mod) yazdığı her mesaj
 * normal genel sohbete gitmek yerine sadece klan üyelerine iletilir.
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

        if (eklenti.getGirdiYoneticisi().bekleyenVarMi(oyuncu.getUniqueId())) {
            olay.setCancelled(true);
            String duzMetin = PlainTextComponentSerializer.plainText().serialize(olay.message());
            // Ana thread'de işlensin (envanter açma/inventory API thread-safe değil)
            eklenti.getServer().getScheduler().runTask(eklenti, () ->
                    eklenti.getGirdiYoneticisi().girdiIsle(oyuncu.getUniqueId(), duzMetin));
            return;
        }

        if (yonetici.sohbetModuAcikMi(oyuncu.getUniqueId())) {
            Optional<Klan> klanOpt = yonetici.klanBul(oyuncu.getUniqueId());
            if (klanOpt.isEmpty()) return;
            olay.setCancelled(true);
            String duzMetin = PlainTextComponentSerializer.plainText().serialize(olay.message());
            klanKomutu.klanSohbetMesajiGonder(klanOpt.get(), oyuncu, duzMetin);
            return;
        }

        if (yonetici.mSohbetModuAcikMi(oyuncu.getUniqueId())) {
            Optional<Klan> klanOpt = yonetici.klanBul(oyuncu.getUniqueId());
            if (klanOpt.isEmpty()) return;
            olay.setCancelled(true);
            String duzMetin = PlainTextComponentSerializer.plainText().serialize(olay.message());
            klanKomutu.klanMuttefikSohbetiGonder(klanOpt.get(), oyuncu, duzMetin);
        }
    }
}

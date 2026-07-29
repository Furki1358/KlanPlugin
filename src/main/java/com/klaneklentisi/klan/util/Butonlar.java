package com.klaneklentisi.klan.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Sohbette tek tıkla işlem yapılabilen butonlar oluşturur (örn. davet kabul/reddet,
 * silme onayı). Böylece oyuncunun komut yazmasına gerek kalmaz.
 */
public final class Butonlar {

    private Butonlar() {}

    /** & renkli bir metni Adventure Component'e çevirir (mesaj gövdesi için). */
    public static Component metin(String legacyRenkli) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(legacyRenkli);
    }

    /**
     * Tıklanınca sunucu tarafında doğrudan çalışan bir buton oluşturur (komut yazmaya gerek yok).
     * @param etiket   Buton üzerinde görünecek metin (örn. "[✔ Kabul Et]")
     * @param renk     Buton rengi
     * @param ipucu    Fareyle üzerine gelince görünecek açıklama (null olabilir)
     * @param aksiyon  Tıklanınca çalışacak kod
     */
    public static Component buton(String etiket, NamedTextColor renk, String ipucu, Consumer<Player> aksiyon) {
        Component bilesen = Component.text(etiket, renk).decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.callback(izleyici -> {
                    if (izleyici instanceof Player oyuncu) {
                        aksiyon.accept(oyuncu);
                    }
                }, olusturucu -> olusturucu.lifetime(Duration.ofMinutes(5)).uses(1)));

        if (ipucu != null) {
            bilesen = bilesen.hoverEvent(HoverEvent.showText(Component.text(ipucu)));
        }
        return bilesen;
    }
}

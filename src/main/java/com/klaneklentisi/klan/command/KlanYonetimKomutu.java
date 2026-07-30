package com.klaneklentisi.klan.command;

import com.klaneklentisi.klan.KlanEklentisi;
import com.klaneklentisi.klan.manager.KlanYoneticisi;
import com.klaneklentisi.klan.model.Klan;
import com.klaneklentisi.klan.util.Mesajlar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class KlanYonetimKomutu implements CommandExecutor, TabCompleter {

    private static final Locale TR = Locale.forLanguageTag("tr");

    private final KlanEklentisi eklenti;
    private final KlanYoneticisi yonetici;
    private final Mesajlar mesajlar;

    public KlanYonetimKomutu(KlanEklentisi eklenti) {
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

    private static final Map<String, String> IZIN_ESLEME = Map.of(
            "sil", "klan.komut.yonetim.sil",
            "liste", "klan.komut.yonetim.liste",
            "yenile", "klan.komut.yonetim.yenile",
            "menu", "klan.komut.yonetim.menu",
            "gui", "klan.komut.yonetim.menu"
    );

    @Override
    public boolean onCommand(CommandSender gonderen, Command komut, String etiket, String[] args) {
        try {
            return komutIsle(gonderen, args);
        } catch (Exception hata) {
            eklenti.getLoglayici().hataKaydet("/klanyonetim komutu (" + String.join(" ", args) + ")", hata);
            gonder(gonderen, "genel.beklenmeyen-hata");
            return true;
        }
    }

    private boolean komutIsle(CommandSender gonderen, String[] args) {
        if (args.length == 0) {
            for (String satir : eklenti.getLangYaml().getStringList("yonetim-yardim-metni")) {
                gonderen.sendMessage(Mesajlar.renkli(satir));
            }
            return true;
        }

        String alt = args[0].toLowerCase(TR);

        String izinDugumu = IZIN_ESLEME.get(alt);
        if (izinDugumu != null && !gonderen.hasPermission(izinDugumu)) {
            gonder(gonderen, "genel.izin-yok");
            return true;
        }

        switch (alt) {
            case "menu", "gui" -> {
                if (!(gonderen instanceof org.bukkit.entity.Player oyuncu)) {
                    gonder(gonderen, "genel.oyuncu-degil");
                    return true;
                }
                new com.klaneklentisi.klan.gui.admin.AdminAnaMenu(eklenti, oyuncu).ac();
            }
            case "sil" -> {
                if (args.length < 2) {
                    gonderen.sendMessage(Mesajlar.renkli("&c➤ Kullanım: &f/klanyonetim sil <isim>"));
                    return true;
                }
                if (yonetici.klanBul(args[1]).isEmpty()) {
                    gonder(gonderen, "yonetim.klan-yok");
                    return true;
                }
                yonetici.klanSil(args[1]);
                Map<String, String> yer = new HashMap<>();
                yer.put("isim", args[1]);
                gonder(gonderen, "yonetim.silindi", yer);
            }
            case "liste" -> {
                for (Klan klan : yonetici.tumKlanlar()) {
                    gonderen.sendMessage(Mesajlar.renkli("&8➤ &f" + klan.getIsim() + " &8[&7" + klan.getEtiket() + "&8]"));
                }
            }
            case "yenile" -> {
                eklenti.reloadConfig();
                mesajlar.yukle();
                eklenti.getKomutAyarlari().yukle();
                gonder(gonderen, "yonetim.yenilendi");
            }
            default -> gonderen.sendMessage(Mesajlar.renkli("&c✖ Bilinmeyen komut. /klanyonetim yazınız."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender gonderen, Command komut, String etiket, String[] args) {
        if (args.length == 1) {
            return List.of("sil", "liste", "yenile", "menu").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase(TR)))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("sil")) {
            return yonetici.tumKlanlar().stream().map(Klan::getIsim).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}

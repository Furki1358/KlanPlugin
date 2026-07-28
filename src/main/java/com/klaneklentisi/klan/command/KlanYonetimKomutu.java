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

    @Override
    public boolean onCommand(CommandSender gonderen, Command komut, String etiket, String[] args) {
        if (args.length == 0) {
            for (String satir : eklenti.getLangYaml().getStringList("yonetim-yardim-metni")) {
                gonderen.sendMessage(Mesajlar.renkli(satir));
            }
            return true;
        }

        String alt = args[0].toLowerCase(TR);
        switch (alt) {
            case "sil" -> {
                if (args.length < 2) {
                    gonderen.sendMessage(Mesajlar.renkli("&cKullanım: /klanyonetim sil <isim>"));
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
                    gonderen.sendMessage(Mesajlar.renkli("&7- &f" + klan.getIsim() + " &8[&7" + klan.getEtiket() + "&8]"));
                }
            }
            case "yenile" -> {
                eklenti.reloadConfig();
                mesajlar.yukle();
                gonder(gonderen, "yonetim.yenilendi");
            }
            default -> gonderen.sendMessage(Mesajlar.renkli("&cBilinmeyen komut. /klanyonetim yazınız."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender gonderen, Command komut, String etiket, String[] args) {
        if (args.length == 1) {
            return List.of("sil", "liste", "yenile").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase(TR)))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("sil")) {
            return yonetici.tumKlanlar().stream().map(Klan::getIsim).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}

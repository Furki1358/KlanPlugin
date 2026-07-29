package com.klaneklentisi.klan.istatistik;

import java.util.Map;
import java.util.UUID;

public interface IstatistikDeposu {
    Map<UUID, OyuncuIstatistik> tumunuYukle();
    void kaydet(Map<UUID, OyuncuIstatistik> istatistikler);
}

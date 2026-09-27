package org.mvs.talons.talonchecker.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MonthName {
    JANUARY(1, "січня"),
    FEBRUARY(2, "лютого"),
    MARCH(3, "березня"),
    APRIL(4, "квітня"),
    MAY(5, "травня"),
    JUNE(6, "червня"),
    JULY(7, "липня"),
    AUGUST(8, "серпня"),
    SEPTEMBER(9, "вересня"),
    OCTOBER(10, "жовтня"),
    NOVEMBER(11, "листопада"),
    DECEMBER(12, "грудня");

    private final int value;
    private final String nameUa;

    public static String getNameUaByValue(int value) {
        for (MonthName month : values()) {
            if (month.value == value) {
                return month.nameUa;
            }
        }
        return "";
    }
}
